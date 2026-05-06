package com.runofashes;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
    private Player player = new Player();

    private ProgressBar healthBar;
    private ProgressBar hungerBar;
    private ProgressBar energyBar;

    @Override
    public void start(Stage stage) {
        // tworzenie elementów HUD
        healthBar = new ProgressBar();
        hungerBar = new ProgressBar();
        energyBar = new ProgressBar();

        healthBar.setPrefWidth(200);
        hungerBar.setPrefWidth(200);
        energyBar.setPrefWidth(200);

        // układanie elementów na ekranie
        VBox hudBox = new VBox(5);
        hudBox.getChildren().addAll(
                new Label("Zdrowie:"), healthBar,
                new Label("Głód:"), hungerBar,
                new Label("Energia:"), energyBar
        );

        // akcja gracza (testowanie GUI)
        Button actionButton = new Button("Wykop dół (-30 Energii, -20 Głodu)");
        actionButton.setOnAction(e -> {
            player.setEnergy(player.getEnergy() - 30);
            player.setHunger(player.getHunger() - 20);

            updateHUD();
        });

        // składanie głównego okna
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(hudBox, actionButton);

        // start aplikacji
        updateHUD();
        Scene scene = new Scene(root, 400, 300);
        stage.setTitle("Run of Ashes - Ekran Główny");
        stage.setScene(scene);
        stage.show();
    }

    // metoda pomocnicza do aktualizacji HUD
    private void updateHUD() {
        healthBar.setProgress(player.getHealth() / 100.0);
        hungerBar.setProgress(player.getHunger() / 100.0);
        energyBar.setProgress(player.getEnergy() / 100.0);
    }

    public static void main(String[] args) {
        launch();
    }
}