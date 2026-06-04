package com.runofashes.ui;

import com.runofashes.engine.GameEngine;
import com.runofashes.model.Achievement;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Map;

public class AchievementPanel extends VBox {

    private final GameEngine engine;
    private final VBox listContainer = new VBox(10);
    private final Label statsLabel = new Label();
    private Runnable onCloseHandler;

    public AchievementPanel(GameEngine engine) {
        this.engine = engine;

        setStyle("-fx-background-color: #141423; -fx-padding: 20; -fx-background-radius: 8; -fx-border-color: #333; -fx-border-radius: 8;");
        setSpacing(16);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("🏆 Osiągnięcia");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        title.setStyle("-fx-text-fill: #f0c040;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: #2a2a3e; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10 4 10; -fx-background-radius: 4;");
        closeBtn.setOnAction(e -> {
            if (onCloseHandler != null) {
                onCloseHandler.run();
            }
        });

        header.getChildren().addAll(title, spacer, closeBtn);

        statsLabel.setFont(Font.font("System", 14));
        statsLabel.setStyle("-fx-text-fill: #aaa;");

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, statsLabel, scrollPane);
        refresh();
    }

    public void setOnClose(Runnable handler) {
        this.onCloseHandler = handler;
    }

    /**
     * Czyta aktualny stan z menedżera osiągnięć i buduje widok z rozwijanymi sekcjami.
     */
    public void refresh() {
        listContainer.getChildren().clear();

        var manager = engine.getAchievementManager();
        if (manager == null) {
            statsLabel.setText("Menedżer osiągnięć jest niedostępny.");
            return;
        }

        statsLabel.setText("Odblokowano: " + manager.getUnlockedCount() + " / " + manager.getTotalCount());

        Map<String, List<Achievement>> grouped = manager.getAchievementsByGroup();

        for (Map.Entry<String, List<Achievement>> entry : grouped.entrySet()) {
            String groupName = entry.getKey();
            List<Achievement> list = entry.getValue();

            long unlockedInGroup = list.stream().filter(Achievement::isUnlocked).count();

            VBox groupWrapper = new VBox(4);

            HBox groupHeader = new HBox(12);
            groupHeader.setAlignment(Pos.CENTER_LEFT);
            groupHeader.setPadding(new Insets(12, 16, 12, 16));
            groupHeader.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 6; -fx-border-color: #2a2a3a; -fx-border-radius: 6; -fx-cursor: hand;");

            Label groupTitle = new Label(groupName + " (" + unlockedInGroup + " / " + list.size() + ")");
            groupTitle.setFont(Font.font("System", FontWeight.BOLD, 15));
            groupTitle.setStyle("-fx-text-fill: #e67e22;");

            Region headerSpacer = new Region();
            HBox.setHgrow(headerSpacer, Priority.ALWAYS);

            Label arrowIndicator = new Label("▶");
            arrowIndicator.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

            groupHeader.getChildren().addAll(groupTitle, headerSpacer, arrowIndicator);

            VBox achievementsList = new VBox(6);
            achievementsList.setPadding(new Insets(4, 8, 8, 8));
            achievementsList.setVisible(false);
            achievementsList.setManaged(false);

            groupHeader.setOnMouseClicked(e -> {
                boolean isExpanded = achievementsList.isVisible();
                achievementsList.setVisible(!isExpanded);
                achievementsList.setManaged(!isExpanded);
                arrowIndicator.setText(!isExpanded ? "▼" : "▶");

                if (!isExpanded) {
                    groupHeader.setStyle("-fx-background-color: #22223c; -fx-background-radius: 6; -fx-border-color: #f0c040; -fx-border-radius: 6; -fx-cursor: hand;");
                } else {
                    groupHeader.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 6; -fx-border-color: #2a2a3a; -fx-border-radius: 6; -fx-cursor: hand;");
                }
            });

            for (Achievement a : list) {
                HBox row = new HBox(16);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8, 12, 8, 12));

                VBox textBlock = new VBox(2);
                Label titleLbl = new Label("Poz. " + a.getLevel() + ": " + a.getTitle());
                titleLbl.setFont(Font.font("System", FontWeight.BOLD, 13));

                Label descLbl = new Label(a.getDescription());
                descLbl.setFont(Font.font("System", 11));
                descLbl.setStyle("-fx-text-fill: #aaaaaa;");
                descLbl.setWrapText(true);

                textBlock.getChildren().addAll(titleLbl, descLbl);
                HBox.setHgrow(textBlock, Priority.ALWAYS);

                Region rowSpacer = new Region();
                HBox.setHgrow(rowSpacer, Priority.ALWAYS);

                Label statusIcon = new Label();
                statusIcon.setStyle("-fx-font-size: 18px;");

                if (a.isUnlocked()) {
                    row.setStyle("-fx-background-color: #24243e; -fx-background-radius: 4;");
                    titleLbl.setStyle("-fx-text-fill: #ffffff;");
                    statusIcon.setText("🏆");
                } else {
                    row.setStyle("-fx-background-color: #161626; -fx-background-radius: 4; -fx-opacity: 0.45;");
                    titleLbl.setStyle("-fx-text-fill: #888888;");
                    statusIcon.setText("🔒");
                }

                row.getChildren().addAll(textBlock, rowSpacer, statusIcon);
                achievementsList.getChildren().add(row);
            }

            groupWrapper.getChildren().addAll(groupHeader, achievementsList);
            listContainer.getChildren().add(groupWrapper);
        }
    }
}