package com.runofashes;

import java.util.Map;

public enum StatusEffect {

    DEHYDRATION(
            "Odwodnienie",
            "🏜️",
            "Szybszy spadek wszystkich statów.",
            Map.of("hydration", -5, "energy", -3, "health", -2),
            3,
            "hydration",   // triggerStat — aktywuje się gdy ten stat jest niski
            15             // triggerThreshold — poniżej tej wartości
    ),
    FEVER(
            "Gorączka",
            "🤒",
            "Ciągły spadek zdrowia i energii.",
            Map.of("health", -8, "energy", -5, "morale", -3),
            4,
            "health",
            20
    ),
    CRAMPS(
            "Skurcze",
            "⚡",
            "Akcje kosztują więcej energii.",
            Map.of("energy", -6, "hunger", -4),
            2,
            "energy",
            15
    ),
    ADRENALINE(
            "Adrenalina",
            "💨",
            "Lepsza skuteczność akcji przez 2 tury.",
            Map.of("energy", 3, "morale", 5),
            2,
            null,          // tylko losowy trigger, nie statowy
            0
    ),
    HALLUCINATIONS(
            "Halucynacje",
            "👁️",
            "Losowe efekty akcji przez kilka tur.",
            Map.of("morale", -6, "energy", -3),
            3,
            "morale",
            10
    ),
    SECOND_WIND(
            "Drugi oddech",
            "🌬️",
            "Chwilowy boost energii i morale.",
            Map.of("energy", 8, "morale", 5),
            2,
            null,
            0
    );

    // ── Pola ──────────────────────────────────────────────────────────────────

    private final String label;
    private final String emoji;
    private final String description;

    private final Map<String, Integer> perTurnEffects;

    private final int defaultDuration;

    private final String triggerStat;

    private final int triggerThreshold;

    // ── Konstruktor ───────────────────────────────────────────────────────────

    StatusEffect(String label,
                 String emoji,
                 String description,
                 Map<String, Integer> perTurnEffects,
                 int defaultDuration,
                 String triggerStat,
                 int triggerThreshold) {
        this.label            = label;
        this.emoji            = emoji;
        this.description      = description;
        this.perTurnEffects   = perTurnEffects;
        this.defaultDuration  = defaultDuration;
        this.triggerStat      = triggerStat;
        this.triggerThreshold = triggerThreshold;
    }

    // ── Gettery ───────────────────────────────────────────────────────────────

    public String getLabel()                        { return label; }
    public String getEmoji()                        { return emoji; }
    public String getDescription()                  { return description; }
    public Map<String, Integer> getPerTurnEffects() { return perTurnEffects; }
    public int    getDefaultDuration()              { return defaultDuration; }
    public String getTriggerStat()                  { return triggerStat; }
    public int    getTriggerThreshold()             { return triggerThreshold; }
    public boolean hasStatTrigger()                 { return triggerStat != null; }
}