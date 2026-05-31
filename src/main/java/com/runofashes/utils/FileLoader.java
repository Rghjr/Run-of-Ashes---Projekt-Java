package com.runofashes.utils;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import java.util.Objects;

public final class FileLoader {

    private FileLoader() {}

    /**
     * Bezpiecznie wczytuje obrazek z podanej ścieżki w resources.
     * Jeśli plik nie istnieje, zwraca null zamiast wysypywać grę.
     */
    public static Image loadImage(String fullPath) {
        try {
            return new Image(Objects.requireNonNull(FileLoader.class.getResourceAsStream(fullPath)));
        } catch (Exception e) {
            System.out.println("FileLoader Error: Nie znaleziono pliku pod ścieżką -> " + fullPath);
            return null;
        }
    }

    /**
     * Wczytuje grafikę z folderu images/ na podstawie samej nazwy pliku.
     */
    public static Image loadUiImage(String imageName) {
        return loadImage("/com/runofashes/ui/images/" + imageName);
    }

    /**
     * Tworzy tło (Background) z efektem miękkiego RadialGradientu (winiety) wtapiającej grafikę z każdej strony.
     * @param image Obrazek bazowy
     * @param hexColor Kolor interfejsu, w który ma się wtopić obrazek (np. "#1a1a2e")
     */
    public static Background createFadedBackground(Image image, String hexColor) {
        if (image == null) {
            return Background.EMPTY;
        }

        BackgroundImage bgi = new BackgroundImage(
                image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true)
        );

        RadialGradient gradient = new RadialGradient(
                0, 0, 0.5, 0.5, 0.45, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(hexColor, 0.3)),   // Środek widoczny
                new Stop(0.6, Color.web(hexColor, 0.7)), // Miękkie wejście cienia
                new Stop(1, Color.web(hexColor, 1.0))    // 100% stopienia przed samą krawędzią
        );
        BackgroundFill vignetteFill = new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY);

        return new Background(new BackgroundFill[]{vignetteFill}, new BackgroundImage[]{bgi});
    }
}