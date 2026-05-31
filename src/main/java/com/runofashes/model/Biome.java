package com.runofashes.model;

import java.util.Map;
import java.util.Random;

/**
 * Biomy trasy z Azji do Europy.
 */
public enum Biome {

    STEPPE(
            "Step",
            "fas-wind",
            Map.of("hydration", 1.3, "hunger", 1.1, "energy", 1.0),
            Map.of("hydration", 20, "move", 10),
            "Bezkresny step. Słońce praży, woda rzadka jak obietnice."
    ),
    DESERT(
            "Pustynia",
            "fas-sun",
            Map.of("hydration", 1.6, "hunger", 1.2, "energy", 1.1),
            Map.of("hydration", 40, "food", 10, "rare", 5),
            "Piach i skwar. Każdy łyk wody jest na wagę złota."
    ),
    MOUNTAINS(
            "Góry",
            "fas-mountain",
            Map.of("energy", 1.5, "hunger", 1.3, "hydration", 0.9),
            Map.of("energy", 30, "food", 15, "move", -10),
            "Kamienne przełęcze. Nogi odmawiają posłuszeństwa, ale widok zapiera dech."
    ),
    FOREST(
            "Las",
            "fas-tree",
            Map.of("hydration", 0.8, "hunger", 1.0, "energy", 1.1),
            Map.of("food", 20, "morale", 10, "rare", 10),
            "Gęsty las. Więcej jedzenia, mniej słońca. Nadzieja rośnie."
    ),
    PLAINS(
            "Równiny",
            "fas-seedling",
            Map.of("hydration", 1.0, "hunger", 1.0, "energy", 0.9),
            Map.of("move", 20, "morale", 15),
            "Otwarte równiny. Czas przyspieszyć kroku."
    );

    // ── Pola ──────────────────────────────────────────────────────────────────

    private final String label;
    private final String emoji;
    /** stat → mnożnik decay dla danego statu (dotyczy ujemnych delt hunger/hydration/energy). */
    private final Map<String, Double> decayMultipliers;
    /** Modyfikatory wag puli eventów (te same klucze co TraitManager). */
    private final Map<String, Integer> eventWeightMods;
    private final String               entryMessage;

    // ── Konstruktor ───────────────────────────────────────────────────────────

    Biome(String label, String emoji,
          Map<String, Double> decayMultipliers,
          Map<String, Integer> eventWeightMods,
          String entryMessage) {
        this.label              = label;
        this.emoji              = emoji;
        this.decayMultipliers   = decayMultipliers;
        this.eventWeightMods    = eventWeightMods;
        this.entryMessage       = entryMessage;
    }

    // ── Gettery ───────────────────────────────────────────────────────────────

    public String getLabel()              { return label; }
    public String getEmoji()              { return emoji; }
    public Map<String, Double>   getDecayMultipliers() { return decayMultipliers; }
    public Map<String, Integer>  getEventWeightMods()  { return eventWeightMods; }
    public String getEntryMessage()       { return entryMessage; }

    /**
     * Zwraca mnożnik decay dla danego statu.
     * Domyślnie 1.0 (brak modyfikacji) gdy stat nie ma wpisu.
     */
    public double getDecayMultiplier(String stat) {
        return decayMultipliers.getOrDefault(stat, 1.0);
    }

    // ── Losowanie nowego biomu ─────────────────────────────────────────────
    public static Biome rollNext(Biome current, Random rng) {
        Biome[] allBiomes = values();
        Biome next;
        do {
            next = allBiomes[rng.nextInt(allBiomes.length)];
        } while (next == current);
        return next;
    }
}