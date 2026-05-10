package com.runofashes;

import java.util.Map;

public enum ItemType {

    WATER(
            "Woda",
            "💧",
            5,
            Map.of("hydration", 40),
            null,
            0
    ),
    WINE(
            "Wino",
            "🍷",
            3,
            Map.of("hydration", 20, "morale", 25),
            Map.of("energy", -10),   // kac — następna akcja droższa
            1
    ),
    OLIVES(
            "Oliwki",
            "🫒",
            4,
            Map.of("hunger", 30),
            Map.of("energy", -8),    // ciężkostrawne
            1
    ),
    GRAPES(
            "Winogrona",
            "🍇",
            4,
            Map.of("hunger", 20, "hydration", 10),
            null,
            0
    ),
    HERBS(
            "Zioła",
            "🌿",
            3,
            Map.of("health", 5),
            Map.of("health", 10),    // leczenie przez kilka tur (symulowane jako delayed)
            2
    ),
    BANDAGE(
            "Bandaże",
            "🩹",
            3,
            Map.of("health", 30),
            null,
            0
    ),
    DRIED_MEAT(
            "Suszone mięso",
            "🥩",
            5,
            Map.of("hunger", 45, "hydration", -10),
            null,
            0
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