package com.runofashes.ui;

import com.runofashes.model.RunStatistics;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

public class StatsScreen extends VBox {

    public StatsScreen(List<RunStatistics> history, Runnable onBackToMenu) {
        setStyle("-fx-background-color: #05050a;");
        setAlignment(Pos.CENTER);
        setSpacing(25);
        setPadding(new Insets(40));

        Label title = new Label("Archiwum Biegów");
        title.setFont(Font.font("Palatino Linotype", 36));
        title.setTextFill(Color.web("#d1c8bd"));

        TableView<RunStatistics> table = new TableView<>();
        table.setPrefHeight(500);
        table.setMaxWidth(900);

        table.setStyle("-fx-base: #111; -fx-control-inner-background: #111; -fx-background-color: #05050a; -fx-table-cell-border-color: #333; -fx-text-fill: white;");

        TableColumn<RunStatistics, String> dateCol = new TableColumn<>("Data");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(140);

        TableColumn<RunStatistics, String> diffCol = new TableColumn<>("Trudność");
        diffCol.setCellValueFactory(new PropertyValueFactory<>("difficulty"));

        TableColumn<RunStatistics, String> resultCol = new TableColumn<>("Wynik");
        resultCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isWin() ? "Przeżycie" : "Śmierć"));

        TableColumn<RunStatistics, String> causeCol = new TableColumn<>("Powód");
        causeCol.setCellValueFactory(new PropertyValueFactory<>("causeOfDeath"));

        TableColumn<RunStatistics, String> distCol = new TableColumn<>("Dystans");
        distCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDistanceTraveled() + " km"));

        TableColumn<RunStatistics, String> progCol = new TableColumn<>("Postęp");
        progCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCompletionPercentage() + "%"));

        TableColumn<RunStatistics, String> daysCol = new TableColumn<>("Dni");
        daysCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getDaysSurvived())));

        TableColumn<RunStatistics, String> questsCol = new TableColumn<>("Questy");
        questsCol.setCellValueFactory(cellData -> {
            int total = cellData.getValue().getGeneralQuestsCompleted() + cellData.getValue().getLocalQuestsCompleted();
            return new SimpleStringProperty(String.valueOf(total));
        });

        TableColumn<RunStatistics, String> scoreCol = new TableColumn<>("Punkty");
        scoreCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().calculateScore())));

        table.getColumns().addAll(dateCol, diffCol, resultCol, causeCol, distCol, progCol, daysCol, questsCol, scoreCol);

        for (int i = history.size() - 1; i >= 0; i--) {
            table.getItems().add(history.get(i));
        }

        Button backBtn = new Button("Powrót do menu");
        backBtn.setStyle("-fx-font-size: 16px; -fx-padding: 10 30; -fx-background-color: #2a2a35; -fx-text-fill: #d1c8bd; -fx-border-color: #d1c8bd; -fx-border-radius: 5; -fx-background-radius: 5;");
        backBtn.setPrefWidth(200);
        backBtn.setOnMouseClicked(e -> onBackToMenu.run());

        getChildren().addAll(title, table, backBtn);
    }
}