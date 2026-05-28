package com.runofashes.ui;

import com.runofashes.engine.EventResult;
import com.runofashes.engine.GameEngine;
import com.runofashes.model.GameEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GameScreen {

    private final GameEngine engine;
    private final Consumer<GameEvent> onCardClick;

    private final GameHUD hud;
    private final Label messageLabel = new Label(" ");
    private final List<VBox> cardSlots = new ArrayList<>();
    private final InventoryPanel inventoryPanel;
    private final QuestPanel questPanel;
    private final BiomePanel biomePanel;

    private HBox root;

    public GameScreen(GameEngine engine, Runnable refreshCallback, Consumer<GameEvent> onCardClick) {
        this.engine      = engine;
        this.onCardClick = onCardClick;
        this.hud             = new GameHUD(engine);
        this.inventoryPanel  = new InventoryPanel(engine, refreshCallback);
        this.questPanel      = new QuestPanel(engine);
        this.biomePanel      = new BiomePanel();
        build();
    }

    public Pane getRoot() {
        return root;
    }

    public void refresh() {
        hud.refresh();
        inventoryPanel.refresh();
        questPanel.refresh();

        String msgColor = switch (engine.getLastResult()) {
            case SUCCESS -> "#7ec8a0";
            case PARTIAL -> "#f0c040";
            case FAIL    -> "#e74c3c";
        };
        messageLabel.setStyle("-fx-text-fill: " + msgColor + "; -fx-font-style: italic; -fx-font-size: 15px;");
        messageLabel.setText(engine.getLastMessage());

        List<GameEvent> cards = engine.getCurrentCards();
        for (int i = 0; i < 4; i++) {
            if (i < cards.size()) {
                EventCardView.fill(cardSlots.get(i), cards.get(i), engine, onCardClick);
            } else {
                EventCardView.fillEmpty(cardSlots.get(i));
            }
        }

        biomePanel.refresh(engine, messageLabel);
    }

    private void build() {
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(640);
        messageLabel.setMinHeight(70);
        messageLabel.setPrefHeight(70);
        messageLabel.setStyle("-fx-text-fill: #f0c040; -fx-font-style: italic; -fx-font-size: 15px;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        for (int i = 0; i < 4; i++) {
            VBox card = new VBox(10);
            card.setPadding(new Insets(16));
            card.setMinHeight(148);
            cardSlots.add(card);
            grid.add(card, i % 2, i / 2);
        }

        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(50);
            grid.getColumnConstraints().add(cc);
        }

        VBox gameContentVBox = new VBox(16, hud, messageLabel, grid);
        HBox.setHgrow(gameContentVBox, Priority.ALWAYS);

        ScrollPane invScroll = new ScrollPane(inventoryPanel);
        invScroll.setFitToWidth(true);
        invScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        invScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        invScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-viewport-background-color: transparent; -fx-border-color: transparent;");

        ScrollPane questScroll = new ScrollPane(questPanel);
        questScroll.setFitToWidth(true);
        questScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        questScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        questScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-viewport-background-color: transparent; -fx-border-color: transparent;");

        StackPane rightContent = new StackPane(questScroll, invScroll);
        VBox.setVgrow(rightContent, Priority.ALWAYS);
        invScroll.setVisible(true);
        questScroll.setVisible(false);

        Button btnInv   = new Button("🎒 Ekwipunek");
        Button btnQuest = new Button("📜 Questy");
        btnInv.setPrefWidth(128);
        btnQuest.setPrefWidth(128);

        String btnActive   = "-fx-background-color: #2a2a3a; -fx-text-fill: #f0c040; -fx-cursor: hand; -fx-font-size: 13px; -fx-background-radius: 6;";
        String btnInactive = "-fx-background-color: #111122; -fx-text-fill: #888; -fx-cursor: hand; -fx-font-size: 13px; -fx-background-radius: 6;";
        btnInv.setStyle(btnActive);
        btnQuest.setStyle(btnInactive);

        btnInv.setOnAction(e -> {
            invScroll.setVisible(true);
            questScroll.setVisible(false);
            btnInv.setStyle(btnActive);
            btnQuest.setStyle(btnInactive);
        });
        btnQuest.setOnAction(e -> {
            questScroll.setVisible(true);
            invScroll.setVisible(false);
            btnQuest.setStyle(btnActive);
            btnInv.setStyle(btnInactive);
        });

        HBox tabButtons = new HBox(4, btnInv, btnQuest);
        VBox rightSide = new VBox(8, biomePanel, tabButtons, rightContent, biomePanel.getStatusesBox());

        root = new HBox(12, gameContentVBox, rightSide);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: #0d0d1a;");

        gameContentVBox.prefWidthProperty().bind(root.widthProperty().multiply(0.7).subtract(24));
        rightSide.prefWidthProperty().bind(root.widthProperty().multiply(0.3).subtract(24));
    }
}
