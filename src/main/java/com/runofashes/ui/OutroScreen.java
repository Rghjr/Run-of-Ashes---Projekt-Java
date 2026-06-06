package com.runofashes.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
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

public class OutroScreen extends StackPane {

    private final Runnable onFinish;
    private final Label textLabel;
    private final ImageView backgroundView;

    private int currentSlide = 0;
    private boolean isSkipped = false;
    private boolean isEnding = false;
    private final boolean isWin;

    private final String[] storyTexts;
    private final String[] backgroundImages;

    private final String[] SUCCESS_TEXTS = {
            "Przedarłeś się przez mróz Karpat i żar stepów. Twoje buty, przesiąknięte krwią i błotem połowy świata, w końcu uderzyły o twardy bruk krakowskiego rynku.",
            "Wawel wznosi się nad tobą, wciąż dumny i jeszcze nienaruszony. Wyciągasz z kieszeni zmięty, pobrudzony pergamin – ostrzeżenie przed losem, który pożarł wschód. Twoja misja dobiegła końca. Przekazałeś im prawdę.",
            "Zrobiłeś wszystko, co mógł zrobić człowiek. Dałeś im najcenniejszy dar, jaki można podarować w tych czasach: czas na modlitwę, czas na ucieczkę... czas na pożegnanie.",
            "Nie wiesz, czy Kraków przetrwa. Nie wiesz, czy mur strachu i kordony sanitarne zatrzymają to, co nadchodzi. Ale dzisiejszej nocy, po raz pierwszy od tysięcy kilometrów, możesz zamknąć oczy bez lęku o kolejny świt.",
            "Dotarłeś. Ocaliłeś ich... albo pozwoliłeś im umrzeć z otwartymi oczami."
    };

    private final String[] DEFEAT_TEXTS = {
            "Droga nie wybacza błędów, a ta runda okazała się twoją ostatnią.",
            "Twoje ciało w końcu odmówiło posłuszeństwa. Osuwasz się na twardą, jałową ziemię, z dala od jakichkolwiek traktów, tam, gdzie nikt nie usłyszy twojego ostatniego tchnienia. Wzrok marnieje, a horyzont – i odległy, bezpieczny Kraków – rozpływa się w gęstniejącej ciemności.",
            "List, który nosisz w kieszeni, nigdy nie trafi do rąk adresata. Słowa ostrzeżenia zgniją w pyle razem z tobą, zamieniając się w milczący pomnik u boku tysięcy innych bezimiennych wędrowców, których pożarł ten szlak.",
            "Zaraza idzie dalej, niepowstrzymana, ślepa i głucha na ludzkie poświęcenie. Zachód wciąż śpi spokojnie, nieświadomy, że ich kat jest już o kilka dni drogi stąd.",
            "Świat, który próbowałeś ratować, powoli obraca się w popiół. Zostaje tylko dym, zimny wiatr i pustka, która wkrótce przykryje twoje ślady.",
            "Upadłeś... a razem z tobą zgasła ostatnia iskra nadziei."
    };

    private final String[] CREDITS_TEXTS = {
            "MUZYKA\nMagic Forest - Kevin MacLeod\nThere Be Dragons - Pufino\n\nGRAFIKI\nUtworzone przy pomocy sztucznej inteligencji",
            "TWÓRCY\n\nMartyna                  Krystian                  Bartłomiej\nTuszewska               Strzępek                Zięcina",
            "Dziękujemy za odbycie wspólnej przygody.\nDo zobaczenia na szlaku!"
    };

    private final String[] CREDITS_IMAGES = {
            "credits_1.png",
            "credits_2.png",
            "credits_3.png"
    };

    public OutroScreen(boolean isWin, String deadStat, String causeOfDeathText, Runnable onFinish) {
        this.isWin = isWin;
        this.onFinish = onFinish;
        setStyle("-fx-background-color: #05050a;");

        if (isWin) {
            int totalLen = SUCCESS_TEXTS.length + CREDITS_TEXTS.length;
            this.storyTexts = new String[totalLen];
            this.backgroundImages = new String[totalLen];

            System.arraycopy(SUCCESS_TEXTS, 0, this.storyTexts, 0, SUCCESS_TEXTS.length);
            String[] winBgs = {
                    "win_outro_1.png", "win_outro_1.png",
                    "win_outro_2.png", "win_outro_2.png", "win_bg.png"
            };
            System.arraycopy(winBgs, 0, this.backgroundImages, 0, winBgs.length);

            System.arraycopy(CREDITS_TEXTS, 0, this.storyTexts, SUCCESS_TEXTS.length, CREDITS_TEXTS.length);
            System.arraycopy(CREDITS_IMAGES, 0, this.backgroundImages, winBgs.length, CREDITS_IMAGES.length);

        } else {
            int totalLen = 2 + DEFEAT_TEXTS.length + CREDITS_TEXTS.length;
            this.storyTexts = new String[totalLen];
            this.backgroundImages = new String[totalLen];

            this.storyTexts[0] = translateDeadStat(deadStat);
            this.storyTexts[1] = causeOfDeathText;

            System.arraycopy(DEFEAT_TEXTS, 0, this.storyTexts, 2, DEFEAT_TEXTS.length);

            String deathImage = (deadStat != null ? deadStat : "event_default") + ".png";
            this.backgroundImages[0] = deathImage;
            this.backgroundImages[1] = deathImage;

            String[] defBgs = {
                    "defeat_outro_1.png", "defeat_outro_1.png",
                    "defeat_outro_2.png", "defeat_outro_2.png",
                    "end_bg.png", "end_bg.png"
            };
            System.arraycopy(defBgs, 0, this.backgroundImages, 2, defBgs.length);

            System.arraycopy(CREDITS_TEXTS, 0, this.storyTexts, 2 + DEFEAT_TEXTS.length, CREDITS_TEXTS.length);
            System.arraycopy(CREDITS_IMAGES, 0, this.backgroundImages, 2 + defBgs.length, CREDITS_IMAGES.length);
        }

        backgroundView = new ImageView();
        backgroundView.fitWidthProperty().bind(this.widthProperty());
        backgroundView.fitHeightProperty().bind(this.heightProperty());
        backgroundView.setPreserveRatio(false);
        backgroundView.setOpacity(0.0);

        PauseTransition hideCursorTimer = new PauseTransition(Duration.seconds(2));
        hideCursorTimer.setOnFinished(e -> {
            if (getScene() != null) getScene().setCursor(Cursor.NONE);
        });
        setOnMouseMoved(e -> {
            if (getScene() != null) getScene().setCursor(Cursor.DEFAULT);
            hideCursorTimer.playFromStart();
        });
        hideCursorTimer.play();

        //jasność tła
        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1);");

        textLabel = new Label();
        textLabel.setTextAlignment(TextAlignment.CENTER);
        textLabel.setAlignment(Pos.CENTER);
        textLabel.setLineSpacing(15);
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(900);
        textLabel.setOpacity(0.0);

        StackPane.setAlignment(textLabel, Pos.CENTER);

        Label skipHint = new Label("Kliknij dowolne miejsce, aby pominąć");
        skipHint.setFont(Font.font("System", 14));
        skipHint.setTextFill(Color.web("#666666"));
        StackPane.setAlignment(skipHint, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(skipHint, new javafx.geometry.Insets(20));

        getChildren().addAll(backgroundView, overlay, textLabel, skipHint);
        setOnMouseClicked(e -> skipOutro());

        playSlide();
    }

    private String translateDeadStat(String stat) {
        if (stat == null) return "UMARŁEŚ Z NIEZNANYCH PRZYCZYN";
        return switch (stat) {
            case "health"    -> "UMARŁEŚ Z POWODU ODNIESIONYCH RAN";
            case "hunger"    -> "UMARŁEŚ Z GŁODU";
            case "hydration" -> "UMARŁEŚ Z ODWODNIENIA";
            case "energy"    -> "UMARŁEŚ Z WYCZERPANIA";
            case "morale"    -> "UMARŁEŚ Z BRAKU NADZIEI";
            default          -> "UMARŁEŚ";
        };
    }

    private void playSlide() {
        if (isSkipped || currentSlide >= storyTexts.length) {
            endOutro();
            return;
        }

        if (!isWin && currentSlide == 0) {
            textLabel.setTextFill(Color.web("#8b0000"));
            textLabel.setFont(Font.font("Palatino Linotype", FontWeight.BOLD, 46));
        } else if (currentSlide >= storyTexts.length - CREDITS_TEXTS.length) {
            textLabel.setTextFill(Color.web("#ffd700"));
            textLabel.setFont(Font.font("Palatino Linotype", FontWeight.NORMAL, 32));
        } else {
            textLabel.setTextFill(Color.web(isWin ? "#e6d5b8" : "#a89f91"));
            textLabel.setFont(Font.font("Palatino Linotype", FontWeight.NORMAL, 32));
        }

        textLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.95), 12, 0, 0, 3);");

        textLabel.setText(storyTexts[currentSlide]);

        Image bgImg = com.runofashes.utils.FileLoader.loadUiImage(backgroundImages[currentSlide]);
        if (bgImg == null) bgImg = com.runofashes.utils.FileLoader.loadUiImage("event_default.png");
        backgroundView.setImage(bgImg);

        FadeTransition fadeInBg = new FadeTransition(Duration.seconds(2), backgroundView);
        fadeInBg.setToValue(1.0);

        FadeTransition fadeInText = new FadeTransition(Duration.seconds(2), textLabel);
        fadeInText.setToValue(1.0);

        double calculatedWait = Math.max(4.0, storyTexts[currentSlide].length() / 20.0);

        double waitTime = (!isWin && currentSlide == 0) ? 3.5 : calculatedWait;
        PauseTransition wait = new PauseTransition(Duration.seconds(waitTime));

        FadeTransition fadeOutText = new FadeTransition(Duration.seconds(2), textLabel);
        fadeOutText.setToValue(0.0);

        FadeTransition fadeOutBg = new FadeTransition(Duration.seconds(2), backgroundView);
        fadeOutBg.setToValue(0.0);

        fadeInBg.play();
        fadeInText.play();

        fadeInText.setOnFinished(e -> wait.play());

        wait.setOnFinished(e -> {
            fadeOutText.play();
            boolean sameNextImage = (currentSlide + 1 < backgroundImages.length) &&
                    backgroundImages[currentSlide].equals(backgroundImages[currentSlide + 1]);

            if (!sameNextImage) {
                fadeOutBg.play();
            } else {
                fadeOutText.setOnFinished(ev -> {
                    currentSlide++;
                    playSlide();
                });
            }
        });

        fadeOutBg.setOnFinished(e -> {
            currentSlide++;
            playSlide();
        });
    }

    private void skipOutro() {
        if (!isSkipped) {
            isSkipped = true;
            endOutro();
        }
    }

    private void endOutro() {
        if (isEnding) return;
        isEnding = true;

        Region darkFade = new Region();
        darkFade.setStyle("-fx-background-color: #05050a;");
        darkFade.setOpacity(0.0);
        getChildren().add(darkFade);

        FadeTransition darken = new FadeTransition(Duration.seconds(1.5), darkFade);
        darken.setToValue(1.0);

        darken.setOnFinished(e -> {
            darken.setOnFinished(null);
            getChildren().clear();
            onFinish.run();
        });

        darken.play();
    }
}