package com.runofashes.ui;

import com.runofashes.engine.GameEngine;
import com.runofashes.model.Biome;
import com.runofashes.model.StatusEffect;
import com.runofashes.model.Weather;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

public class BiomePanel extends VBox {

    private final FlowPane titleFlow    = new FlowPane(12, 6);
    private final Label descLabel    = new Label();
    private final Label effectsLabel = new Label();
    private final VBox  statusesBox  = new VBox(6);

    public BiomePanel() {
        setMinHeight(Region.USE_PREF_SIZE);

        descLabel.setWrapText(true);
        descLabel.setMinHeight(Region.USE_PREF_SIZE);
        descLabel.getStyleClass().add("biome-desc");
        effectsLabel.setWrapText(true);
        effectsLabel.setMinHeight(Region.USE_PREF_SIZE);
        effectsLabel.getStyleClass().add("biome-effects");

        statusesBox.setPadding(new Insets(12, 0, 0, 0));
        statusesBox.setMinHeight(Region.USE_PREF_SIZE);

        setSpacing(10);
        getStyleClass().add("biome-panel");
        getChildren().addAll(titleFlow, descLabel, effectsLabel);
    }

    public VBox getStatusesBox() {
        return statusesBox;
    }

    public void refresh(GameEngine engine, Label messageLabel) {
        Biome currentBiome     = engine.getCurrentBiome();
        Weather currentWeather = engine.getCurrentWeather();
        String currentStage    = engine.getCurrentStageName();
        titleFlow.getChildren().clear();

        Label stageLbl = new Label("🚩 " + currentStage.toUpperCase() + " |");
        stageLbl.getStyleClass().add("biome-title");

        Label biomeLbl = new Label(currentBiome.getLabel().toUpperCase() + " |");
        biomeLbl.setGraphic(new FontIcon(currentBiome.getEmoji()));
        biomeLbl.getStyleClass().add("biome-title");

        Label weatherLbl = new Label(currentWeather.getLabel().toUpperCase());
        weatherLbl.setGraphic(new FontIcon(currentWeather.getEmoji()));
        weatherLbl.getStyleClass().add("biome-title");

        titleFlow.getChildren().addAll(stageLbl, biomeLbl, weatherLbl);

        descLabel.setText(currentBiome.getEntryMessage());
        effectsLabel.setText(engine.buildBiomeInfo(currentBiome));

        StatusEffect triggered = engine.getStatusManager().consumeLastTriggered();
        if (triggered != null) {
            messageLabel.setText(messageLabel.getText()
                    + "\n[!] Nowy status: " + triggered.getLabel()
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

                Label nameLbl = new Label(status.getLabel() + " (" + turns + " " + t + ")");
                nameLbl.setGraphic(new FontIcon(status.getEmoji()));
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
