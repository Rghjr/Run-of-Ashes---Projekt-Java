package com.runofashes.ui;

import com.runofashes.engine.GameEngine;
import com.runofashes.model.Biome;
import com.runofashes.model.StatusEffect;
import com.runofashes.model.Weather;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class BiomePanel extends VBox {

    private final Label titleLabel   = new Label();
    private final Label descLabel    = new Label();
    private final Label effectsLabel = new Label();
    private final VBox  statusesBox  = new VBox(6);

    public BiomePanel() {
        titleLabel.setStyle("-fx-text-fill: #f0c040; -fx-font-size: 14px; -fx-font-weight: bold;");
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12px; -fx-font-style: italic;");
        effectsLabel.setWrapText(true);
        effectsLabel.setStyle("-fx-text-fill: #ccc; -fx-font-size: 13px; -fx-line-spacing: 5px;");

        statusesBox.setPadding(new Insets(12, 0, 0, 0));
        statusesBox.setMinHeight(80);

        setSpacing(8);
        setStyle("""
            -fx-background-color: #151522;
            -fx-padding: 16;
            -fx-background-radius: 8;
            -fx-border-color: #2a2a3a;
            -fx-border-radius: 8;
            -fx-border-width: 1;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);
        """);
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

        StatusEffect triggered = engine.getStatusManager().getLastTriggered();
        if (triggered != null) {
            messageLabel.setText(messageLabel.getText()
                    + "\n" + triggered.getEmoji() + " Nowy status: " + triggered.getLabel()
                    + " — " + triggered.getDescription());
        }

        statusesBox.getChildren().clear();
        Label statusTitle = new Label("✦ Aktywne statusy");
        statusTitle.setStyle("-fx-text-fill: #9ab; -fx-font-size: 14px;");
        statusesBox.getChildren().add(statusTitle);

        var statuses = engine.getStatusManager().getActiveStatuses();
        if (statuses.isEmpty()) {
            Label empty = new Label("Brak aktywnych statusów.");
            empty.setStyle("-fx-text-fill: #555; -fx-font-style: italic; -fx-font-size: 13px;");
            statusesBox.getChildren().add(empty);
        } else {
            statuses.forEach((status, turns) -> {
                VBox statusBox = new VBox(2);
                String t = turns == 1 ? "tura" : (turns < 5 ? "tury" : "tur");
                Label nameLbl = new Label(status.getEmoji() + " " + status.getLabel() + " (" + turns + " " + t + ")");
                nameLbl.setStyle("-fx-text-fill: #f0c040; -fx-font-size: 13px;");
                Label descLbl = new Label(status.getDescription());
                descLbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
                descLbl.setWrapText(true);
                statusBox.getChildren().addAll(nameLbl, descLbl);
                statusesBox.getChildren().add(statusBox);
            });
        }
    }
}
