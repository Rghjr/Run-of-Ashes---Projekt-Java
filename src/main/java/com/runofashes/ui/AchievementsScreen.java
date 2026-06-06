package com.runofashes.ui;

import com.runofashes.engine.AchievementManager;
import com.runofashes.model.Achievement;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;
import java.util.Map;

public class AchievementsScreen extends VBox {

    public AchievementsScreen(AchievementManager achievementManager, Runnable onBack) {
        setStyle("-fx-background-color: #05050a;");
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(40));

        Label title = new Label("TABLICA OSIĄGNIĘĆ");
        title.setFont(Font.font("Palatino Linotype", 36));
        title.setTextFill(Color.web("#d1c8bd"));

        Label progress = new Label("Odblokowano: " + achievementManager.getUnlockedCount() + " / " + achievementManager.getTotalCount());
        progress.setFont(Font.font(18));
        progress.setTextFill(Color.web("#888888"));

        VBox contentBox = new VBox(15);
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setPadding(new Insets(10, 40, 10, 40));
        contentBox.setStyle("-fx-background-color: transparent;");

        Map<String, List<Achievement>> grouped = achievementManager.getAchievementsByGroup();

        for (Map.Entry<String, List<Achievement>> entry : grouped.entrySet()) {
            Label groupLabel = new Label("--- " + entry.getKey().toUpperCase() + " ---");
            groupLabel.setTextFill(Color.web("#ffd700"));
            groupLabel.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 20));
            groupLabel.setPadding(new Insets(20, 0, 5, 0));
            contentBox.getChildren().add(groupLabel);

            for (Achievement a : entry.getValue()) {
                String icon = a.isUnlocked() ? "🏆" : "🔒";
                Label achLabel = new Label(icon + " " + a.getTitle() + " - " + a.getDescription());

                achLabel.setTextFill(a.isUnlocked() ? Color.web("#d1c8bd") : Color.web("#444455"));
                achLabel.setFont(Font.font(16));
                achLabel.setWrapText(true);

                contentBox.getChildren().add(achLabel);
            }
        }

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #05050a; -fx-background-color: transparent; -fx-border-color: #333;");
        scrollPane.setPrefHeight(500);
        scrollPane.setMaxWidth(800);

        Button backBtn = new Button("Powrót");
        backBtn.setStyle("-fx-font-size: 16px; -fx-padding: 10 30; -fx-background-color: #2a2a35; -fx-text-fill: #d1c8bd; -fx-background-radius: 5;");
        backBtn.setOnAction(e -> onBack.run());

        getChildren().addAll(title, progress, scrollPane, backBtn);
    }
}