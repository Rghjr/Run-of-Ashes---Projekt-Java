package com.runofashes.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.File;
import java.util.function.Consumer;

public class LoadGameScreen extends VBox {

    public LoadGameScreen(Consumer<String> onLoadSelect, Runnable onBack) {
        setStyle("-fx-background-color: #05050a;");
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(40));

        Label title = new Label("WCZYTAJ GRĘ");
        title.setFont(Font.font("Palatino Linotype", 48));
        title.setStyle("-fx-text-fill: #d1c8bd; -fx-padding: 0 0 30 0;");

        VBox listContainer = new VBox(10);
        listContainer.setAlignment(Pos.CENTER);

        File currentDir = new File(".");
        File[] files = currentDir.listFiles((dir, name) -> name.startsWith("save") && name.endsWith(".json"));

        if (files != null && files.length > 0) {
            for (File file : files) {
                Button saveBtn = new Button(file.getName());
                saveBtn.getStyleClass().add("btn-stats");
                saveBtn.setMinWidth(300);
                saveBtn.setOnAction(e -> onLoadSelect.accept(file.getName()));
                listContainer.getChildren().add(saveBtn);
            }
        } else {
            Label noSaves = new Label("Brak zapisanych gier.");
            noSaves.setStyle("-fx-text-fill: #888888; -fx-font-size: 18px;");
            listContainer.getChildren().add(noSaves);
        }

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.getStyleClass().add("transparent-scroll-pane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setMaxWidth(400);
        scrollPane.setMaxHeight(400);

        Button backBtn = new Button("Wróć");
        backBtn.getStyleClass().add("btn-stats");
        backBtn.setMinWidth(200);
        backBtn.setOnAction(e -> onBack.run());

        getChildren().addAll(title, scrollPane, backBtn);
    }
}