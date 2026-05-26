package com.runofashes.ui;

import com.runofashes.engine.GameEngine;
import com.runofashes.model.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main extends Application {

    private final GameEngine engine = new GameEngine();
    private Stage primaryStage;
    private Scene mainScene;

    // Ekrany pre-game
    private DifficultyScreen    difficultyScreen;
    private TraitSelectionScreen traitScreen;

    // Ekrany gry
    private Pane gameRoot;
    private VBox endRoot, winRoot;
    private Label endTextLabel;

    // HUD
    private GameHUD hud;
    private QuestPanel questPanel;
    private InventoryPanel inventoryPanel;
    private VBox activeStatusesBox;

    private VBox biomeInfoPanel;
    private Label biomeTitleLabel;
    private Label biomeDescLabel;
    private Label biomeEffectsLabel;

    private Label messageLabel;
    private final List<VBox> cardSlots = new ArrayList<>();

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        engine.load();

        gameRoot = buildGameScreen();
        endRoot  = buildEndScreen();
        winRoot  = buildWinScreen();

        difficultyScreen = new DifficultyScreen(this::onDifficultyConfirmed);
        mainScene = new Scene(difficultyScreen, 980, 860);

        stage.setTitle("Run of Ashes");

        stage.setMinWidth(980);
        stage.setMinHeight(750);

        stage.setScene(mainScene);
        stage.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Przepływ ekranów pre-game
    // ══════════════════════════════════════════════════════════════════════════

    // Wywoływane przy restarcie (EndScreen/WinScreen) — mainScene juz istnieje
    private void showDifficultyScreen() {
        difficultyScreen = new DifficultyScreen(this::onDifficultyConfirmed);
        mainScene.setRoot(difficultyScreen);
    }

    private void onDifficultyConfirmed() {
        Difficulty diff = difficultyScreen.getSelected();
        if (diff == null) return;

        traitScreen = new TraitSelectionScreen(diff, () -> onTraitsConfirmed(diff), this::showDifficultyScreen);
        mainScene.setRoot(traitScreen);
        primaryStage.setWidth(980);
    }

    private void onTraitsConfirmed(Difficulty diff) {
        Set<Trait> traits = traitScreen.getSelected();
        engine.configure(diff, traits);
        engine.reset();
        refreshAll();
        mainScene.setRoot(gameRoot);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Ekran gry
    // ══════════════════════════════════════════════════════════════════════════

    private HBox buildGameScreen() {
        hud = new GameHUD(engine);

        messageLabel = new Label(" ");
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

        inventoryPanel = new InventoryPanel(engine, this::refreshAll);
        questPanel = new QuestPanel(engine);

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

        biomeTitleLabel = new Label();
        biomeTitleLabel.setStyle("-fx-text-fill: #f0c040; -fx-font-size: 14px; -fx-font-weight: bold;");

        biomeDescLabel = new Label();
        biomeDescLabel.setWrapText(true);
        biomeDescLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12px; -fx-font-style: italic;");

        biomeEffectsLabel = new Label();
        biomeEffectsLabel.setWrapText(true);
        biomeEffectsLabel.setStyle("-fx-text-fill: #ccc; -fx-font-size: 13px; -fx-line-spacing: 5px;"); // line-spacing daje oddech między linijkami

        biomeInfoPanel = new VBox(8, biomeTitleLabel, biomeDescLabel, biomeEffectsLabel);
        biomeInfoPanel.setStyle("""
            -fx-background-color: #151522;
            -fx-padding: 16;
            -fx-background-radius: 8;
            -fx-border-color: #2a2a3a;
            -fx-border-radius: 8;
            -fx-border-width: 1;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);
        """);

        VBox.setVgrow(rightContent, Priority.ALWAYS);

        activeStatusesBox = new VBox(6);
        activeStatusesBox.setPadding(new Insets(12, 0, 0, 0));
        activeStatusesBox.setMinHeight(80);

        VBox rightSide = new VBox(8, biomeInfoPanel, tabButtons, rightContent, activeStatusesBox);

        HBox mainLayout = new HBox(12, gameContentVBox, rightSide);
        mainLayout.setPadding(new Insets(18));
        mainLayout.setStyle("-fx-background-color: #0d0d1a;");

        gameContentVBox.prefWidthProperty().bind(mainLayout.widthProperty().multiply(0.7).subtract(24));
        rightSide.prefWidthProperty().bind(mainLayout.widthProperty().multiply(0.3).subtract(24));

        return mainLayout;
    }

    private void fillCard(VBox card, GameEvent event) {
        card.getChildren().clear();

        boolean isWait  = "WAIT_TURN".equals(event.getId());
        boolean rare    = event.isHiddenEffects();
        boolean isQuest = event.getQuestId() != null;

        String bg   = isWait ? "#1a1a0e" : rare ? "#2c1a0e" : isQuest ? "#1a2e1a" : "#16213e";
        String bgHo = isWait ? "#2a2a18" : rare ? "#4a2a10" : isQuest ? "#1f3d1f" : "#1a2a50";
        String fg   = isWait ? "#f0c040" : rare ? "#ffaa44" : "#eee";

        card.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8; -fx-cursor: hand;");

        Label lbl = new Label(event.getLabel());
        lbl.setWrapText(true);
        lbl.setStyle("-fx-text-fill: " + fg + "; -fx-font-size: 14px;");
        lbl.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(lbl, Priority.ALWAYS);

        String fxStr = event.buildEffectsString();
        Label fxLabel = new Label(fxStr.isEmpty() ? "" : fxStr);
        fxLabel.setWrapText(true);
        fxLabel.setStyle("-fx-text-fill: " + (isWait ? "#f0c040" : "#7ec8a0") + "; -fx-font-size: 13px;");

        String distText = event.getDistanceCost() > 0 ? "   📍 -" + event.getDistanceCost() + " km" : "";
        Label metaLbl = new Label("⏱ " + event.getTimeCost() + "h" + distText);
        metaLbl.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        if (isWait) {
            Label badge = new Label("⏳ przeczekanie");
            badge.setStyle("-fx-text-fill: #f0c040; -fx-font-size: 12px;");
            card.getChildren().addAll(lbl, fxLabel, metaLbl, badge);
        } else if (isQuest && event.getQuestStage() > 1) {
            Label badge = new Label("📜 kontynuacja questa");
            badge.setStyle("-fx-text-fill: #f0c040; -fx-font-size: 12px;");
            card.getChildren().addAll(lbl, fxLabel, metaLbl, badge);
        } else if (isQuest) {
            Label badge = new Label("📜 quest");
            badge.setStyle("-fx-text-fill: #8bc48b; -fx-font-size: 12px;");
            card.getChildren().addAll(lbl, fxLabel, metaLbl, badge);
        } else {
            card.getChildren().addAll(lbl, fxLabel, metaLbl);
        }

        if (event.getDistanceCost() > 0 && engine.hasActiveLocalQuests(event.getQuestId())) {
            Label warnLbl = new Label("Ruch anuluje lokalne zadania!");
            warnLbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
            card.getChildren().add(warnLbl);
        }

        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace(bg, bgHo)));
        card.setOnMouseExited(e  -> card.setStyle(card.getStyle().replace(bgHo, bg)));
        card.setOnMouseClicked(e -> onCardClicked(event));
    }

    private void fillCardEmpty(VBox card) {
        card.getChildren().clear();
        card.setOnMouseClicked(null);
        card.setOnMouseEntered(null);
        card.setOnMouseExited(null);
        card.setStyle("-fx-background-color: #0f0f1a; -fx-background-radius: 8;");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Ekrany końcowe
    // ══════════════════════════════════════════════════════════════════════════

    private VBox buildEndScreen() {
        Label title = new Label("KONIEC GRY");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#e74c3c"));

        endTextLabel = new Label();
        endTextLabel.setFont(Font.font("Georgia", 17));
        endTextLabel.setTextFill(Color.web("#ddd"));
        endTextLabel.setWrapText(true);
        endTextLabel.setMaxWidth(480);
        endTextLabel.setTextAlignment(TextAlignment.CENTER);

        Button restart = new Button("▶  Zagraj ponownie");
        Button quit    = new Button("✕  Wyjdź");
        styleEndBtn(restart, "#27ae60");
        styleEndBtn(quit,    "#c0392b");
        restart.setOnAction(e -> showDifficultyScreen());
        quit.setOnAction(e    -> primaryStage.close());

        HBox btns = new HBox(24, restart, quit);
        btns.setAlignment(Pos.CENTER);

        VBox box = new VBox(32, title, endTextLabel, btns);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(90, 48, 48, 48));
        box.setStyle("-fx-background-color: #0d0d1a;");
        return box;
    }

    private VBox buildWinScreen() {
        Label title = new Label("KRAKÓW");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 44));
        title.setTextFill(Color.web("#f0c040"));

        Label sub = new Label("Dotarłeś.");
        sub.setFont(Font.font("Georgia", FontPosture.ITALIC, 22));
        sub.setTextFill(Color.web("#aaa"));

        Label detail = new Label("4000 kilometrów. Koniec drogi.");
        detail.setFont(Font.font("Georgia", 16));
        detail.setTextFill(Color.web("#666"));

        Button restart = new Button("▶  Zagraj ponownie");
        Button quit    = new Button("✕  Wyjdź");
        styleEndBtn(restart, "#27ae60");
        styleEndBtn(quit,    "#555");
        restart.setOnAction(e -> showDifficultyScreen());
        quit.setOnAction(e    -> primaryStage.close());

        HBox btns = new HBox(24, restart, quit);
        btns.setAlignment(Pos.CENTER);

        VBox box = new VBox(28, title, sub, detail, btns);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(120, 48, 48, 48));
        box.setStyle("-fx-background-color: #0d0d1a;");
        return box;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Logika kliknięć
    // ══════════════════════════════════════════════════════════════════════════

    private void onCardClicked(GameEvent event) {
        engine.executeEvent(event);
        refreshAll();
        if (engine.hasWon()) {
            mainScene.setRoot(winRoot);
        } else if (engine.isGameOver()) {
            endTextLabel.setText(engine.getEndingText());
            mainScene.setRoot(endRoot);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Odświeżanie HUD
    // ══════════════════════════════════════════════════════════════════════════

    private void refreshAll() {
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
            if (i < cards.size()) fillCard(cardSlots.get(i), cards.get(i));
            else                  fillCardEmpty(cardSlots.get(i));
        }

        StatusEffect triggered = engine.getStatusManager().getLastTriggered();
        if (triggered != null) {
            messageLabel.setText(messageLabel.getText()
                    + "\n" + triggered.getEmoji() + " Nowy status: " + triggered.getLabel()
                    + " — " + triggered.getDescription());
        }

        // AKTUALIZACJA PANELU ŚRODOWISKA
        Biome currentBiome = engine.getCurrentBiome();
        Weather currentWeather = engine.getCurrentWeather();
        String currentStage = engine.getCurrentStageName();

        biomeTitleLabel.setText("🚩 " + currentStage.toUpperCase()
                + "   |   " + currentBiome.getEmoji() + " " + currentBiome.getLabel().toUpperCase()  + "   |   " + currentWeather.getEmoji() + " " + currentWeather.getLabel().toUpperCase());

        biomeTitleLabel.setText(currentBiome.getEmoji() + " " + currentBiome.getLabel().toUpperCase());
        biomeDescLabel.setText(currentBiome.getEntryMessage());
        biomeEffectsLabel.setText(engine.buildBiomeInfo(currentBiome));

        activeStatusesBox.getChildren().clear();
        Label statusTitle = new Label("✦ Aktywne statusy");
        statusTitle.setStyle("-fx-text-fill: #9ab; -fx-font-size: 14px;");
        activeStatusesBox.getChildren().add(statusTitle);

        var statuses = engine.getStatusManager().getActiveStatuses();
        if (statuses.isEmpty()) {
            Label empty = new Label("Brak aktywnych statusów.");
            empty.setStyle("-fx-text-fill: #555; -fx-font-style: italic; -fx-font-size: 13px;");
            activeStatusesBox.getChildren().add(empty);
        } else {
            statuses.forEach((status, turns) -> {
                VBox statusBox = new VBox(2);

                String t = turns == 1 ? "tura" : (turns < 5 ? "tury" : "tur");
                Label nameLbl = new Label(status.getEmoji() + " " + status.getLabel() + " (" + turns + " " + t + ")");
                nameLbl.setStyle("-fx-text-fill: #f0c040; -fx-font-size: 13px;");

                Label descLbl = new Label(status.getDescription());
                descLbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
                descLbl.setWrapText(true);

                statusBox.getChildren().addAll(nameLbl, descLbl);
                activeStatusesBox.getChildren().add(statusBox);
            });
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI helpers
    // ══════════════════════════════════════════════════════════════════════════

    private void styleEndBtn(Button btn, String color) {
        btn.setStyle("""
            -fx-background-color: %s; -fx-text-fill: white;
            -fx-font-size: 16px; -fx-padding: 12 32;
            -fx-background-radius: 6; -fx-cursor: hand;
        """.formatted(color));
    }

    public static void main(String[] args) { launch(); }
}