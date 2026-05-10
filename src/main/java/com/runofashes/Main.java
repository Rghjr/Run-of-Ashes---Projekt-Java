package com.runofashes;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private final GameEngine engine = new GameEngine();
    private Stage primaryStage;

    private ProgressBar healthBar, hungerBar, hydrationBar, energyBar, moraleBar;
    private Label healthVal, hungerVal, hydrationVal, energyVal, moraleVal;
    private Label timeLabel, distanceLabel;
    private Label messageLabel;

    private final List<VBox> cardSlots = new ArrayList<>();

    private VBox gameRoot, endRoot, winRoot;
    private Label endTextLabel;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        engine.load();

        gameRoot = buildGameScreen();
        endRoot  = buildEndScreen();
        winRoot  = buildWinScreen();

        refreshAll();

        Scene scene = new Scene(gameRoot, 680, 860);
        stage.setTitle("Run of Ashes");
        stage.setScene(scene);
        stage.show();
    }

    // ── Ekran gry ─────────────────────────────────────────────────────────────

    private VBox buildGameScreen() {
        timeLabel     = styledLabel("Dzień 1,  00:00", "#aaa", 15);
        distanceLabel = styledLabel("4000 km do Krakowa", "#e67e22", 15);

        HBox topRow = new HBox(24, timeLabel, distanceLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        healthBar    = makeBar("#e74c3c");
        hungerBar    = makeBar("#e67e22");
        hydrationBar = makeBar("#3498db");
        energyBar    = makeBar("#f1c40f");
        moraleBar    = makeBar("#9b59b6");

        healthVal    = valueLabel();
        hungerVal    = valueLabel();
        hydrationVal = valueLabel();
        energyVal    = valueLabel();
        moraleVal    = valueLabel();

        VBox hud = new VBox(8,
                topRow,
                statRow("❤  Zdrowie",     healthBar,    healthVal),
                statRow("🍗  Głód",        hungerBar,    hungerVal),
                statRow("💧  Nawodnienie", hydrationBar, hydrationVal),
                statRow("⚡  Energia",     energyBar,    energyVal),
                statRow("😊  Nadzieja",    moraleBar,    moraleVal)
        );
        hud.setPadding(new Insets(16));
        hud.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 8;");

        messageLabel = new Label(" ");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(640);
        messageLabel.setMinHeight(42);
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

        VBox root = new VBox(16, hud, messageLabel, grid);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: #0d0d1a;");
        return root;
    }

    private void fillCard(VBox card, GameEvent event) {
        card.getChildren().clear();

        boolean rare    = event.isHiddenEffects();
        boolean isQuest = event.getQuestId() != null;
        String  bg   = rare ? "#2c1a0e" : isQuest ? "#1a2e1a" : "#16213e";
        String  bgHo = rare ? "#4a2a10" : isQuest ? "#1f3d1f" : "#1a2a50";
        String  fg   = rare ? "#ffaa44" : "#eee";

        card.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 8; -fx-cursor: hand;", bg));

        // opis akcji
        Label lbl = new Label(event.getLabel());
        lbl.setWrapText(true);
        lbl.setStyle("-fx-text-fill: " + fg + "; -fx-font-size: 14px;");
        lbl.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(lbl, Priority.ALWAYS);

        // efekty oczekiwane
        String fxStr = event.buildEffectsString();
        Label fxLabel = new Label(fxStr.isEmpty() ? "" : fxStr);
        fxLabel.setWrapText(true);
        fxLabel.setStyle("-fx-text-fill: #7ec8a0; -fx-font-size: 13px;");

        // meta
        String distText = event.getDistanceCost() > 0 ? "   📍 -" + event.getDistanceCost() + " km" : "";
        Label metaLbl = new Label("⏱ " + event.getTimeCost() + "h" + distText);
        metaLbl.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        if (isQuest && event.getQuestStage() > 1) {
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

    // ── Ekrany końcowe ────────────────────────────────────────────────────────

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

        Button restart = new Button("▶  Od początku");
        Button quit    = new Button("✕  Wyjdź");
        styleEndBtn(restart, "#27ae60");
        styleEndBtn(quit,    "#c0392b");
        restart.setOnAction(e -> restartGame());
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
        restart.setOnAction(e -> restartGame());
        quit.setOnAction(e    -> primaryStage.close());

        HBox btns = new HBox(24, restart, quit);
        btns.setAlignment(Pos.CENTER);

        VBox box = new VBox(28, title, sub, detail, btns);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(120, 48, 48, 48));
        box.setStyle("-fx-background-color: #0d0d1a;");
        return box;
    }

    // ── Logika ────────────────────────────────────────────────────────────────

    private void onCardClicked(GameEvent event) {
        engine.executeEvent(event);
        refreshAll();
        if (engine.hasWon()) {
            primaryStage.getScene().setRoot(winRoot);
        } else if (engine.isGameOver()) {
            endTextLabel.setText(engine.getEndingText());
            primaryStage.getScene().setRoot(endRoot);
        }
    }

    private void restartGame() {
        engine.reset();
        refreshAll();
        primaryStage.getScene().setRoot(gameRoot);
    }

    // ── Odświeżanie ───────────────────────────────────────────────────────────

    private void refreshAll() {
        Player p = engine.getPlayer();

        setBar(healthBar,    healthVal,    p.getHealth());
        setBar(hungerBar,    hungerVal,    p.getHunger());
        setBar(hydrationBar, hydrationVal, p.getHydration());
        setBar(energyBar,    energyVal,    p.getEnergy());
        setBar(moraleBar,    moraleVal,    p.getMorale());

        timeLabel.setText(p.getTimeFormatted());
        distanceLabel.setText(p.getDistance() + " km do Krakowa");

        String msgColor = switch (engine.getLastResult()) {
            case SUCCESS -> "#7ec8a0";
            case PARTIAL -> "#f0c040";
            case FAIL    -> "#e74c3c";
        };
        messageLabel.setStyle("-fx-text-fill: " + msgColor
                + "; -fx-font-style: italic; -fx-font-size: 15px;");
        messageLabel.setText(engine.getLastMessage());

        List<GameEvent> cards = engine.getCurrentCards();
        for (int i = 0; i < 4; i++) {
            if (i < cards.size()) fillCard(cardSlots.get(i), cards.get(i));
            else                  fillCardEmpty(cardSlots.get(i));
        }
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private HBox statRow(String name, ProgressBar bar, Label val) {
        Label n = new Label(name);
        n.setMinWidth(158);
        n.setStyle("-fx-text-fill: #ccc; -fx-font-size: 15px;");
        bar.setPrefWidth(240);
        HBox row = new HBox(12, n, bar, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private ProgressBar makeBar(String hex) {
        ProgressBar bar = new ProgressBar(1.0);
        bar.setPrefHeight(18);
        bar.setStyle("-fx-accent: " + hex + ";");
        return bar;
    }

    private void setBar(ProgressBar bar, Label lbl, int value) {
        bar.setProgress(value / 100.0);
        lbl.setText(value + "/100");
    }

    private Label valueLabel() {
        Label l = new Label();
        l.setMinWidth(64);
        l.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
        return l;
    }

    private Label styledLabel(String text, String color, int size) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + size + "px;");
        return l;
    }

    private void styleEndBtn(Button btn, String color) {
        btn.setStyle(String.format("""
            -fx-background-color: %s; -fx-text-fill: white;
            -fx-font-size: 16px; -fx-padding: 12 32;
            -fx-background-radius: 6; -fx-cursor: hand;
        """, color));
    }

    public static void main(String[] args) { launch(); }
}