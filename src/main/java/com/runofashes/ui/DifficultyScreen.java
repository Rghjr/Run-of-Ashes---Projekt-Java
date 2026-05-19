package com.runofashes.ui;

import com.runofashes.model.Difficulty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import java.util.EnumMap;
import java.util.Map;

public class DifficultyScreen extends VBox {

    private Difficulty selected = null;
    private final Runnable onConfirm;

    private Button confirmBtn;

    private final Map<Difficulty, VBox>   cards       = new EnumMap<>(Difficulty.class);
    private final Map<Difficulty, String> normalBgs   = new EnumMap<>(Difficulty.class);
    private final Map<Difficulty, String> selectedBgs = new EnumMap<>(Difficulty.class);
    private final Map<Difficulty, String> accents     = new EnumMap<>(Difficulty.class);

    public DifficultyScreen(Runnable onConfirm) {
        this.onConfirm = onConfirm;

        setStyle("-fx-background-color: #0d0d1a;");
        setAlignment(Pos.CENTER);
        setSpacing(32);
        setPadding(new Insets(60, 48, 48, 48));

        normalBgs.put(Difficulty.EASY,   "#16213e");
        normalBgs.put(Difficulty.NORMAL, "#16213e");
        normalBgs.put(Difficulty.HARD,   "#16213e");

        selectedBgs.put(Difficulty.EASY,   "#1a2e1a");
        selectedBgs.put(Difficulty.NORMAL, "#1a1a2e");
        selectedBgs.put(Difficulty.HARD,   "#2e1a1a");

        accents.put(Difficulty.EASY,   "#7ec8a0");
        accents.put(Difficulty.NORMAL, "#f0c040");
        accents.put(Difficulty.HARD,   "#e74c3c");

        Label title = new Label("Wybierz trudność");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
        title.setStyle("-fx-text-fill: #f0c040;");

        Label subtitle = new Label("Trudność wpływa na tempo spadku statów, szansę sukcesu i zasady wyboru cech.");
        subtitle.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
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
        confirmBtn.setStyle(
                "-fx-background-color: #2a3a1e; -fx-text-fill: #7ec8a0;" +
                        "-fx-font-size: 16px; -fx-padding: 12 40;" +
                        "-fx-background-radius: 6; -fx-cursor: hand;"
        );
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
        applyNormalStyle(card, diff);

        Label emoji = new Label(diff.getEmoji());
        emoji.setStyle("-fx-font-size: 32px;");

        Label name = new Label(diff.getLabel());
        name.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        name.setStyle("-fx-text-fill: " + accents.get(diff) + ";");

        Label desc = new Label(diff.getDescription());
        desc.setStyle("-fx-text-fill: #ccc; -fx-font-size: 13px;");
        desc.setWrapText(true);
        desc.setTextAlignment(TextAlignment.CENTER);

        Label rules = new Label(diff.getRulesText());
        rules.setStyle("-fx-text-fill: #888; -fx-font-size: 12px; -fx-font-style: italic;");
        rules.setWrapText(true);
        rules.setTextAlignment(TextAlignment.CENTER);

        card.getChildren().addAll(emoji, name, desc, rules, buildModsBox(diff));

        card.setOnMouseEntered(e -> {
            if (selected != diff)
                card.setStyle("-fx-background-color: #1e2a3e; -fx-background-radius: 10; -fx-cursor: hand;" +
                        " -fx-border-color: transparent; -fx-border-width: 1; -fx-border-radius: 10;");
        });
        card.setOnMouseExited(e -> {
            if (selected != diff) applyNormalStyle(card, diff);
        });
        card.setOnMouseClicked(e -> selectDifficulty(diff));

        return card;
    }

    // ─── Selekcja ─────────────────────────────────────────────────────────────

    private void selectDifficulty(Difficulty diff) {
        if (selected != null) applyNormalStyle(cards.get(selected), selected);
        selected = diff;
        applySelectedStyle(cards.get(diff), diff);
        confirmBtn.setDisable(false);
    }

    private void applyNormalStyle(VBox card, Difficulty diff) {
        card.setStyle("-fx-background-color: " + normalBgs.get(diff) +
                "; -fx-background-radius: 10; -fx-cursor: hand;" +
                " -fx-border-color: transparent; -fx-border-width: 1; -fx-border-radius: 10;");
    }

    private void applySelectedStyle(VBox card, Difficulty diff) {
        card.setStyle("-fx-background-color: " + selectedBgs.get(diff) +
                "; -fx-background-radius: 10; -fx-cursor: hand;" +
                " -fx-border-color: " + accents.get(diff) +
                "; -fx-border-width: 1.5; -fx-border-radius: 10;");
    }

    // ─── Modyfikatory ─────────────────────────────────────────────────────────

    private VBox buildModsBox(Difficulty diff) {
        String accent = accents.get(diff);
        VBox box = new VBox(4);
        box.setPadding(new Insets(8, 0, 0, 0));
        box.setAlignment(Pos.CENTER);

        if (diff.getStartStatBonus() != 0) {
            String val = (diff.getStartStatBonus() > 0 ? "+" : "") + diff.getStartStatBonus();
            box.getChildren().add(modLabel("Staty startowe: " + val, accent));
        }
        if (diff.getSuccessBonus() != 0) {
            int pct = (int)(diff.getSuccessBonus() * 100);
            box.getChildren().add(modLabel((pct > 0 ? "+" : "") + pct + "% szansa sukcesu", accent));
        }
        if (diff.getDrainMultiplier() != 1.0) {
            box.getChildren().add(modLabel("Głód/woda ×" + diff.getDrainMultiplier(), accent));
        }
        return box;
    }

    private Label modLabel(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
        return l;
    }
}