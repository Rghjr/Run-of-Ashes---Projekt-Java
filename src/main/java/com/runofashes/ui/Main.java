package com.runofashes.ui;

import com.runofashes.engine.AchievementTracker;
import com.runofashes.engine.AudioManager;
import com.runofashes.engine.GameEngine;
import com.runofashes.model.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Set;

public class Main extends Application {

    private final GameEngine engine = new GameEngine();

    private Stage primaryStage;
    private Scene mainScene;

    private DifficultyScreen difficultyScreen;
    private TraitSelectionScreen traitScreen;

    private GameScreen gameScreen;
    private EndScreen endScreen;
    private WinScreen winScreen;
    private String keyBuffer = "";

    private boolean isTransitioning = false;

    private final AudioManager audioManager = new AudioManager();

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        engine.load();

        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        audioManager.playTheme();

        gameScreen = new GameScreen(
                engine,
                this::refreshAll,
                this::onCardClicked,
                this::showMainMenu,
                this::showSettingsFromGame,
                () -> primaryStage.close()
        );
        endScreen  = new EndScreen(this::returnToMenuFromEnd, this::showCurrentStatsScreen, this::quitFromEnd);
        winScreen  = new WinScreen(this::returnToMenuFromEnd, this::showCurrentStatsScreen, this::quitFromEnd);

        mainScene = new Scene(new javafx.scene.layout.StackPane(), 980, 800, javafx.scene.paint.Color.web("#05050a"));
        showIntro();

        mainScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_TYPED, event -> {
            if (mainScene.getRoot() == gameScreen.getRoot()) {
                keyBuffer += event.getCharacter().toLowerCase();

                if (keyBuffer.length() > 4) {
                    keyBuffer = keyBuffer.substring(keyBuffer.length() - 4);
                }

                if (keyBuffer.equals("baba")) {
                    show();
                    keyBuffer = "";
                }
            }
        });

        mainScene.getStylesheets().add(Objects.requireNonNull(getClass().
                getResource("/com/runofashes/ui/style.css")).toExternalForm());

        stage.setTitle("Run of Ashes");
        try {
            javafx.scene.image.Image appIcon = new javafx.scene.image.Image(
                    java.util.Objects.requireNonNull(getClass().getResourceAsStream("/com/runofashes/ui/images/icon.png"))
            );
            stage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.out.println("Nie udało się załadować ikony gry: " + e.getMessage());
        }
        stage.setMinWidth(980);
        stage.setMinHeight(800);
        stage.setScene(mainScene);
        stage.show();
    }

    private void showDifficultyScreen() {
        mainScene.setCursor(javafx.scene.Cursor.DEFAULT);
        difficultyScreen = new DifficultyScreen(this::onDifficultyConfirmed);
        mainScene.setRoot(difficultyScreen);
    }

    private void showStatsScreen() {
        mainScene.setCursor(javafx.scene.Cursor.DEFAULT);
        StatsScreen statsScreen = new StatsScreen(engine.getStatsManager().getAllRuns(), this::showDifficultyScreen);
        mainScene.setRoot(statsScreen);
    }

    private void showAchievements(Runnable onBack) {
        AchievementsScreen achievementsScreen = new AchievementsScreen(engine.getAchievementManager(), onBack);
        mainScene.setRoot(achievementsScreen);
    }

    private void showCurrentStatsScreen() {
        RunStatistics currentStats = engine.getStatsManager().getCurrentRun();

        if (currentStats == null) {
            var allRuns = engine.getStatsManager().getAllRuns();
            if (!allRuns.isEmpty()) {
                currentStats = allRuns.get(allRuns.size() - 1);
            }
        }

        CurrentRunStatsScreen statsScreen = new CurrentRunStatsScreen(
                currentStats,
                () -> showAchievements(this::showCurrentStatsScreen),
                this::returnToMenuFromEnd,
                this::quitFromEnd
        );
        mainScene.setRoot(statsScreen);
    }

    private void showMainMenu() {
        MainMenuScreen mainMenu = new MainMenuScreen(
                this::showSaveNameScreen,
                this::showLoadGameScreen,
                this::showSettingsFromMenu,
                () -> primaryStage.close()
        );
        mainScene.setRoot(mainMenu);
    }

    private void showIntro() {
        IntroScreen intro = new IntroScreen(this::showMainMenu);
        mainScene.setRoot(intro);
    }

    private void showLoadGameScreen() {
        mainScene.setCursor(javafx.scene.Cursor.DEFAULT);
        LoadGameScreen loadScreen = new LoadGameScreen(
                this::loadGame,
                this::showMainMenu
        );
        mainScene.setRoot(loadScreen);
    }

    private void showSaveNameScreen() {
        mainScene.setCursor(javafx.scene.Cursor.DEFAULT);
        SaveNameScreen saveNameScreen = new SaveNameScreen(
                filename -> {
                    engine.setSaveFilename(filename);
                    showDifficultyScreen();
                },
                this::showMainMenu
        );
        mainScene.setRoot(saveNameScreen);
    }

    private void showSettingsFromMenu() {
        mainScene.setCursor(javafx.scene.Cursor.DEFAULT);
        SettingsScreen settings = new SettingsScreen(audioManager, this::showMainMenu);
        mainScene.setRoot(settings);
    }

    private void showSettingsFromGame() {
        mainScene.setCursor(javafx.scene.Cursor.DEFAULT);
        SettingsScreen settings = new SettingsScreen(audioManager, () -> {
            mainScene.setRoot(gameScreen.getRoot());
        });
        mainScene.setRoot(settings);
    }

    private void onDifficultyConfirmed() {
        Difficulty diff = difficultyScreen.getSelected();
        if (diff == null) return;

        traitScreen = new TraitSelectionScreen(diff, () -> onTraitsConfirmed(diff), this::showDifficultyScreen);
        mainScene.setRoot(traitScreen);
    }

    private void onTraitsConfirmed(Difficulty diff) {
        Set<Trait> traits = traitScreen.getSelected();
        engine.configure(diff, traits);
        engine.reset();
        gameScreen.setLastEvent(null);
        mainScene.setRoot(gameScreen.getRoot());
        javafx.application.Platform.runLater(this::refreshAll);
    }

    private void onCardClicked(GameEvent event) {
        if (engine.hasWon() || engine.isGameOver()) return;

        gameScreen.setLastEvent(event);

        engine.executeEvent(event);
        refreshAll();

        if (engine.hasWon()) {
            engine.deleteSaveFile();
            AchievementTracker.checkEndGame(engine, true);
            engine.getStatsManager().finalizeAndSaveRun(true, "Przeżycie", engine.getPlayer());

            audioManager.changeMusic("/com/runofashes/ui/sounds/Pufino-ThereBeDragons.mp3");

            OutroScreen outro = new OutroScreen(true, null, null, () -> mainScene.setRoot(winScreen));
            mainScene.setRoot(outro);

        } else if (engine.isGameOver()) {
            engine.deleteSaveFile();
            AchievementTracker.checkEndGame(engine, false);

            String deadStat = engine.getPlayer().getDeadStat();
            String deathReason = engine.getEndingText();

            engine.getStatsManager().finalizeAndSaveRun(false, deadStat, engine.getPlayer());
            endScreen.setEndingText(deathReason);

            //inna muzyka
            //audioManager.changeMusic("/com/runofashes/ui/sounds/Pufino-ThereBeDragons.mp3");

            OutroScreen outro = new OutroScreen(false, deadStat, deathReason, () -> mainScene.setRoot(endScreen));
            mainScene.setRoot(outro);
        }
    }

    private void refreshAll() {
        gameScreen.refresh();
    }

    public static void main(String[] args) {
        launch();
    }

    private void show() {
        try {
            javafx.scene.image.Image img = new javafx.scene.image.Image(
                    java.util.Objects.requireNonNull(getClass().getResourceAsStream("/com/runofashes/ui/images/baba_z_ogorem.jpg"))
            );
            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
            imgView.setPreserveRatio(true);
            imgView.setFitHeight(800);

            javafx.scene.layout.StackPane pane = new javafx.scene.layout.StackPane(imgView);
            pane.setStyle("-fx-background-color: transparent;");

            javafx.stage.Stage eggStage = new javafx.stage.Stage();
            javafx.scene.Scene eggScene = new javafx.scene.Scene(pane, javafx.scene.paint.Color.TRANSPARENT);

            eggStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            eggStage.initOwner(primaryStage);
            eggStage.setScene(eggScene);

            eggStage.show();

            eggStage.setX(primaryStage.getX() + (primaryStage.getWidth() - eggStage.getWidth()) / 2);
            eggStage.setY(primaryStage.getY() + (primaryStage.getHeight() - eggStage.getHeight()) / 2);

            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            delay.setOnFinished(e -> eggStage.close());
            delay.play();

        } catch (Exception e) {
            System.out.println("Błąd Easter Ega: " + e.getMessage());
        }
    }

    private void loadGame(String filename) {
        try {
            engine.loadGame(filename);
            gameScreen.setLastEvent(null);
            mainScene.setRoot(gameScreen.getRoot());
            javafx.application.Platform.runLater(this::refreshAll);
        } catch (Exception e) {
            System.err.println("Błąd wczytywania: " + e.getMessage());
        }
    }

    private void returnToMenuFromEnd() {
        audioManager.changeMusic("/com/runofashes/ui/sounds/magic-forest-kevin-macleod.mp3");
        showMainMenu();
    }

    private void quitFromEnd() {
        primaryStage.close();
    }
}