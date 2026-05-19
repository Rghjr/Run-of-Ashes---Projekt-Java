package com.runofashes.ui;

import com.runofashes.model.Difficulty;
import com.runofashes.model.Trait;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

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

    private Label   statusLabel;
    private Button  confirmBtn;

    public TraitSelectionScreen(Difficulty difficulty, Runnable onConfirm, Runnable onBack) {
        this.difficulty = difficulty;
        this.onConfirm  = onConfirm;
        this.onBack     = onBack;

        setStyle("-fx-background-color: #0d0d1a;");
        setAlignment(Pos.TOP_CENTER);
        setSpacing(24);
        setPadding(new Insets(40, 40, 40, 40));

        Label title = new Label("Wybierz cechy postaci");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: #f0c040;");

        Label rulesLabel = new Label(difficulty.getEmoji() + "  " + difficulty.getLabel()
                + " — " + difficulty.getRulesText());
        rulesLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13px;");
        rulesLabel.setWrapText(true);
        rulesLabel.setMaxWidth(680);
        rulesLabel.setTextAlignment(TextAlignment.CENTER);

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

        statusLabel = new Label("Wybierz cechy zgodnie z zasadami poziomu trudności.");
        statusLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(680);
        statusLabel.setTextAlignment(TextAlignment.CENTER);

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

        getChildren().addAll(title, rulesLabel);
        getChildren().addAll(dynamicSections);
        getChildren().addAll(statusLabel, buttonBox);
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
        String selBg    = isPos ? "#1f4a1f" : "#4a1f1f";
        String accent   = isPos ? "#7ec8a0" : "#e74c3c";

        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setPrefWidth(155);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: " + normalBg + "; -fx-background-radius: 8; -fx-cursor: hand;");

        Label emoji = new Label(trait.getEmoji());
        emoji.setStyle("-fx-font-size: 22px;");

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
            String modTxt = buildModString(trait.getPerTurnMods());
            Label modLbl = new Label("Per tura: " + modTxt);
            modLbl.setStyle("-fx-text-fill: " + accent + "; -fx-font-size: 10px;");
            modLbl.setWrapText(true);
            modLbl.setTextAlignment(TextAlignment.CENTER);
            card.getChildren().addAll(emoji, name, desc, modLbl);
        } else {
            card.getChildren().addAll(emoji, name, desc);
        }

        card.setOnMouseEntered(e -> {
            if (!selected.contains(trait))
                card.setStyle("-fx-background-color: " + darken(normalBg)
                        + "; -fx-background-radius: 8; -fx-cursor: hand;");
        });
        card.setOnMouseExited(e -> {
            if (!selected.contains(trait))
                card.setStyle("-fx-background-color: " + normalBg
                        + "; -fx-background-radius: 8; -fx-cursor: hand;");
        });
        card.setOnMouseClicked(e -> toggleTrait(trait, card, normalBg, selBg, accent));

        return card;
    }

    // ─── Toggle cechy ─────────────────────────────────────────────────────────

    private void toggleTrait(Trait trait, VBox card, String normalBg, String selBg, String accent) {
        if (selected.contains(trait)) {
            // Odznaczenie zawsze dozwolone
            selected.remove(trait);
            card.setStyle("-fx-background-color: " + normalBg + "; -fx-background-radius: 8; -fx-cursor: hand;");
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
            card.setStyle("-fx-background-color: " + selBg
                    + "; -fx-background-radius: 8; -fx-border-color: " + accent
                    + "; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-cursor: hand;");
        }
        updateStatus();
    }

    // ─── Status i walidacja ───────────────────────────────────────────────────

    private void updateStatus() {
        int pos = countPositive();
        int neg = countNegative();
        boolean valid = difficulty.isValidSelection(pos, neg);

        statusLabel.setText(pos + " pozytywnych, " + neg + " negatywnych"
                + (valid ? " ✓" : " — jeszcze nie gotowe"));
        statusLabel.setStyle("-fx-text-fill: " + (valid ? "#7ec8a0" : "#f0c040") + "; -fx-font-size: 13px;");

        // EASY i HARD: potwierdzamy od razu gdy warunki spełnione
        // NORMAL: 0+0 też jest OK
        confirmBtn.setDisable(!valid);
    }

    private void flashError(String msg) {
        statusLabel.setText("⚠  " + msg);
        statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
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

    private String buildModString(Map<String, Integer> mods) {
        StringBuilder sb = new StringBuilder();
        mods.forEach((stat, val) ->
                sb.append(val > 0 ? "+" : "").append(val).append(" ").append(statEmoji(stat)).append(" "));
        return sb.toString().trim();
    }

    private static String statEmoji(String stat) {
        return switch (stat) {
            case "health"    -> "❤";
            case "hunger"    -> "🍗";
            case "hydration" -> "💧";
            case "energy"    -> "⚡";
            case "morale"    -> "😊";
            default          -> stat;
        };
    }

    /** Lekkie przyciemnienie koloru hex do efektu hover — prosty heurystyk. */
    private static String darken(String hex) {
        // Zamiast parsować, zwracamy po prostu trochę jaśniejszy wariant
        return hex.replace("1a", "22").replace("2e", "3a");
    }
}