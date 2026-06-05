package com.runofashes.engine;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;

public class AudioManager {
    private MediaPlayer mediaPlayer;
    private static final double TARGET_VOLUME = 0.4;

    public void playTheme() {
        try {
            String path = getClass().getResource("/com/runofashes/ui/sounds/magic-forest-kevin-macleod.mp3").toExternalForm();
            Media sound = new Media(path);
            mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(TARGET_VOLUME);
            mediaPlayer.play();
        } catch (Exception e) {
            System.err.println("Nie udało się załadować muzyki: " + e.getMessage());
        }
    }

    /**
     * Płynnie wycisza obecną muzykę i włącza nową
     * @param resourcePath Ścieżka do pliku audio
     */
    public void changeMusic(String resourcePath) {
        if (mediaPlayer != null) {
            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.seconds(3), new KeyValue(mediaPlayer.volumeProperty(), 0))
            );

            fadeOut.setOnFinished(e -> {
                mediaPlayer.stop();
                startNewMusic(resourcePath);
            });

            fadeOut.play();
        } else {
            startNewMusic(resourcePath);
        }
    }

    /**
     * Uruchamia nową muzykę z efektem fade-in.
     */
    private void startNewMusic(String resourcePath) {
        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource == null) {
                System.err.println("Nie znaleziono pliku audio: " + resourcePath);
                return;
            }

            Media media = new Media(resource.toString());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setVolume(0.0);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.play();

            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.seconds(3), new KeyValue(mediaPlayer.volumeProperty(), TARGET_VOLUME))
            );
            fadeIn.play();

        } catch (Exception e) {
            System.err.println("Błąd ładowania muzyki (" + resourcePath + "): " + e.getMessage());
        }
    }
}