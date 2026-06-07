package com.runofashes.ui;

import com.runofashes.engine.GameEngine;
import com.runofashes.model.EventChoice;
import com.runofashes.model.GameEvent;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.geometry.Pos;

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
    private final Runnable onMainMenu;
    private final Runnable onQuit;
    private final Runnable onSettings;

    private StackPane root;

    public GameScreen(GameEngine engine, Runnable refreshCallback, Consumer<GameEvent> onCardClick, Runnable onMainMenu,Runnable onSettings, Runnable onQuit) {
        this.engine      = engine;
        this.onCardClick = onCardClick;
        this.onMainMenu  = onMainMenu;
        this.onQuit      = onQuit;
        this.onSettings  = onSettings;

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

        String msgClass = switch (engine.getLastResult()) {
            case SUCCESS -> "msg-success";
            case PARTIAL -> "msg-partial";
            case FAIL    -> "msg-fail";
        };
        messageLabel.getStyleClass().removeAll("msg-success", "msg-partial", "msg-fail");
        messageLabel.getStyleClass().add(msgClass);

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

    public void setLastEvent(GameEvent event) {
        hud.setEventImage(event);
    }

    /**
     * Wyświetla nakładkę z opcjami decyzyjnymi dla wydarzenia typu "wybór".
     * Po wyborze opcji nakładka znika i wywoływany jest {@code onPick}.
     */
    public void showChoiceOverlay(GameEvent event, Consumer<EventChoice> onPick) {
        VBox overlay = new VBox(22);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPadding(new Insets(40));
        overlay.setStyle("-fx-background-color: rgba(5, 5, 10, 0.93);");

        boolean depressed = engine.getPlayer().getMorale() < 30;
        String titleText = (depressed && event.getLowMoraleLabel() != null)
                ? event.getLowMoraleLabel()
                : event.getLabel();

        Label title = new Label(titleText);
        title.setWrapText(true);
        title.setMaxWidth(640);
        title.setStyle("-fx-text-fill: #f0c040; -fx-font-size: 26px; -fx-font-family: 'Palatino Linotype'; "
                + "-fx-font-weight: bold; -fx-text-alignment: center;");

        Label hint = new Label("Wybierz, co robisz:");
        hint.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 16px;");

        VBox optionsBox = new VBox(12);
        optionsBox.setAlignment(Pos.CENTER);
        optionsBox.setMaxWidth(640);

        for (EventChoice choice : event.getChoices()) {
            int pct = (int) Math.round(engine.getChoiceChance(choice) * 100);
            Button btn = new Button(choice.getLabel() + "\nSzansa powodzenia: " + pct + "%");
            btn.setWrapText(true);
            btn.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setMinHeight(64);

            String chanceColor = pct >= 66 ? "#3ba55d" : (pct >= 33 ? "#d8a657" : "#c0563f");
            btn.setStyle("-fx-background-color: #1f1f33; -fx-text-fill: #f0f0f0; -fx-font-size: 16px; "
                    + "-fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 12 18; "
                    + "-fx-border-color: " + chanceColor + "; -fx-border-width: 2; -fx-border-radius: 8;");

            btn.setOnAction(e -> {
                root.getChildren().remove(overlay);
                onPick.accept(choice);
            });
            optionsBox.getChildren().add(btn);
        }

        overlay.getChildren().addAll(title, hint, optionsBox);
        root.getChildren().add(overlay);
        overlay.toFront();
    }

    private void build() {
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(640);
        messageLabel.setMinHeight(Region.USE_PREF_SIZE);
        messageLabel.getStyleClass().addAll("message-label", "msg-partial");

        ScrollPane messageScroll = new ScrollPane(messageLabel);
        messageScroll.setFitToWidth(true);
        messageScroll.setMinHeight(90);
        messageScroll.setPrefHeight(130);
        messageScroll.setMaxHeight(240);
        messageScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messageScroll.getStyleClass().add("transparent-scroll-pane");

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

        VBox.setVgrow(grid, Priority.ALWAYS);

        VBox gameContentVBox = new VBox(16, hud, messageScroll, grid);
        HBox.setHgrow(gameContentVBox, Priority.ALWAYS);

        ScrollPane invScroll = new ScrollPane(inventoryPanel);
        invScroll.setFitToWidth(true);
        invScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        invScroll.getStyleClass().add("transparent-scroll-pane");

        ScrollPane questScroll = new ScrollPane(questPanel);
        questScroll.setFitToWidth(true);
        questScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        questScroll.getStyleClass().add("transparent-scroll-pane");

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
        btnInv.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnInv, Priority.ALWAYS);

        Button btnQuest = new Button("Questy");
        btnQuest.setGraphic(questIcon);
        btnQuest.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnQuest, Priority.ALWAYS);

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

        GridPane mainLayout = new GridPane();
        mainLayout.setHgap(12);
        mainLayout.setPadding(new Insets(18));
        mainLayout.getStyleClass().add("root-pane");

        GridPane.setHgrow(gameContentVBox, Priority.ALWAYS);
        GridPane.setVgrow(gameContentVBox, Priority.ALWAYS);
        GridPane.setHgrow(rightSide, Priority.ALWAYS);
        GridPane.setVgrow(rightSide, Priority.ALWAYS);

        mainLayout.add(gameContentVBox, 0, 0);
        mainLayout.add(rightSide, 1, 0);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(70);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(30);
        mainLayout.getColumnConstraints().addAll(col1, col2);

        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS);
        mainLayout.getRowConstraints().add(row);

        Button menuBtn = new Button("☰");
        menuBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f0c040; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 2 12 2 16;");

        HBox topMenuBar = new HBox(menuBtn);
        topMenuBar.setAlignment(Pos.CENTER_LEFT);
        topMenuBar.setStyle("-fx-background-color: #111122; -fx-border-color: #2a2a3e; -fx-border-width: 0 0 1 0;");

        VBox windowContent = new VBox(topMenuBar, mainLayout);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        root = new StackPane();
        root.getChildren().add(windowContent);

        VBox dropdownMenu = new VBox(8);
        dropdownMenu.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #444; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 5);");
        dropdownMenu.setMaxWidth(200);
        dropdownMenu.setMaxHeight(VBox.USE_PREF_SIZE);
        dropdownMenu.setVisible(false);

        Button btnMainMenu = new Button("Menu główne");
        Button btnSave = new Button("Zapisz grę");
        Button btnSettings = new Button("Ustawienia");
        Button btnAchievements = new Button("Osiągnięcia");
        Button btnQuit = new Button("Wyjdź z gry");

        String menuBtnStyle = "-fx-background-color: #2a2a3e; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 160px; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 10;";
        btnMainMenu.setStyle(menuBtnStyle);
        btnSave.setStyle(menuBtnStyle);
        btnAchievements.setStyle(menuBtnStyle);
        btnSettings.setStyle(menuBtnStyle);
        btnQuit.setStyle(menuBtnStyle);

        dropdownMenu.getChildren().addAll(btnMainMenu, btnSave, btnSettings, btnAchievements, btnQuit);

        StackPane.setAlignment(dropdownMenu, Pos.TOP_LEFT);
        StackPane.setMargin(dropdownMenu, new Insets(38, 0, 0, 12));

        btnSave.setOnAction(e -> {
            engine.saveGame();
            dropdownMenu.setVisible(false);

            btnSave.setText("Zapisano!");
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(2));
            pause.setOnFinished(ev -> btnSave.setText("Zapisz grę"));
            pause.play();
        });

        btnMainMenu.setOnAction(e -> {
            dropdownMenu.setVisible(false);
            onMainMenu.run();
        });

        btnSettings.setOnAction(e -> {
            dropdownMenu.setVisible(false);
            onSettings.run();
        });

        btnQuit.setOnAction(e -> {
            onQuit.run();
        });

        // Panel osiągnięć
        AchievementPanel achievementPanel = new AchievementPanel(engine);
        achievementPanel.setMaxWidth(650);
        achievementPanel.setMaxHeight(600);
        achievementPanel.setVisible(false);
        StackPane.setAlignment(achievementPanel, Pos.CENTER);

        achievementPanel.setOnClose(() -> achievementPanel.setVisible(false));

        btnAchievements.setOnAction(e -> {
            dropdownMenu.setVisible(false);
            achievementPanel.setVisible(true);
            achievementPanel.refresh();
            achievementPanel.toFront();
        });

        menuBtn.setOnAction(e -> {
            dropdownMenu.setVisible(!dropdownMenu.isVisible());
            if (dropdownMenu.isVisible()) {
                dropdownMenu.toFront();
            }
        });

        root.getChildren().addAll(dropdownMenu, achievementPanel);
    }
}
