package com.runofashes.engine;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AudioManager {
    private MediaPlayer mediaPlayer;

    public void playTheme() {
        try {
            String path = getClass().getResource("/com/runofashes/ui/sounds/magic-forest-kevin-macleod.mp3").toExternalForm();
            Media sound = new Media(path);
            mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(0.4);
            mediaPlayer.play();
        } catch (Exception e) {
            System.err.println("Nie udało się załadować muzyki: " + e.getMessage());
        }
    }
}