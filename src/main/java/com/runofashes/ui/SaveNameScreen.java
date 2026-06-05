package com.runofashes.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.function.Consumer;

public class SaveNameScreen extends VBox {

    public SaveNameScreen(Consumer<String> onNext, Runnable onBack) {
        setStyle("-fx-background-color: #05050a;");
        setAlignment(Pos.CENTER);
        setSpacing(20);

        Label title = new Label("NOWA WYPRAWA");
        title.setFont(Font.font("Palatino Linotype", 48));
        title.setStyle("-fx-text-fill: #d1c8bd; -fx-padding: 0 0 30 0;");

        Label hint = new Label("Podaj nazwę dla tego zapisu (np. imię bohatera):");
        hint.setStyle("-fx-text-fill: #888888; -fx-font-size: 18px;");

        TextField nameInput = new TextField();
        nameInput.setMaxWidth(300);
        nameInput.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: white; -fx-font-size: 18px; -fx-border-color: #444; -fx-border-radius: 4; -fx-padding: 10;");

        Button nextBtn = new Button("Dalej");
        nextBtn.getStyleClass().add("btn-stats");
        nextBtn.setMinWidth(250);

        Button backBtn = new Button("Wróć");
        backBtn.getStyleClass().add("btn-stats");
        backBtn.setMinWidth(250);

        nextBtn.setOnAction(e -> {
            String input = nameInput.getText().trim();
            if (input.isEmpty()) {
                input = "Nieznany_Wedrowiec";
            }

            String safeName = input.replaceAll("[^a-zA-Z0-9ąćęłńóśźżĄĆĘŁŃÓŚŹŻ _-]", "").replace(" ", "_");
            String filename = safeName + ".json";

            onNext.accept(filename);
        });

        backBtn.setOnAction(e -> onBack.run());

        getChildren().addAll(title, hint, nameInput, nextBtn, backBtn);
    }
}