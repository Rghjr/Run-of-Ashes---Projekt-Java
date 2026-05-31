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
    private String keyBuffer = "";

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        engine.load();

        gameScreen = new GameScreen(engine, this::refreshAll, this::onCardClicked);
        endScreen  = new EndScreen(this::showDifficultyScreen, () -> primaryStage.close());
        winScreen  = new WinScreen(this::showDifficultyScreen, () -> primaryStage.close());

        difficultyScreen = new DifficultyScreen(this::onDifficultyConfirmed);
        mainScene = new Scene(difficultyScreen, 980, 800);

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
        mainScene.setRoot(gameScreen.getRoot());
        javafx.application.Platform.runLater(this::refreshAll);
    }

    private void onCardClicked(GameEvent event) {
        engine.executeEvent(event);
        gameScreen.setLastEvent(event);
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
}