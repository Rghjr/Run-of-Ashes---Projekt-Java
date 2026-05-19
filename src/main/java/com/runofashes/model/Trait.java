package com.runofashes.model;

import java.util.Map;

/**
 * 10 cech postaci: 5 pozytywnych, 5 negatywnych.
 * Każda cecha ma:
 *  - modyfikatory startowe statów (jednorazowe przy inicjacji gracza)
 *  - modyfikatory wag eventów (per kategoria, np. +20 do food = więcej kart jedzenia)
 *  - modyfikatory per-tura na statystyki (np. Asceta traci -2 hunger każdą turę)
 *  - modyfikator szansy sukcesu (od -0.15 do +0.15)
 */
public enum Trait {

    // ═══════════════════════════════════════════════
    //  CECHY POZYTYWNE
    // ═══════════════════════════════════════════════

    HARDY(
            "Zahartowany",
            "💪",
            "Zdrowie +20 na start. Mniej podatny na choroby i urazy.",
            true,
            Map.of("health", 20),
            Map.of(),
            Map.of("health", 1),   // +1 HP per tura
            0.08
    ),

    FORAGER(
            "Zbieracz",
            "🌿",
            "Więcej szans na znalezienie jedzenia i przedmiotów w terenie.",
            true,
            Map.of("hunger", 10),
            Map.of("food", 35, "rare", 15),
            Map.of(),
            0.05
    ),

    PILGRIM(
            "Pielgrzym",
            "✝",
            "Morale +20 na start. Modlitwa i odpoczynek dają więcej.",
            true,
            Map.of("morale", 20),
            Map.of("morale", 30, "energy", 10),
            Map.of("morale", 1),
            0.05
    ),

    WAYFARER(
            "Wędrowiec",
            "👣",
            "Akcje ruchu kosztują mniej energii. Częściej trafiają się okazje podróży.",
            true,
            Map.of("energy", 15),
            Map.of("move", 25),
            Map.of(),
            0.06
    ),

    HERBALIST(
            "Zielarz",
            "🫙",
            "Zioła i bandaże leczą więcej. Częstsze eventy ze źródłami wody.",
            true,
            Map.of(),
            Map.of("hydration", 20, "food", 10),
            Map.of("health", 2),
            0.04
    ),

    // ═══════════════════════════════════════════════
    //  CECHY NEGATYWNE
    // ═══════════════════════════════════════════════

    SICKLY(
            "Chorowity",
            "🤒",
            "Zdrowie -15 na start. Statusy chorobowe trwają dłużej.",
            false,
            Map.of("health", -15),
            Map.of(),
            Map.of("health", -1),   // -1 HP per tura
            -0.08
    ),

    GLUTTON(
            "Żarłok",
            "🍖",
            "Głód spada 2x szybciej. Potrzebujesz więcej jedzenia by przeżyć.",
            false,
            Map.of("hunger", -10),
            Map.of("food", 20),     // więcej kart jedzenia bo potrzeba
            Map.of("hunger", -2),   // -2 hunger per tura
            -0.05
    ),

    MELANCHOLIC(
            "Melancholik",
            "😔",
            "Morale -20 na start. Sukcesy dają mniej radości.",
            false,
            Map.of("morale", -20),
            Map.of("morale", -20),
            Map.of("morale", -1),
            -0.05
    ),

    CLUMSY(
            "Niezdarny",
            "🪨",
            "Wyższe ryzyko niepowodzenia akcji. -10% szans na sukces.",
            false,
            Map.of(),
            Map.of(),
            Map.of(),
            -0.10
    ),

    PARCHED(
            "Suchy",
            "🏜",
            "Nawodnienie spada 2x szybciej. Pragnienie zagraża szybciej.",
            false,
            Map.of("hydration", -15),
            Map.of("hydration", 25),  // więcej kart wody
            Map.of("hydration", -2),  // -2 hydration per tura
            -0.06
    );

    // ─── Pola ────────────────────────────────────────────────────────────────

    private final String  label;
    private final String  emoji;
    private final String  description;
    private final boolean positive;

    /** Jednorazowe modyfikatory startowe (health, hunger itp.) */
    private final Map<String, Integer> startBonus;

    /**
     * Modyfikatory wag kategorii eventów.
     * Klucze: "food" | "hydration" | "energy" | "morale" | "move" | "quest" | "rare"
     * Wartości: dodatnia = więcej eventów tej kategorii, ujemna = mniej
     */
    private final Map<String, Integer> weightMods;

    /** Modyfikatory statów per tura (tick każdą turę po resolveResult). */
    private final Map<String, Integer> perTurnMods;

    /**
     * Modyfikator szansy sukcesu dodawany do resolveResult.
     * Zakres: -0.15 .. +0.15
     */
    private final double successMod;

    // ─── Konstruktor ─────────────────────────────────────────────────────────

    Trait(String label, String emoji, String description, boolean positive,
          Map<String, Integer> startBonus, Map<String, Integer> weightMods,
          Map<String, Integer> perTurnMods, double successMod) {
        this.label       = label;
        this.emoji       = emoji;
        this.description = description;
        this.positive    = positive;
        this.startBonus  = startBonus;
        this.weightMods  = weightMods;
        this.perTurnMods = perTurnMods;
        this.successMod  = successMod;
    }

    // ─── Gettery ─────────────────────────────────────────────────────────────

    public String  getLabel()                        { return label; }
    public String  getEmoji()                        { return emoji; }
    public String  getDescription()                  { return description; }
    public boolean isPositive()                      { return positive; }
    public Map<String, Integer> getStartBonus()      { return startBonus; }
    public Map<String, Integer> getWeightMods()      { return weightMods; }
    public Map<String, Integer> getPerTurnMods()     { return perTurnMods; }
    public double  getSuccessMod()                   { return successMod; }
}