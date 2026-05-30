package com.runofashes.ui;

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

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        engine.load();

        gameScreen = new GameScreen(engine, this::refreshAll, this::onCardClicked);
        endScreen  = new EndScreen(this::showDifficultyScreen, () -> primaryStage.close());
        winScreen  = new WinScreen(this::showDifficultyScreen, () -> primaryStage.close());

        difficultyScreen = new DifficultyScreen(this::onDifficultyConfirmed);
        mainScene = new Scene(difficultyScreen, 980, 800);

        mainScene.getStylesheets().add(Objects.requireNonNull(getClass().
                getResource("/com/runofashes/ui/style.css")).toExternalForm());

        stage.setTitle("Run of Ashes");
        stage.setMinWidth(980);
        stage.setMinHeight(800);
        stage.setScene(mainScene);
        stage.show();
    }

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
        mainScene.setRoot(gameScreen.getRoot());
    }

    private void onCardClicked(GameEvent event) {
        engine.executeEvent(event);
        refreshAll();
        if (engine.hasWon()) {
            mainScene.setRoot(winScreen);
        } else if (engine.isGameOver()) {
            endScreen.setEndingText(engine.getEndingText());
            mainScene.setRoot(endScreen);
        }
    }

    private void refreshAll() {
        gameScreen.refresh();
    }

    public static void main() {
        launch();
    }
}
