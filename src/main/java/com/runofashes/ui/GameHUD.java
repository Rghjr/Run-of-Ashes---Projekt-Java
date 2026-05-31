package com.runofashes.ui;

import com.runofashes.engine.GameEngine;
import com.runofashes.model.Difficulty;
import com.runofashes.model.GameEvent;
import com.runofashes.model.Player;
import com.runofashes.model.Trait;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Objects;

public class GameHUD extends VBox {

    private final GameEngine engine;

    private ProgressBar healthBar, hungerBar, hydrationBar, energyBar, moraleBar;
    private Label healthVal, hungerVal, hydrationVal, energyVal, moraleVal;

    private ProgressBar distanceBar;
    private Label timeLabel, distanceLabel;

    private Label difficultyLabel;
    private HBox  traitsBox, distanceBox;
    private Label biomeLabel, weatherLabel;

    private ImageView eventImageView;

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
        distanceBar     = makeBar("hud-bar-distance");
        distanceBar.setProgress(0.0);
        distanceBar.setPrefWidth(300);

        distanceBox = new HBox(12, distanceBar, distanceLabel);
        distanceBox.setAlignment(Pos.CENTER_LEFT);

        difficultyLabel = styledLabel("", "hud-difficulty");
        traitsBox       = new HBox(12);
        traitsBox.setAlignment(Pos.CENTER_LEFT);

        biomeLabel      = styledLabel("", "hud-biome");
        weatherLabel    = styledLabel("", "hud-weather");

        HBox topRow = new HBox(24, timeLabel, distanceBox);
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

        VBox statsBox = new VBox(8,
                statRow("❤  Zdrowie",     healthBar,    healthVal),
                statRow("🍗  Głód",        hungerBar,    hungerVal),
                statRow("💧  Nawodnienie", hydrationBar, hydrationVal),
                statRow("⚡  Energia",     energyBar,    energyVal),
                statRow("😊  Nadzieja",    moraleBar,    moraleVal)
        );

        StackPane imageContainer = createFadedImageWrapper(240, 180);

        HBox bottomRow = new HBox(40, statsBox, imageContainer);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(topRow, envRow, metaRow, bottomRow);
    }

    private StackPane createFadedImageWrapper(double width, double height) {
        eventImageView = new ImageView();
        eventImageView.setFitWidth(width);
        eventImageView.setFitHeight(height);
        eventImageView.setPreserveRatio(false);

        Rectangle vignette = new Rectangle(width, height);

        RadialGradient gradient = new RadialGradient(
                0, 0, 0.5, 0.5, 0.45, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.3, Color.TRANSPARENT),
                new Stop(1, Color.web("#1a1a2e", 1.0))
        );
        vignette.setFill(gradient);

        StackPane container = new StackPane(eventImageView, vignette);
        container.setPrefSize(width, height);
        return container;
    }

    public void setEventImage(String imageName) {
        Image img = com.runofashes.utils.FileLoader.loadUiImage(imageName);
        if (img != null) {
            eventImageView.setImage(img);
        } else {
            eventImageView.setImage(com.runofashes.utils.FileLoader.loadUiImage("event_default.png"));
        }
    }

    // --- NOWA METODA - SKANER KART ---
    public void setEventImage(GameEvent event) {
        if (event == null || event.getId() == null) return;

        String eventId = event.getId().toLowerCase();
        String imageName = "event_default.png";

        if (eventId.contains("village") || eventId.contains("wies") || eventId.contains("chata") || eventId.contains("inn")) {
            imageName = "village.png";
        } else if (eventId.contains("water") || eventId.contains("river") || eventId.contains("stream") || eventId.contains("drink") || eventId.contains("rain") || eventId.contains("ford")) {
            imageName = "river.png";
        } else if (eventId.contains("fight") || eventId.contains("soldier") || eventId.contains("wilki") || eventId.contains("hunt") || eventId.contains("guard")) {
            imageName = "soldier.png";
        } else if (eventId.contains("merchant") || eventId.contains("trade") || eventId.contains("buy") || eventId.contains("karawana")) {
            imageName = "merchant.png";
        } else if (eventId.contains("rest") || eventId.contains("sleep") || eventId.contains("camp") || eventId.contains("nap") || eventId.contains("fire")) {
            imageName = "camp.png";
        } else if (eventId.contains("monastery") || eventId.contains("church") || eventId.contains("cross") || eventId.contains("pray") || eventId.contains("monk") || eventId.contains("bishop")) {
            imageName = "religion.png";
        } else if (eventId.contains("mountain") || eventId.contains("gory") || eventId.contains("lawina") || eventId.contains("szczelina")) {
            imageName = "mountains.png";
        } else if (eventId.contains("forest") || eventId.contains("las") || eventId.contains("tree") || eventId.contains("wood")) {
            imageName = "forest.png";
        }

        setEventImage(imageName);
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

        double startDistance = 4000.0;
        double actualProgress = Math.max(0, startDistance - p.getDistance()) / startDistance;

        animateBar(distanceBar, actualProgress);

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
        double targetProgress = Math.min(value, maxValue) / (double) maxValue;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(400), new KeyValue(bar.progressProperty(), targetProgress))
        );
        timeline.play();

        lbl.setText(value + "/" + maxValue);
    }

    private void animateBar(ProgressBar bar, double targetProgress) {
        Timeline oldTimeline = (Timeline) bar.getProperties().get("timeline");
        if (oldTimeline != null) {
            oldTimeline.stop();
        }

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(400), new KeyValue(bar.progressProperty(), targetProgress))
        );

        bar.getProperties().put("timeline", timeline);
        timeline.play();
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