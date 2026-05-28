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
        title.setTextFill(Color.web("#f0c040"));

        Label sub = new Label("Dotarłeś.");
        sub.setFont(Font.font("Georgia", FontPosture.ITALIC, 22));
        sub.setTextFill(Color.web("#aaa"));

        Label detail = new Label("4000 kilometrów. Koniec drogi.");
        detail.setFont(Font.font("Georgia", 16));
        detail.setTextFill(Color.web("#666"));

        Button restart = new Button("▶  Zagraj ponownie");
        Button quit    = new Button("✕  Wyjdź");
        styleBtn(restart, "#27ae60");
        styleBtn(quit,    "#555");
        restart.setOnAction(e -> onRestart.run());
        quit.setOnAction(e    -> onQuit.run());

        HBox btns = new HBox(24, restart, quit);
        btns.setAlignment(Pos.CENTER);

        setAlignment(Pos.CENTER);
        setSpacing(28);
        setPadding(new Insets(120, 48, 48, 48));
        setStyle("-fx-background-color: #0d0d1a;");
        getChildren().addAll(title, sub, detail, btns);
    }

    private static void styleBtn(Button btn, String color) {
        btn.setStyle("""
            -fx-background-color: %s; -fx-text-fill: white;
            -fx-font-size: 16px; -fx-padding: 12 32;
            -fx-background-radius: 6; -fx-cursor: hand;
        """.formatted(color));
    }
}
