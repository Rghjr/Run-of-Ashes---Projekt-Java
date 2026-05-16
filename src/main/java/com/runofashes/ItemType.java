package com.runofashes;

import java.util.Map;

public enum ItemType {

    WATER(
            "Woda",
            "💧",
            1,
            Map.of("hydration", 22),
            null,
            0
    ),
    WINE(
            "Wino",
            "🍷",
            1,
            Map.of("hydration", 10, "morale", 12),
            Map.of("energy", -8),
            1
    ),
    OLIVES(
            "Oliwki",
            "🫒",
            1,
            Map.of("hunger", 16),
            Map.of("energy", -5),
            1
    ),
    GRAPES(
            "Winogrona",
            "🍇",
            1,
            Map.of("hunger", 10, "hydration", 5),
            null,
            0
    ),
    HERBS(
            "Zioła",
            "🌿",
            1,
            Map.of("health", 4),
            Map.of("health", 7),
            2
    ),
    BANDAGE(
            "Bandaże",
            "🩹",
            1,
            Map.of("health", 18),
            null,
            0
    ),
    DRIED_MEAT(
            "Suszone mięso",
            "🥩",
            1,
            Map.of("hunger", 24, "hydration", -8),
            null,
            0
    ),
    TALLOW_CANDLE(
            "Łojowa świeca",
            "🕯",
            1,
            Map.of("morale", 8, "energy", 4),
            null,
            0
    ),
    SALT(
            "Sól",
            "🧂",
            1,
            Map.of("hunger", 6),
            Map.of("health", 4),
            1
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