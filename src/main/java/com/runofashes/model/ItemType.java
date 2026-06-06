package com.runofashes.model;

import java.util.Map;

public enum ItemType {

    WATER(
            "Woda",
            "fas-tint",
            1,
            Map.of("hydration", 22),
            null,
            0
    ),
    WINE(
            "Wino",
            "fas-wine-glass",
            1,
            Map.of("hydration", 10, "morale", 12),
            Map.of("energy", -8),
            1
    ),
    OLIVES(
            "Oliwki",
            "fas-lemon",
            1,
            Map.of("hunger", 16),
            Map.of("energy", -5),
            1
    ),
    GRAPES(
            "Winogrona",
            "fas-apple-alt",
            1,
            Map.of("hunger", 10, "hydration", 5),
            null,
            0
    ),
    HERBS(
            "Zioła",
            "fas-leaf",
            1,
            Map.of("health", 4),
            Map.of("health", 7),
            2
    ),
    BANDAGE(
            "Bandaże",
            "fas-band-aid",
            1,
            Map.of("health", 18),
            null,
            0
    ),
    DRIED_MEAT(
            "Suszone mięso",
            "fas-drumstick-bite",
            1,
            Map.of("hunger", 24, "hydration", -8),
            null,
            0
    ),
    TALLOW_CANDLE(
            "Łojowa świeca",
            "fas-fire",
            1,
            Map.of("morale", 8, "energy", 4),
            null,
            0
    ),
    SALT(
            "Sól",
            "fas-cube",
            1,
            Map.of("hunger", 6),
            Map.of("health", 4),
            1
    ),
    // --- UNIKALNE PRZEDMIOTY ---
    MYSTERIOUS_VIAL(
            "Tajemnicza fiolka",
            "fas-flask",
            1,
            Map.of("health", 40, "morale", -15),
            null,
            0
    ),
    HERMIT_BREW(
            "Wywar pustelnika",
            "fas-mug-hot",
            1,
            Map.of("energy", 35, "hydration", -20),
            null,
            0
    ),
    GLOWING_ORE(
            "Świecąca ruda",
            "fas-gem",
            1,
            Map.of("energy", 25, "health", -10, "morale", -10),
            null,
            0
    ),
    STRANGE_FUNGUS(
            "Dziwny grzyb",
            "fas-biohazard",
            1,
            Map.of("hunger", 30, "health", -15),
            Map.of("health", -5),
            2
    ),
    WEIRD_BREAD(
            "Chałko-koń",
            "fas-bread-slice",
            1,
            Map.of("hunger", 30),
            null,
            0
    ),
    MILITARY_STIMPACK(
            "Stymulant bojowy",
            "fas-syringe",
            1,
            Map.of("energy", 50, "morale", 30, "health", -20),
            null,
            0
    ),
    IRON_INGOT(
            "Sztabka żelaza",
            "fas-cubes",
            5,
            null,
            null,
            0
    ),
    SACRED_AMULET(
            "Święty amulet",
            "fas-cross",
            1,
            Map.of("morale", 20),
            null,
            0
    ),
    SHARPENED_BLADE(
            "Naostrzona klinga",
            "fas-slash",
            1,
            Map.of("energy", 10, "morale", 10),
            null,
            0
    ),
    POISONED_WINE(
            "Zatrute wino",
            "fas-skull",
            1,
            Map.of("morale", 40),
            Map.of("health", -40),
            2
    );

    // ── Pola ──────────────────────────────────────────────────────────────────

    private final String label;
    private final String emoji;
    private final int    maxStack;

    private final Map<String, Integer> immediateEffects;

    private final Map<String, Integer> delayedEffects;
    private final int                  delayedTurns;

    // ── Konstruktor ───────────────────────────────────────────────────────────

    ItemType(String label,
             String emoji,
             int maxStack,
             Map<String, Integer> immediateEffects,
             Map<String, Integer> delayedEffects,
             int delayedTurns) {
        this.label            = label;
        this.emoji            = emoji;
        this.maxStack         = maxStack;
        this.immediateEffects = immediateEffects;
        this.delayedEffects   = delayedEffects;
        this.delayedTurns     = delayedTurns;
    }

    // ── Gettery ───────────────────────────────────────────────────────────────

    public String getLabel()                        { return label; }
    public String getEmoji()                        { return emoji; }
    public int    getMaxStack()                     { return maxStack; }
    public Map<String, Integer> getImmediateEffects() { return immediateEffects; }
    public Map<String, Integer> getDelayedEffects()   { return delayedEffects; }
    public int    getDelayedTurns()                 { return delayedTurns; }
    public boolean hasDelayedEffect()               { return delayedEffects != null && delayedTurns > 0; }

    public String buildEffectDescription() {
        StringBuilder sb = new StringBuilder();
        if (immediateEffects != null) {
            immediateEffects.forEach((stat, val) ->
                    sb.append(val > 0 ? "+" : "").append(val).append(" ").append(statEmoji(stat)).append("  "));
        }
        if (delayedEffects != null) {
            sb.append("(po ").append(delayedTurns).append(delayedTurns == 1 ? " turze" : " turach").append(": ");
            delayedEffects.forEach((stat, val) ->
                    sb.append(val > 0 ? "+" : "").append(val).append(" ").append(statEmoji(stat)).append(" "));
            sb.append(")");
        }
        return sb.toString().trim();
    }

    private static String statEmoji(String stat) {
        return switch (stat) {
            case "health"    -> "❤";
            case "hunger"    -> "🍗";
            case "hydration" -> "💧";
            case "energy"    -> "⚡";
            case "morale"    -> "😊";
            default          -> stat;
        };
    }
}