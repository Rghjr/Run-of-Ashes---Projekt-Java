package com.runofashes.engine;

public final class GameStage {

    private GameStage() {}

    public static String nameForDistance(int distance) {
        if (distance > 2600) return "Azja Mniejsza";
        if (distance > 1400) return "Góry";
        return "Europa";
    }
}
