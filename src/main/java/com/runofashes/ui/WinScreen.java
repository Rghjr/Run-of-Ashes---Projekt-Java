package com.runofashes.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class WinScreen extends VBox {

    public WinScreen(Runnable onRestart, Runnable onQuit) {
        FontIcon winIcon = new FontIcon("fas-trophy");
        winIcon.setIconSize(72);
        winIcon.getStyleClass().add("win-icon");

        Label title = new Label("KRAKÓW");
        title.getStyleClass().add("win-title");

        Label sub = new Label("Dotarłeś.");
        sub.getStyleClass().add("win-sub");

        Label detail = new Label("4000 kilometrów. Koniec drogi.");
        detail.getStyleClass().add("win-detail");

        VBox textBox = new VBox(12, winIcon, title, sub, detail);
        textBox.setAlignment(Pos.CENTER);
        textBox.setMaxWidth(550);
        textBox.getStyleClass().add("win-box");

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
