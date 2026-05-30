package com.runofashes.ui;

import com.runofashes.engine.GameEngine;
import com.runofashes.model.GameEvent;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.util.Duration;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;

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
        if (!messageLabel.getText().equals(engine.getLastMessage())) {
            messageLabel.setText(engine.getLastMessage());

            FadeTransition ft = new FadeTransition(Duration.millis(600), messageLabel);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        }

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
        invScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        ScrollPane questScroll = new ScrollPane(questPanel);
        questScroll.setFitToWidth(true);
        questScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        questScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        StackPane rightContent = new StackPane(questScroll, invScroll);
        VBox.setVgrow(rightContent, Priority.ALWAYS);
        invScroll.setVisible(true);
        questScroll.setVisible(false);

        FontIcon bagIcon = new FontIcon(MaterialDesignB.BAG_PERSONAL);
        bagIcon.setIconSize(18);

        FontIcon questIcon = new FontIcon(MaterialDesignS.SCRIPT_TEXT);
        questIcon.setIconSize(18);

        Button btnInv   = new Button("Ekwipunek");
        btnInv.setGraphic(bagIcon);

        Button btnQuest = new Button("Questy");
        btnQuest.setGraphic(questIcon);

        btnInv.getStyleClass().add("tab-button-active");
        btnQuest.getStyleClass().add("tab-button-inactive");

        btnInv.setOnAction(e -> {
            invScroll.setVisible(true);
            questScroll.setVisible(false);
            btnInv.getStyleClass().setAll("button", "tab-button-active");
            btnQuest.getStyleClass().setAll("button", "tab-button-inactive");
        });
        btnQuest.setOnAction(e -> {
            questScroll.setVisible(true);
            invScroll.setVisible(false);
            btnQuest.getStyleClass().setAll("button", "tab-button-active");
            btnInv.getStyleClass().setAll("button", "tab-button-inactive");
        });

        HBox tabButtons = new HBox(4, btnInv, btnQuest);
        VBox rightSide = new VBox(8, biomePanel, tabButtons, rightContent, biomePanel.getStatusesBox());

        root = new HBox(12, gameContentVBox, rightSide);
        root.setPadding(new Insets(18));

        root.getStyleClass().add("root-pane");

        gameContentVBox.prefWidthProperty().bind(root.widthProperty().multiply(0.7).subtract(24));
        rightSide.prefWidthProperty().bind(root.widthProperty().multiply(0.3).subtract(24));
    }
}
