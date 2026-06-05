package com.runofashes.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class MainMenuScreen extends VBox {

    public MainMenuScreen(Runnable onNewGame, Runnable onLoadGame, Runnable onSettings, Runnable onQuit) {
        setStyle("-fx-background-color: #05050a;");
        setAlignment(Pos.CENTER);
        setSpacing(20);

        Label title = new Label("RUN OF ASHES");
        title.setFont(Font.font("Palatino Linotype", 48));
        title.setStyle("-fx-text-fill: #d1c8bd; -fx-padding: 0 0 50 0;");

        Button newGameBtn = createMenuButton("Nowa gra", onNewGame);
        Button loadGameBtn = createMenuButton("Wczytaj grę", onLoadGame);
        Button settingsBtn = createMenuButton("Ustawienia", onSettings);
        Button quitBtn = createMenuButton("Wyjdź z gry", onQuit);

        getChildren().addAll(title, newGameBtn, loadGameBtn, settingsBtn, quitBtn);
    }

    private Button createMenuButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("btn-stats"); // Używamy Twojego stylu dla spójności
        btn.setMinWidth(250);
        btn.setOnAction(e -> action.run());
        return btn;
    }
}