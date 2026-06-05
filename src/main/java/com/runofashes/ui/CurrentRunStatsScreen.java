package com.runofashes.ui;

import com.runofashes.model.RunStatistics;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CurrentRunStatsScreen extends VBox {

    public CurrentRunStatsScreen(RunStatistics stats, Runnable onShowAchievements, Runnable onRestart, Runnable onQuit) {
        setStyle("-fx-background-color: #05050a;");
        setAlignment(Pos.CENTER);
        setSpacing(40);
        setPadding(new Insets(40));

        Label title = new Label("PODSUMOWANIE BIEGU");
        title.setFont(Font.font("Palatino Linotype", 36));
        title.setTextFill(Color.web("#d1c8bd"));

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(30);
        grid.setVgap(15);

        int row = 0;
        addRow(grid, row++, "Wynik gry:", stats.isWin() ? "Przeżycie" : "Śmierć (" + stats.getCauseOfDeath() + ")");
        addRow(grid, row++, "Poziom trudności:", stats.getDifficulty());
        addRow(grid, row++, "Przebyty dystans:", stats.getDistanceTraveled() + " km (" + stats.getCompletionPercentage() + "% trasy)");
        addRow(grid, row++, "Przetrwane dni:", String.valueOf(stats.getDaysSurvived()));
        addRow(grid, row++, "Wykonane questy:", stats.getLocalQuestsCompleted() + " (lokalne) / " + stats.getGeneralQuestsCompleted() + " (główne)");
        addRow(grid, row++, "Zużyte przedmioty:", String.valueOf(stats.getItemsUsed()));

        Label scoreLabel = new Label("PUNKTY (SCORE):");
        scoreLabel.setTextFill(Color.web("#d1c8bd"));
        scoreLabel.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 22));

        Label scoreValue = new Label(String.valueOf(stats.calculateScore()));
        scoreValue.setTextFill(Color.web("#ffd700"));
        scoreValue.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 22));
        grid.add(scoreLabel, 0, row);
        grid.add(scoreValue, 1, row);

        Button achBtn  = new Button("🏆 Osiągnięcia");
        Button backBtn = new Button("▶ Zagraj ponownie");
        Button quitBtn = new Button("✕ Wyjdź z gry");

        achBtn.getStyleClass().add("btn-stats");
        backBtn.getStyleClass().add("btn-success");
        quitBtn.getStyleClass().add("btn-danger");

        achBtn.setOnAction(e -> onShowAchievements.run());
        backBtn.setOnAction(e -> onRestart.run());
        quitBtn.setOnAction(e -> onQuit.run());

        HBox btns = new HBox(30, achBtn, backBtn, quitBtn);
        btns.setAlignment(Pos.CENTER);

        getChildren().addAll(title, grid, btns);
    }

    private void addRow(GridPane grid, int row, String labelText, String valueText) {
        Label lbl1 = new Label(labelText);
        lbl1.setTextFill(Color.web("#888888"));
        lbl1.setFont(Font.font(18));

        Label lbl2 = new Label(valueText);
        lbl2.setTextFill(Color.web("#d1c8bd"));
        lbl2.setFont(Font.font(18));

        grid.add(lbl1, 0, row);
        grid.add(lbl2, 1, row);
    }
}