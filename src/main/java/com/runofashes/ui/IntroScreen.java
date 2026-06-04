package com.runofashes.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.scene.Cursor;

public class IntroScreen extends StackPane {

    private final Runnable onFinish;
    private final Label textLabel;
    private final ImageView backgroundView;

    private int currentSlide = 0;
    private boolean isSkipped = false;

    private final String[] storyTexts = {
            "Świat, który znaliśmy, obrócił się w popiół.\nZaraza przyszła ze wschodu, pożerając imperia.",
            "Nie ma już królów, nie ma armii.\nZostali tylko zdesperowani wędrowcy pośród zgliszcz.",
            "W twojej kieszeni spoczywa wiadomość dla Krakowa.\nOstrzeżenie przed losem, który pożarł wschód.",
            "Przed tobą tysiące kilometrów pustyni i gór.\nDroga nie wybacza błędów.",
            "Ruszaj."
    };

    private final String[] backgroundImages = {
            "intro1.png",
            "intro2.png",
            "intro3.png",
            "mountains.png",
            "intro5.png"
    };

    public IntroScreen(Runnable onFinish) {
        this.onFinish = onFinish;
        setStyle("-fx-background-color: #05050a;");

        backgroundView = new ImageView();
        backgroundView.fitWidthProperty().bind(this.widthProperty());
        backgroundView.fitHeightProperty().bind(this.heightProperty());
        backgroundView.setPreserveRatio(false);
        backgroundView.setOpacity(0.0);

        PauseTransition hideCursorTimer = new PauseTransition(Duration.seconds(2));
        hideCursorTimer.setOnFinished(e -> {
            if (getScene() != null) {
                getScene().setCursor(Cursor.NONE);
            }
        });
        setOnMouseMoved(e -> {
            if (getScene() != null) {
                getScene().setCursor(Cursor.DEFAULT);
            }
            hideCursorTimer.playFromStart();
        });
        hideCursorTimer.play();

        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");

        textLabel = new Label();
        textLabel.setFont(Font.font("Palatino Linotype", FontWeight.NORMAL, 34));
        textLabel.setTextFill(Color.web("#d1c8bd"));

        textLabel.setTextAlignment(TextAlignment.CENTER);
        textLabel.setAlignment(Pos.CENTER);
        textLabel.setLineSpacing(15);
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(900);
        textLabel.setOpacity(0.0);

        StackPane.setAlignment(textLabel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(textLabel, new javafx.geometry.Insets(0, 0, 180, 0));
        Label skipHint = new Label("Kliknij dowolne miejsce, aby pominąć");
        skipHint.setFont(Font.font("System", 14));
        skipHint.setTextFill(Color.web("#666666"));
        StackPane.setAlignment(skipHint, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(skipHint, new javafx.geometry.Insets(20));

        getChildren().addAll(backgroundView, overlay, textLabel, skipHint);
        setOnMouseClicked(e -> skipIntro());
        playSlide();
    }

    private void playSlide() {
        if (isSkipped || currentSlide >= storyTexts.length) {
            endIntro();
            return;
        }

        textLabel.setText(storyTexts[currentSlide]);
        Image bgImg = com.runofashes.utils.FileLoader.loadUiImage(backgroundImages[currentSlide]);
        if (bgImg != null) backgroundView.setImage(bgImg);

        FadeTransition fadeInBg = new FadeTransition(Duration.seconds(2), backgroundView);
        fadeInBg.setToValue(1.0);
        FadeTransition fadeInText = new FadeTransition(Duration.seconds(2), textLabel);
        fadeInText.setToValue(1.0);
        PauseTransition wait = new PauseTransition(Duration.seconds(3));
        FadeTransition fadeOutText = new FadeTransition(Duration.seconds(2), textLabel);
        fadeOutText.setToValue(0.0);
        FadeTransition fadeOutBg = new FadeTransition(Duration.seconds(2), backgroundView);
        fadeOutBg.setToValue(0.0);

        fadeInBg.play();
        fadeInText.play();

        fadeInText.setOnFinished(e -> wait.play());
        wait.setOnFinished(e -> {
            fadeOutBg.play();
            fadeOutText.play();
        });

        fadeOutText.setOnFinished(e -> {
            currentSlide++;
            playSlide();
        });
    }

    private void skipIntro() {
        if (!isSkipped) {
            isSkipped = true;
            endIntro();
        }
    }

    private void endIntro() {
        Region whiteFlash = new Region();
        whiteFlash.setStyle("-fx-background-color: white;");
        whiteFlash.setOpacity(0.0);

        Region darkFade = new Region();
        darkFade.setStyle("-fx-background-color: #05050a;");
        darkFade.setOpacity(0.0);

        getChildren().addAll(whiteFlash, darkFade);

        FadeTransition flashIn = new FadeTransition(Duration.seconds(0.8), whiteFlash);
        flashIn.setToValue(1.0);
        FadeTransition darken = new FadeTransition(Duration.seconds(1.2), darkFade);
        darken.setToValue(1.0);

        flashIn.setOnFinished(e -> darken.play());
        darken.setOnFinished(e -> {
            darken.setOnFinished(null);
            getChildren().clear();

            onFinish.run();
        });

        flashIn.play();
    }
}