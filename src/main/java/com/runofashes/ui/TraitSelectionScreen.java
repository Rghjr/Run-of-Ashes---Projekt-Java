package com.runofashes.ui;

import com.runofashes.model.Difficulty;
import com.runofashes.model.Trait;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.*;

/**
 * Ekran wyboru cech — wyświetla 5 pozytywnych i 5 negatywnych cech.
 * Zasady wyboru zależą od aktualnej trudności.
 * Wywołuje onConfirm gdy gracz potwierdzi legalny wybór.
 */
public class TraitSelectionScreen extends VBox {

    private final Difficulty difficulty;
    private final Runnable   onConfirm;
    private final Runnable   onBack;

    private final Set<Trait> selected = new LinkedHashSet<>();
    private final Map<Trait, VBox> cardMap = new EnumMap<>(Trait.class);

    private HBox     statusBox;
    private Label    statusLabel;
    private FontIcon statusIcon;
    private Button  confirmBtn;

    public TraitSelectionScreen(Difficulty difficulty, Runnable onConfirm, Runnable onBack) {
        this.difficulty = difficulty;
        this.onConfirm  = onConfirm;
        this.onBack     = onBack;

        getStyleClass().add("root-pane");
        setAlignment(Pos.TOP_CENTER);
        setSpacing(24);
        setPadding(new Insets(40, 40, 40, 40));

        Label title = new Label("Wybierz cechy postaci");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: #f0c040;");

        HBox rulesBanner = new HBox(12);
        rulesBanner.setAlignment(Pos.CENTER);
        rulesBanner.getStyleClass().add("info-banner");
        rulesBanner.setMaxWidth(680);

        FontIcon diffIcon = new FontIcon(difficulty.getEmoji());
        diffIcon.setIconSize(24);
        diffIcon.setIconColor(javafx.scene.paint.Color.web("#f0c040"));

        Label rulesLabel = new Label(difficulty.getLabel() + " — " + difficulty.getRulesText());
        rulesLabel.getStyleClass().add("info-banner-text");
        rulesLabel.setWrapText(true);

        rulesBanner.getChildren().addAll(diffIcon, rulesLabel);

        List<javafx.scene.Node> dynamicSections = new ArrayList<>();

        // Na poziomie EASY i NORMAL pokazujemy pozytywne
        if (difficulty == Difficulty.EASY || difficulty == Difficulty.NORMAL ||  difficulty == Difficulty.HARD) {
            Label posHeader = sectionHeader("✦  Cechy pozytywne", "#7ec8a0");
            TilePane posGrid = buildGrid(true);
            dynamicSections.add(posHeader);
            dynamicSections.add(posGrid);
        }

        // Na poziomie HARD i NORMAL pokazujemy negatywne
        if (difficulty == Difficulty.HARD || difficulty == Difficulty.NORMAL) {
            Label negHeader = sectionHeader("✦  Cechy negatywne", "#e74c3c");
            TilePane negGrid = buildGrid(false);
            dynamicSections.add(negHeader);
            dynamicSections.add(negGrid);
        }

        statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.getStyleClass().addAll("status-banner", "status-banner-default");
        statusBox.setMaxWidth(680);

        statusIcon = new FontIcon("fas-info-circle");
        statusIcon.setIconSize(18);
        statusIcon.setIconColor(javafx.scene.paint.Color.web("#888"));

        statusLabel = new Label("Wybierz cechy zgodnie z zasadami poziomu trudności.");
        statusLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13px;");

        statusBox.getChildren().addAll(statusIcon, statusLabel);

        Button backBtn = new Button("◀  Cofnij");
        backBtn.setStyle("""
            -fx-background-color: transparent; -fx-text-fill: #aaa;
            -fx-font-size: 16px; -fx-padding: 12 32;
            -fx-border-color: #444; -fx-border-radius: 6; -fx-cursor: hand;
        """);
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(backBtn.getStyle()
                .replace("#444", "#888").replace("#aaa", "#fff")));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(backBtn.getStyle()
                .replace("#888", "#444").replace("#fff", "#aaa")));
        backBtn.setOnAction(e -> onBack.run());

        confirmBtn = new Button("Rozpocznij grę ▶");
        confirmBtn.setDisable(!difficulty.isValidSelection(0, 0));
        confirmBtn.setStyle("""
            -fx-background-color: #2a3a1e; -fx-text-fill: #7ec8a0;
            -fx-font-size: 16px; -fx-padding: 12 40;
            -fx-background-radius: 6; -fx-cursor: hand;
        """);
        confirmBtn.setOnAction(e -> onConfirm.run());

        HBox buttonBox = new HBox(24, backBtn, confirmBtn);
        buttonBox.setAlignment(Pos.CENTER);

        getChildren().addAll(title, rulesBanner);
        getChildren().addAll(dynamicSections);
        getChildren().addAll(statusBox, buttonBox);

        updateStatus();
    }

    public Set<Trait> getSelected() { return Collections.unmodifiableSet(selected); }

    // ─── Budowanie siatki kart ────────────────────────────────────────────────

    private TilePane buildGrid(boolean positive) {
        TilePane pane = new TilePane(12, 12);
        pane.setPrefColumns(5);
        pane.setMaxWidth(860);
        pane.setAlignment(Pos.CENTER);

        for (Trait t : Trait.values()) {
            if (t.isPositive() == positive) {
                VBox card = buildTraitCard(t);
                cardMap.put(t, card);
                pane.getChildren().add(card);
            }
        }
        return pane;
    }

    private VBox buildTraitCard(Trait trait) {
        boolean isPos   = trait.isPositive();
        String normalBg = isPos ? "#1a2e1a" : "#2e1a1a";
        String accent   = isPos ? "#7ec8a0" : "#e74c3c";

        String selClass = isPos ? "selection-card-selected-green" : "selection-card-selected-red";

        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setPrefWidth(155);
        card.setAlignment(Pos.TOP_CENTER);

        card.getStyleClass().add("selection-card");
        card.setStyle("-fx-background-color: " + normalBg + ";");

        FontIcon icon = new FontIcon(trait.getEmoji());
        icon.setIconSize(28);
        icon.setIconColor(javafx.scene.paint.Color.web(accent));
        icon.getStyleClass().add("card-icon");

        Label name = new Label(trait.getLabel());
        name.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        name.setStyle("-fx-text-fill: " + accent + ";");
        name.setTextAlignment(TextAlignment.CENTER);
        name.setWrapText(true);

        Label desc = new Label(trait.getDescription());
        desc.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        desc.setWrapText(true);
        desc.setTextAlignment(TextAlignment.CENTER);

        // Efekty per-tura jeśli istnieją
        if (!trait.getPerTurnMods().isEmpty()) {
            HBox modBox = buildModBox(trait.getPerTurnMods(), accent);
            card.getChildren().addAll(icon, name, desc, modBox);
        } else {
            card.getChildren().addAll(icon, name, desc);
        }

        card.setOnMouseClicked(e -> toggleTrait(trait, card, selClass));

        return card;
    }

    private HBox buildModBox(Map<String, Integer> mods, String accent) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER);

        Label prefix = new Label("Per tura:");
        prefix.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        box.getChildren().add(prefix);

        mods.forEach((stat, val) -> {
            String valStr = (val > 0 ? "+" : "") + val;
            Label valLbl = new Label(valStr);
            valLbl.setStyle("-fx-text-fill: " + accent + "; -fx-font-size: 11px; -fx-font-weight: bold;");

            FontIcon statIcon = new FontIcon(getStatIconCode(stat));
            statIcon.setIconSize(11);
            statIcon.setIconColor(javafx.scene.paint.Color.web(accent));

            HBox statGroup = new HBox(3, valLbl, statIcon);
            statGroup.setAlignment(Pos.CENTER);
            box.getChildren().add(statGroup);
        });

        return box;
    }

    private static String getStatIconCode(String stat) {
        return switch (stat) {
            case "health"    -> "fas-heart";
            case "hunger"    -> "fas-drumstick-bite";
            case "hydration" -> "fas-tint";
            case "energy"    -> "fas-bolt";
            case "morale"    -> "fas-smile";
            default          -> "fas-star";
        };
    }

    // ─── Toggle cechy ─────────────────────────────────────────────────────────

    private void toggleTrait(Trait trait, VBox card, String selClass) {
        if (selected.contains(trait)) {
            // Odznaczenie zawsze dozwolone
            selected.remove(trait);
            card.getStyleClass().remove(selClass);
        } else {
            // Sprawdź limity absolutne
            if (trait.isPositive() && countPositive() >= difficulty.getMaxPositive()) {
                flashError("Osiągnięto limit cech pozytywnych dla tego poziomu.");
                return;
            }
            if (!trait.isPositive() && countNegative() >= difficulty.getMaxNegative()) {
                flashError("Osiągnięto limit cech negatywnych dla tego poziomu.");
                return;
            }

            selected.add(trait);
            card.getStyleClass().add(selClass);
        }
        updateStatus();
    }

    // ─── Status i walidacja ───────────────────────────────────────────────────

    private void updateStatus() {
        int pos = countPositive();
        int neg = countNegative();
        boolean valid = difficulty.isValidSelection(pos, neg);

        statusBox.getStyleClass().removeAll("status-banner-default", "status-banner-valid", "status-banner-error");

        if (valid) {
            statusBox.getStyleClass().add("status-banner-valid");
            statusIcon.setIconLiteral("fas-check-circle");
            statusIcon.setIconColor(javafx.scene.paint.Color.web("#7ec8a0"));
            statusLabel.setText(pos + " poz. | " + neg + " neg. — Gotowe do gry!");
            statusLabel.setStyle("-fx-text-fill: #7ec8a0; -fx-font-size: 13px; -fx-font-weight: bold;");
        } else {
            statusBox.getStyleClass().add("status-banner-default");
            statusIcon.setIconLiteral("fas-info-circle");
            statusIcon.setIconColor(javafx.scene.paint.Color.web("#f0c040"));
            statusLabel.setText(pos + " poz. | " + neg + " neg. — Wybierz kolejne cechy, aby spełnić warunki.");
            statusLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13px;");
        }

        // EASY i HARD: potwierdzamy od razu gdy warunki spełnione
        // NORMAL: 0+0 też jest OK
        confirmBtn.setDisable(!valid);
    }

    private void flashError(String msg) {
        statusBox.getStyleClass().removeAll("status-banner-default", "status-banner-valid", "status-banner-error");
        statusBox.getStyleClass().add("status-banner-error");

        statusIcon.setIconLiteral("fas-exclamation-triangle");
        statusIcon.setIconColor(javafx.scene.paint.Color.web("#e74c3c"));

        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px; -fx-font-weight: bold;");
    }

    private int countPositive() {
        return (int) selected.stream().filter(Trait::isPositive).count();
    }

    private int countNegative() {
        return (int) selected.stream().filter(t -> !t.isPositive()).count();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Label sectionHeader(String text, String color) {
        Label l = new Label(text);
        l.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        l.setStyle("-fx-text-fill: " + color + ";");
        return l;
    }
}