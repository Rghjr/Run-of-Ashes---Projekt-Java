package com.runofashes.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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

        VBox listContainer = new VBox(15);
        listContainer.setAlignment(Pos.CENTER);

        File savesDir = new File("saves");
        File[] files = null;
        if (savesDir.exists() && savesDir.isDirectory()) {
            files = savesDir.listFiles((dir, name) -> name.endsWith(".json") && !name.equals("settings.json"));
        }

        if (files != null && files.length > 0) {
            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm");

            for (File file : files) {
                String actualFilename = file.getName();
                String displayName = actualFilename.replace(".json", "");
                String dateStr = sdf.format(new Date(file.lastModified()));

                HBox saveCard = new HBox(15);
                saveCard.setAlignment(Pos.CENTER_LEFT);
                saveCard.setStyle("-fx-background-color: #111122; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #2a2a3e; -fx-border-radius: 8;");
                saveCard.setMaxWidth(480);

                Button loadBtn = new Button(displayName);
                loadBtn.getStyleClass().add("btn-stats");
                loadBtn.setMinWidth(220);
                loadBtn.setOnAction(e -> onLoadSelect.accept(actualFilename));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                VBox rightSection = new VBox(8);
                rightSection.setAlignment(Pos.CENTER_RIGHT);

                Label dateLbl = new Label(dateStr);
                dateLbl.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 14px;");

                Button deleteBtn = new Button("Usuń zapis");
                deleteBtn.getStyleClass().add("btn-danger");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 12;"); // Pomniejszony przycisk

                deleteBtn.setOnAction(e -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Potwierdzenie usunięcia");
                    alert.setHeaderText(null);
                    alert.setContentText("Czy na pewno chcesz bezpowrotnie usunąć zapis: " + displayName + "?");

                    Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
                    if (okButton != null) okButton.setText("Tak, usuń");

                    Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
                    if (cancelButton != null) cancelButton.setText("Anuluj");

                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            file.delete();
                            listContainer.getChildren().remove(saveCard);

                            if (listContainer.getChildren().isEmpty()) {
                                Label noSaves = new Label("Brak zapisanych gier.");
                                noSaves.setStyle("-fx-text-fill: #888888; -fx-font-size: 18px;");
                                listContainer.getChildren().add(noSaves);
                            }
                        }
                    });
                });

                rightSection.getChildren().addAll(dateLbl, deleteBtn);
                saveCard.getChildren().addAll(loadBtn, spacer, rightSection);
                listContainer.getChildren().add(saveCard);
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
        scrollPane.setMaxWidth(530);
        scrollPane.setMaxHeight(450);

        Button backBtn = new Button("Wróć");
        backBtn.getStyleClass().add("btn-stats");
        backBtn.setMinWidth(200);
        backBtn.setOnAction(e -> onBack.run());

        getChildren().addAll(title, scrollPane, backBtn);
    }
}