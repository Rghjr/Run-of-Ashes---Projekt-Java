package com.runofashes.ui;

import com.runofashes.model.Difficulty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.EnumMap;
import java.util.Map;

public class DifficultyScreen extends VBox {

    private Difficulty selected = null;
    private final Runnable onConfirm;

    private final Button confirmBtn;

    private final Map<Difficulty, VBox>   cards       = new EnumMap<>(Difficulty.class);

    public DifficultyScreen(Runnable onConfirm) {
        this.onConfirm = onConfirm;

        getStyleClass().add("root-pane");
        setAlignment(Pos.CENTER);
        setSpacing(32);
        setPadding(new Insets(60, 48, 48, 48));

        Label title = new Label("Wybierz trudność");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
        title.getStyleClass().addAll("screen-title", "screen-title-large");

        Label subtitle = new Label("Trudność wpływa na tempo spadku statów, szansę sukcesu i zasady wyboru cech.");
        subtitle.getStyleClass().add("screen-subtitle");
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(TextAlignment.CENTER);
        subtitle.setMaxWidth(520);

        HBox cardRow = new HBox(20);
        cardRow.setAlignment(Pos.CENTER);
        for (Difficulty diff : Difficulty.values()) {
            VBox card = buildCard(diff);
            cards.put(diff, card);
            cardRow.getChildren().add(card);
        }

        confirmBtn = new Button("Dalej →");
        confirmBtn.setDisable(true);
        confirmBtn.getStyleClass().add("primary-button");
        confirmBtn.setOnAction(e -> onConfirm.run());

        getChildren().addAll(title, subtitle, cardRow, confirmBtn);
    }

    public Difficulty getSelected() { return selected; }

    // ─── Budowanie kafelka ────────────────────────────────────────────────────

    private VBox buildCard(Difficulty diff) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(24));
        card.setMinWidth(210);
        card.setMaxWidth(210);
        card.setAlignment(Pos.TOP_CENTER);

        card.getStyleClass().addAll("selection-card", "diff-card");
        String colorClass = getColorClass(diff);

        FontIcon icon = new FontIcon(diff.getEmoji());
        icon.setIconSize(40);
        icon.getStyleClass().addAll("card-icon", colorClass);

        Label name = new Label(diff.getLabel());
        name.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        name.getStyleClass().addAll("diff-name", colorClass);

        Label desc = new Label(diff.getDescription());
        desc.getStyleClass().add("diff-desc");
        desc.setWrapText(true);
        desc.setTextAlignment(TextAlignment.CENTER);

        Label rules = new Label(diff.getRulesText());
        rules.getStyleClass().add("diff-rules");
        rules.setWrapText(true);
        rules.setTextAlignment(TextAlignment.CENTER);

        card.getChildren().addAll(icon, name, desc, rules, buildModsBox(diff, colorClass));

        card.setOnMouseClicked(e -> selectDifficulty(diff));

        return card;
    }

    // ─── Selekcja ─────────────────────────────────────────────────────────────

    private void selectDifficulty(Difficulty diff) {
        if (selected != null) cards.get(selected).getStyleClass().remove(getSelectionClass(selected));
        selected = diff;
        cards.get(diff).getStyleClass().add(getSelectionClass(diff));
        confirmBtn.setDisable(false);
    }

    private String getSelectionClass(Difficulty diff) {
        return switch (diff) {
            case EASY   -> "selection-card-selected-green";
            case NORMAL -> "selection-card-selected-yellow";
            case HARD   -> "selection-card-selected-red";
        };
    }

    private String getColorClass(Difficulty diff) {
        return switch (diff) {
            case EASY   -> "color-easy";
            case NORMAL -> "color-normal";
            case HARD   -> "color-hard";
        };
    }

    // ─── Modyfikatory ─────────────────────────────────────────────────────────

    private VBox buildModsBox(Difficulty diff, String colorClass) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(8, 0, 0, 0));
        box.setAlignment(Pos.CENTER);

        if (diff.getStartStatBonus() != 0) {
            String val = (diff.getStartStatBonus() > 0 ? "+" : "") + diff.getStartStatBonus();
            box.getChildren().add(modLabel("Staty startowe: " + val, colorClass));
        }
        if (diff.getSuccessBonus() != 0) {
            int pct = (int)(diff.getSuccessBonus() * 100);
            box.getChildren().add(modLabel((pct > 0 ? "+" : "") + pct + "% szansa sukcesu", colorClass));
        }
        if (diff.getDrainMultiplier() != 1.0) {
            box.getChildren().add(modLabel("Głód/woda ×" + diff.getDrainMultiplier(), colorClass));
        }
        return box;
    }

    private Label modLabel(String text, String colorClass) {
        Label l = new Label(text);
        l.getStyleClass().addAll("diff-mod-label", colorClass);
        return l;
    }
}