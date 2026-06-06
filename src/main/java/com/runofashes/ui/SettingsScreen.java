package com.runofashes.ui;

import com.runofashes.engine.AudioManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class SettingsScreen extends VBox {

    public SettingsScreen(AudioManager audioManager, Runnable onBack) {
        setStyle("-fx-background-color: #05050a;");
        setAlignment(Pos.CENTER);
        setSpacing(30);

        Label title = new Label("USTAWIENIA");
        title.setFont(Font.font("Palatino Linotype", 48));
        title.setStyle("-fx-text-fill: #d1c8bd; -fx-padding: 0 0 30 0;");

        Label volLabel = new Label("Głośność muzyki");
        volLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        Slider volSlider = new Slider(0.0, 1.0, audioManager.getVolume());
        volSlider.setMaxWidth(300);
        volSlider.setStyle("-fx-cursor: hand;");

        volSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            audioManager.setVolume(newVal.doubleValue());
        });

        Button backBtn = new Button("Wróć");
        backBtn.getStyleClass().add("btn-stats");
        backBtn.setMinWidth(250);
        backBtn.setOnAction(e -> onBack.run());

        getChildren().addAll(title, volLabel, volSlider, backBtn);
    }
}