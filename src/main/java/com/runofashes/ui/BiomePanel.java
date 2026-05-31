package com.runofashes.ui;

import com.runofashes.engine.GameEngine;
import com.runofashes.model.Biome;
import com.runofashes.model.StatusEffect;
import com.runofashes.model.Weather;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;

public class BiomePanel extends VBox {

    private final Label titleLabel   = new Label();
    private final Label descLabel    = new Label();
    private final Label effectsLabel = new Label();
    private final VBox  statusesBox  = new VBox(6);

    public BiomePanel() {
        titleLabel.getStyleClass().add("biome-title");
        descLabel.setWrapText(true);
        descLabel.setMinHeight(Region.USE_PREF_SIZE);
        descLabel.getStyleClass().add("biome-desc");
        effectsLabel.setWrapText(true);
        effectsLabel.setMinHeight(Region.USE_PREF_SIZE);
        effectsLabel.getStyleClass().add("biome-effects");

        statusesBox.setPadding(new Insets(12, 0, 0, 0));
        statusesBox.setMinHeight(80);

        setSpacing(8);
        getStyleClass().add("biome-panel");
        getChildren().addAll(titleLabel, descLabel, effectsLabel);
    }

    public VBox getStatusesBox() {
        return statusesBox;
    }

    public void refresh(GameEngine engine, Label messageLabel) {
        Biome currentBiome     = engine.getCurrentBiome();
        Weather currentWeather = engine.getCurrentWeather();
        String currentStage    = engine.getCurrentStageName();

        titleLabel.setText(
                "🚩 " + currentStage.toUpperCase()
                        + "   |   " + currentBiome.getEmoji() + " " + currentBiome.getLabel().toUpperCase()
                        + "   |   " + currentWeather.getEmoji() + " " + currentWeather.getLabel().toUpperCase()
        );
        descLabel.setText(currentBiome.getEntryMessage());
        effectsLabel.setText(engine.buildBiomeInfo(currentBiome));

        StatusEffect triggered = engine.getStatusManager().consumeLastTriggered();
        if (triggered != null) {
            messageLabel.setText(messageLabel.getText()
                    + "\n" + triggered.getEmoji() + " Nowy status: " + triggered.getLabel()
                    + " — " + triggered.getDescription());
        }

        statusesBox.getChildren().clear();
        Label statusTitle = new Label("✦ Aktywne statusy");
        statusTitle.getStyleClass().add("biome-status-title");
        statusesBox.getChildren().add(statusTitle);

        var statuses = engine.getStatusManager().getActiveStatuses();
        if (statuses.isEmpty()) {
            Label empty = new Label("Brak aktywnych statusów.");
            empty.getStyleClass().add("biome-status-empty");
            statusesBox.getChildren().add(empty);
        } else {
            statuses.forEach((status, turns) -> {
                VBox statusBox = new VBox(2);
                String t = turns == 1 ? "tura" : (turns < 5 ? "tury" : "tur");
                Label nameLbl = new Label(status.getEmoji() + " " + status.getLabel() + " (" + turns + " " + t + ")");
                nameLbl.getStyleClass().add("biome-status-name");
                Label descLbl = new Label(status.getDescription());
                descLbl.getStyleClass().add("biome-status-desc");
                descLbl.setWrapText(true);
                descLbl.setMinHeight(Region.USE_PREF_SIZE);
                statusBox.getChildren().addAll(nameLbl, descLbl);
                statusesBox.getChildren().add(statusBox);
            });
        }
    }
}
