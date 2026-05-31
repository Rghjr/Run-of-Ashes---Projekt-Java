package com.runofashes.model;

import java.util.Map;

/**
 * Pogoda — losowo zmienia się co kilka tur.
 * Każda pogoda ma efekty per-tura i modyfikatory puli eventów.
 */
public enum Weather {

    CLEAR(
            "Pogodnie",
            "fas fa-sun",
            Map.of(),                                  // brak efektów per-tura
            Map.of("move", 10),                        // łatwiej się poruszać
            4, 8,                                      // minTurns, maxTurns zanim zmiana
            0.35                                       // waga losowania
    ),
    HOT(
            "Upał",
            "fas fa-temperature-high",
            Map.of("hydration", -3, "energy", -2),    // szybka dehydratacja
            Map.of("hydration", 25),
            3, 6,
            0.20
    ),
    RAIN(
            "Deszcz",
            "fas fa-cloud-rain",
            Map.of("morale", -2, "energy", -1),
            Map.of("hydration", -15, "morale", 10),   // mniej eventów wody (i tak mokro), więcej morale (ulga)
            3, 7,
            0.25
    ),
    STORM(
            "Burza",
            "fas fa-bolt",
            Map.of("morale", -4, "energy", -3, "health", -1),
            Map.of("move", -20, "hydration", -10),    // trudno się poruszać
            2, 4,
            0.10
    ),
    COLD(
            "Mróz",
            "fas fa-snowflake",
            Map.of("energy", -3, "health", -2),
            Map.of("food", 15, "energy", 10),          // więcej eventów jedzenia i ogrzewania
            3, 6,
            0.10
    );

    // ── Pola ──────────────────────────────────────────────────────────────────

    private final String label;
    private final String emoji;
    /** Efekty aplikowane każdą turę gdy ta pogoda jest aktywna. */
    private final Map<String, Integer> perTurnEffects;
    /** Modyfikatory wag puli eventów. */
    private final Map<String, Integer> eventWeightMods;
    /** Min/max tur zanim pogoda się zmieni. */
    private final int minTurns;
    private final int maxTurns;
    /** Waga w losowaniu kolejnej pogody (wyżej = częściej). */
    private final double weight;

    // ── Konstruktor ───────────────────────────────────────────────────────────

    Weather(String label, String emoji,
            Map<String, Integer> perTurnEffects,
            Map<String, Integer> eventWeightMods,
            int minTurns, int maxTurns,
            double weight) {
        this.label           = label;
        this.emoji           = emoji;
        this.perTurnEffects  = perTurnEffects;
        this.eventWeightMods = eventWeightMods;
        this.minTurns        = minTurns;
        this.maxTurns        = maxTurns;
        this.weight          = weight;
    }

    // ── Gettery ───────────────────────────────────────────────────────────────

    public String getLabel()                        { return label; }
    public String getEmoji()                        { return emoji; }
    public Map<String, Integer> getPerTurnEffects() { return perTurnEffects; }
    public Map<String, Integer> getEventWeightMods(){ return eventWeightMods; }
    public int    getMinTurns()                     { return minTurns; }
    public int    getMaxTurns()                     { return maxTurns; }
    public double getWeight()                       { return weight; }

    /**
     * Losuje nową pogodę ważonym losowaniem — nie może być ta sama co current.
     */
    public static Weather rollNext(Weather current, java.util.Random rng) {
        double totalWeight = 0;
        for (Weather w : values()) {
            if (w != current) totalWeight += w.weight;
        }
        double roll = rng.nextDouble() * totalWeight;
        double cumulative = 0;
        for (Weather w : values()) {
            if (w == current) continue;
            cumulative += w.weight;
            if (roll < cumulative) return w;
        }
        return CLEAR; // fallback
    }
}