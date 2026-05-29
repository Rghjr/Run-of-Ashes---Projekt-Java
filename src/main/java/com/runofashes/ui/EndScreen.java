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
        title.setTextFill(Color.web("#e74c3c"));

        endTextLabel.setFont(Font.font("Georgia", 17));
        endTextLabel.setTextFill(Color.web("#ddd"));
        endTextLabel.setWrapText(true);
        endTextLabel.setMaxWidth(480);
        endTextLabel.setTextAlignment(TextAlignment.CENTER);

        Button restart = new Button("▶  Zagraj ponownie");
        Button quit    = new Button("✕  Wyjdź");
        styleBtn(restart, "#27ae60");
        styleBtn(quit,    "#c0392b");
        restart.setOnAction(e -> onRestart.run());
        quit.setOnAction(e    -> onQuit.run());

        HBox btns = new HBox(24, restart, quit);
        btns.setAlignment(Pos.CENTER);

        setAlignment(Pos.CENTER);
        setSpacing(32);
        setPadding(new Insets(90, 48, 48, 48));
        setStyle("-fx-background-color: #0d0d1a;");
        getChildren().addAll(title, endTextLabel, btns);
    }

    public void setEndingText(String text) {
        endTextLabel.setText(text);
    }

    private static void styleBtn(Button btn, String color) {
        btn.setStyle("""
            -fx-background-color: %s; -fx-text-fill: white;
            -fx-font-size: 16px; -fx-padding: 12 32;
            -fx-background-radius: 6; -fx-cursor: hand;
        """.formatted(color));
    }
}
