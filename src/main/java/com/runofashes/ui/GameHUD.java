package com.runofashes.ui;

import com.runofashes.engine.GameEngine;
import com.runofashes.model.Difficulty;
import com.runofashes.model.Player;
import com.runofashes.model.Trait;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class GameHUD extends VBox {

    private final GameEngine engine;

    private ProgressBar healthBar, hungerBar, hydrationBar, energyBar, moraleBar;
    private Label healthVal, hungerVal, hydrationVal, energyVal, moraleVal;
    private Label timeLabel, distanceLabel;
    private Label difficultyLabel, traitsLabel;
    private Label biomeLabel, weatherLabel;

    public GameHUD(GameEngine engine) {
        this.engine = engine;
        buildUI();
        setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 8;");
        setPadding(new Insets(16));
        setSpacing(8);
    }

    private void buildUI() {
        timeLabel     = styledLabel("Dzień 1,  00:00", "#aaa", 15);
        distanceLabel = styledLabel("4000 km do Krakowa", "#e67e22", 15);
        difficultyLabel = styledLabel("", "#888", 12);
        traitsLabel     = styledLabel("", "#666", 12);

        biomeLabel   = styledLabel("", "#2ecc71", 14);
        weatherLabel = styledLabel("", "#3498db", 14);

        HBox topRow = new HBox(24, timeLabel, distanceLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        HBox envRow = new HBox(24, biomeLabel, weatherLabel);
        envRow.setAlignment(Pos.CENTER_LEFT);

        HBox metaRow = new HBox(16, difficultyLabel, traitsLabel);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        healthBar    = makeBar("#e74c3c");
        hungerBar    = makeBar("#e67e22");
        hydrationBar = makeBar("#3498db");
        energyBar    = makeBar("#f1c40f");
        moraleBar    = makeBar("#9b59b6");

        healthVal    = valueLabel();
        hungerVal    = valueLabel();
        hydrationVal = valueLabel();
        energyVal    = valueLabel();
        moraleVal    = valueLabel();

        getChildren().addAll(
                topRow, envRow, metaRow,
                statRow("❤  Zdrowie",     healthBar,    healthVal),
                statRow("🍗  Głód",        hungerBar,    hungerVal),
                statRow("💧  Nawodnienie", hydrationBar, hydrationVal),
                statRow("⚡  Energia",     energyBar,    energyVal),
                statRow("😊  Nadzieja",    moraleBar,    moraleVal)
        );
    }

    public void refresh() {
        Player p = engine.getPlayer();

        setBar(healthBar,    healthVal,    p.getHealth(),    p.getMaxHealth());
        setBar(hungerBar,    hungerVal,    p.getHunger(),    p.getMaxHunger());
        setBar(hydrationBar, hydrationVal, p.getHydration(), p.getMaxHydration());
        setBar(energyBar,    energyVal,    p.getEnergy(),    p.getMaxEnergy());
        setBar(moraleBar,    moraleVal,    p.getMorale(),    p.getMaxMorale());

        timeLabel.setText(p.getTimeFormatted());
        distanceLabel.setText(p.getDistance() + " km do Krakowa");

        biomeLabel.setText(engine.getCurrentBiome().getEmoji() + " " + engine.getCurrentBiome().getLabel());
        weatherLabel.setText(engine.getCurrentWeather().getEmoji() + " " + engine.getCurrentWeather().getLabel());

        Difficulty diff = engine.getDifficulty();
        difficultyLabel.setText(diff.getEmoji() + " " + diff.getLabel());

        List<Trait> traits = engine.getTraitManager().getActiveTraits();
        if (traits.isEmpty()) {
            traitsLabel.setText("Brak cech");
        } else {
            StringBuilder sb = new StringBuilder();
            traits.forEach(t -> sb.append(t.getEmoji()).append(" ").append(t.getLabel()).append("  "));
            traitsLabel.setText(sb.toString().trim());
        }
    }

    // Helpery UI
    private HBox statRow(String name, ProgressBar bar, Label val) {
        Label n = new Label(name);
        n.setMinWidth(158);
        n.setStyle("-fx-text-fill: #ccc; -fx-font-size: 15px;");
        bar.setPrefWidth(240);
        HBox row = new HBox(12, n, bar, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private ProgressBar makeBar(String hex) {
        ProgressBar bar = new ProgressBar(1.0);
        bar.setPrefHeight(18);
        bar.setStyle("-fx-accent: " + hex + ";");
        return bar;
    }

    private void setBar(ProgressBar bar, Label lbl, int value, int maxValue) {
        bar.setProgress(Math.min(value, maxValue) / (double) maxValue);
        lbl.setText(value + "/" + maxValue);
    }

    private Label valueLabel() {
        Label l = new Label();
        l.setMinWidth(64);
        l.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
        return l;
    }

    private Label styledLabel(String text, String color, int size) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + size + "px;");
        return l;
    }
}