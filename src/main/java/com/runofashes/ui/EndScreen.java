package com.runofashes.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.kordamp.ikonli.javafx.FontIcon;

public class EndScreen extends VBox {

    private final Label endTextLabel = new Label();

    public EndScreen(Runnable onRestart, Runnable onShowStats, Runnable onQuit) {
        FontIcon deathIcon = new FontIcon("fas-skull");
        deathIcon.setIconSize(64);
        deathIcon.getStyleClass().add("end-icon");

        Label title = new Label("KONIEC GRY");
        title.getStyleClass().add("end-title");

        endTextLabel.getStyleClass().add("end-text");
        endTextLabel.setWrapText(true);
        endTextLabel.setTextAlignment(TextAlignment.CENTER);

        VBox textBox = new VBox(20, deathIcon, title, endTextLabel);
        textBox.setAlignment(Pos.CENTER);
        textBox.setMaxWidth(550);
        textBox.getStyleClass().add("end-box");

        Button restart = new Button("▶  Zagraj ponownie");
        Button stats   = new Button("📊  Statystyki");
        Button quit    = new Button("✕  Wyjdź");

        restart.getStyleClass().add("btn-success");
        stats.getStyleClass().add("btn-stats");
        quit.getStyleClass().add("btn-danger");

        restart.setOnAction(e -> onRestart.run());
        stats.setOnAction(e -> onShowStats.run());
        quit.setOnAction(e    -> onQuit.run());

        HBox btns = new HBox(24, restart, stats, quit);
        btns.setAlignment(Pos.CENTER);

        Image img = com.runofashes.utils.FileLoader.loadUiImage("end_bg.png");
        if (img == null) {
            img = com.runofashes.utils.FileLoader.loadUiImage("event_default.png");
        }
        this.setBackground(com.runofashes.utils.FileLoader.createFadedBackground(img, "#1a1a2e"));

        setAlignment(Pos.CENTER);
        setSpacing(32);
        setPadding(new Insets(90, 48, 48, 48));
        getStyleClass().add("end-screen");
        getChildren().addAll(textBox, btns);
    }

    public void setEndingText(String text) {
        endTextLabel.setText(text);
    }
}