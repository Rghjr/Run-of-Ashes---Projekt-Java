package com.runofashes.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class EndScreen extends VBox {

    private final Label endTextLabel = new Label();

    public EndScreen(Runnable onRestart, Runnable onQuit) {
        Label title = new Label("KONIEC GRY");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        title.getStyleClass().add("end-title");

        endTextLabel.setFont(Font.font("Georgia", 17));
        endTextLabel.getStyleClass().add("end-text");
        endTextLabel.setWrapText(true);
        endTextLabel.setMaxWidth(480);
        endTextLabel.setTextAlignment(TextAlignment.CENTER);

        Button restart = new Button("▶  Zagraj ponownie");
        Button quit    = new Button("✕  Wyjdź");
        restart.getStyleClass().add("btn-success");
        quit.getStyleClass().add("btn-danger");
        restart.setOnAction(e -> onRestart.run());
        quit.setOnAction(e    -> onQuit.run());

        HBox btns = new HBox(24, restart, quit);
        btns.setAlignment(Pos.CENTER);

        setAlignment(Pos.CENTER);
        setSpacing(32);
        setPadding(new Insets(90, 48, 48, 48));
        getStyleClass().add("end-screen");
        getChildren().addAll(title, endTextLabel, btns);
    }

    public void setEndingText(String text) {
        endTextLabel.setText(text);
    }
}
