package com.runofashes;

import java.util.Map;

public class GameEvent {

    private String id;
    private String label;
    private int timeCost     = 1;
    private int distanceCost = 0;   // km odejmowane od dystansu (tylko eventy ruchu)

    private Map<String, Integer> effects;
    private String successMessage;
    private Map<String, Integer> itemEffects;

    private double failChance = 0.0;
    private Map<String, Integer> failEffects;
    private String failMessage;

    private String questId;
    private int questStage;
    private int turnsUntilNext;
    private boolean localQuest;

    private boolean hiddenEffects;
    private String revealMessage;

    // kategoria do ważenia losowania
    private String category;  // food | hydration | energy | morale | move | quest | rare

    // ograniczenie pory dnia (0-23); domyślnie cały dzień
    private final int minHour = 0;
    private final int maxHour = 23;

    public String getId()                        { return id; }
    public String getLabel()                     { return label; }
    public int getTimeCost()                     { return timeCost; }
    public int getDistanceCost()                 { return distanceCost; }
    public String getCategory()                  { return category; }

    public Map<String, Integer> getEffects()     { return effects; }
    public String getSuccessMessage()            { return successMessage; }
    public Map<String, Integer> getItemEffects() { return itemEffects; }

    public double getFailChance()                { return failChance; }
    public Map<String, Integer> getFailEffects() { return failEffects; }
    public String getFailMessage()               { return failMessage; }

    public String getQuestId()                   { return questId; }
    public int getQuestStage()                   { return questStage; }
    public int getTurnsUntilNext()               { return turnsUntilNext; }
    public boolean isLocalQuest()                { return localQuest; }

    public boolean isHiddenEffects()             { return hiddenEffects; }
    public String getRevealMessage()             { return revealMessage; }
    public int getMinHour()                      { return minHour; }
    public int getMaxHour()                      { return maxHour; }

    /** True jeśli event może się pojawić o danej godzinie (obsługuje przejście przez północ). */
    public boolean isAvailableAt(int hour) {
        if (minHour <= maxHour) return hour >= minHour && hour <= maxHour;
        return hour >= minHour || hour <= maxHour; // np. 20–4 (nocne)
    }

    /** Buduje czytelny string efektów do wyświetlenia na kafelku, np. "+30 🍗  -10 ⚡" */
    public String buildEffectsString() {
        if (hiddenEffects || effects == null || effects.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        effects.forEach((stat, val) -> {
            if (val == 0) return;
            sb.append(val > 0 ? "+" : "").append(val).append(" ").append(statLabel(stat)).append("  ");
        });
        return sb.toString().trim();
    }

    private static String statLabel(String stat) {
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