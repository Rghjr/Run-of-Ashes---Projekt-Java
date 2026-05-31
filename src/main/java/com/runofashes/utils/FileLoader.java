package com.runofashes.utils;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.*;

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
     * Tworzy tło (Background) z samym obrazkiem, bez winiety.
     * Używać tam gdzie nie chcemy efektu zanikania (np. BiomePanel).
     */
    public static Background createPlainBackground(Image image) {
        if (image == null) {
            return Background.EMPTY;
        }
        ImagePattern imagePattern = new ImagePattern(image, 0, 0, 1, 1, true);
        return new Background(new BackgroundFill(imagePattern, CornerRadii.EMPTY, Insets.EMPTY));
    }

    /**
     * Tworzy tło (Background) z efektem miękkiego RadialGradientu (winiety) wtapiającej grafikę z każdej strony.
     * @param image Obrazek bazowy
     * @param hexColor Kolor interfejsu, w który ma się wtopić obrazek (np. "#1a1a2e")
     */
    public static Background createFadedBackground(Image image, String hexColor) {
        if (image == null) {
            BackgroundFill solidFill = new BackgroundFill(Color.web(hexColor), CornerRadii.EMPTY, Insets.EMPTY);
            return new Background(solidFill);
        }

        BackgroundFill solidFill = new BackgroundFill(Color.web(hexColor), CornerRadii.EMPTY, Insets.EMPTY);

        ImagePattern imagePattern = new ImagePattern(image, 0, 0, 1, 1, true);
        BackgroundFill imageFill = new BackgroundFill(imagePattern, CornerRadii.EMPTY, Insets.EMPTY);

        RadialGradient gradient = new RadialGradient(
                0, 0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web(hexColor, 0.0)),  // Środek: obrazek w pełni widoczny
                new Stop(0.5, Color.web(hexColor, 0.2)),  // Delikatne wejście winiety
                new Stop(0.75, Color.web(hexColor, 0.7)), // Miękkie ściemnienie
                new Stop(1.0, Color.web(hexColor, 1.0))   // Krawędzie: pełne wtopienie w kolor tła
        );
        BackgroundFill vignetteFill = new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY);

        return new Background(solidFill, imageFill, vignetteFill);
    }
}