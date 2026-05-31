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
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class GameHUD extends VBox {

    private final GameEngine engine;

    private ProgressBar healthBar, hungerBar, hydrationBar, energyBar, moraleBar;
    private Label healthVal, hungerVal, hydrationVal, energyVal, moraleVal;
    private Label timeLabel, distanceLabel;
    private Label difficultyLabel;
    private HBox  traitsBox;
    private Label biomeLabel, weatherLabel;

    public GameHUD(GameEngine engine) {
        this.engine = engine;
        buildUI();
        getStyleClass().add("game-hud");
        setPadding(new Insets(16));
        setSpacing(8);
    }

    private void buildUI() {
        timeLabel       = styledLabel("Dzień 1,  00:00", "hud-time");
        distanceLabel   = styledLabel("4000 km do Krakowa", "hud-distance");
        difficultyLabel = styledLabel("", "hud-difficulty");
        traitsBox       = new HBox(12);
        traitsBox.setAlignment(Pos.CENTER_LEFT);

        biomeLabel      = styledLabel("", "hud-biome");
        weatherLabel    = styledLabel("", "hud-weather");

        HBox topRow = new HBox(24, timeLabel, distanceLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        HBox envRow = new HBox(24, biomeLabel, weatherLabel);
        envRow.setAlignment(Pos.CENTER_LEFT);

        HBox metaRow = new HBox(16, difficultyLabel, traitsBox);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        healthBar    = makeBar("hud-bar-health");
        hungerBar    = makeBar("hud-bar-hunger");
        hydrationBar = makeBar("hud-bar-hydration");
        energyBar    = makeBar("hud-bar-energy");
        moraleBar    = makeBar("hud-bar-morale");

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

        biomeLabel.setText("🚩 " + engine.getCurrentStageName() + "  |  " + engine.getCurrentBiome().getLabel());
        biomeLabel.setGraphic(new FontIcon(engine.getCurrentBiome().getEmoji()));

        weatherLabel.setText(engine.getCurrentWeather().getLabel());
        weatherLabel.setGraphic(new FontIcon(engine.getCurrentWeather().getEmoji()));

        Difficulty diff = engine.getDifficulty();
        difficultyLabel.setText(diff.getLabel());
        difficultyLabel.setGraphic(new FontIcon(diff.getEmoji()));

        traitsBox.getChildren().clear();
        List<Trait> traits = engine.getTraitManager().getActiveTraits();
        if (traits.isEmpty()) {
            traitsBox.getChildren().add(styledLabel("Brak cech", "hud-traits"));
        } else {
            for (Trait t : traits) {
                Label traitLbl = styledLabel(t.getLabel(), "hud-traits");
                traitLbl.setGraphic(new FontIcon(t.getEmoji()));
                traitsBox.getChildren().add(traitLbl);
            }
        }
    }

    // Helpery UI
    private HBox statRow(String name, ProgressBar bar, Label val) {
        Label n = new Label(name);
        n.setMinWidth(158);
        n.getStyleClass().add("hud-stat-name");
        bar.setPrefWidth(240);
        HBox row = new HBox(12, n, bar, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private ProgressBar makeBar(String cssClass) {
        ProgressBar bar = new ProgressBar(1.0);
        bar.setPrefHeight(18);
        bar.getStyleClass().add(cssClass);
        return bar;
    }

    private void setBar(ProgressBar bar, Label lbl, int value, int maxValue) {
        bar.setProgress(Math.min(value, maxValue) / (double) maxValue);
        lbl.setText(value + "/" + maxValue);
    }

    private Label valueLabel() {
        Label l = new Label();
        l.setMinWidth(64);
        l.getStyleClass().add("hud-stat-value");
        return l;
    }

    private Label styledLabel(String text, String cssClass) {
        Label l = new Label(text);
        l.getStyleClass().add(cssClass);
        return l;
    }
}