package com.runofashes;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Button btn = new Button("Run of Ashes — Start");
        btn.setOnAction(e -> btn.setText("Działa!"));

        StackPane root = new StackPane(btn);
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Run of Ashes");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}