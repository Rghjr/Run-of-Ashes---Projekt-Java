package com.runofashes.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class WinScreen extends VBox {

    public WinScreen(Runnable onRestart, Runnable onQuit) {
        Label title = new Label("KRAKÓW");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 44));
        title.getStyleClass().add("win-title");

        Label sub = new Label("Dotarłeś.");
        sub.setFont(Font.font("Georgia", FontPosture.ITALIC, 22));
        sub.getStyleClass().add("win-sub");

        Label detail = new Label("4000 kilometrów. Koniec drogi.");
        detail.setFont(Font.font("Georgia", 16));
        detail.getStyleClass().add("win-detail");

        Button restart = new Button("▶  Zagraj ponownie");
        Button quit    = new Button("✕  Wyjdź");
        restart.getStyleClass().add("btn-success");
        quit.getStyleClass().add("btn-neutral");
        restart.setOnAction(e -> onRestart.run());
        quit.setOnAction(e    -> onQuit.run());

        HBox btns = new HBox(24, restart, quit);
        btns.setAlignment(Pos.CENTER);

        setAlignment(Pos.CENTER);
        setSpacing(28);
        setPadding(new Insets(120, 48, 48, 48));
        getStyleClass().add("end-screen");
        getChildren().addAll(title, sub, detail, btns);
    }
}
