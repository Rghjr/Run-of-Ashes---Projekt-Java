package com.runofashes.model;

import java.util.List;
import java.util.Map;

public class GameEvent {

    private String id;
    private String label;
    private int timeCost     = 1;
    private int distanceCost = 0;
    private String requiredStage;

    private Map<String, Integer> effects;
    private Map<String, Integer> itemEffects;

    /**
     * Opcje decyzyjne. Jeśli niepuste, wydarzenie jest typu "wybór":
     * po kliknięciu karty gracz wybiera jedną z opcji zamiast natychmiastowego
     * rozstrzygnięcia. Każda opcja ma własną szansę sukces/porażka.
     */
    private List<EventChoice> choices;

    private double failChance = 0.0;
    private Map<String, Integer> failEffects;

    private String questId;
    private int questStage;
    private int turnsUntilNext;
    private boolean localQuest;

    private String successMessage;
    private String failMessage;

    private String lowMoraleLabel;
    private String lowMoraleSuccessMessage;
    private String lowMoraleFailMessage;

    /**
     * Jeśli true, w trakcie oczekiwania na kolejny etap questa pojawia się
     * karta "Przeczekaj turę" — gracz może stać w miejscu i nie straci questa.
     * Ustaw w JSON jako "allowWait": true.
     */
    private boolean allowWait;

    private boolean hiddenEffects;
    private String revealMessage;

    private String category;

    private int minHour = 0;
    private int maxHour = 23;

    public String getId()                        { return id; }
    public String getLabel()                     { return label; }
    public int getTimeCost()                     { return timeCost; }
    public int getDistanceCost()                 { return distanceCost; }
    public String getCategory()                  { return category; }
    public String getRequiredStage()             { return requiredStage; }

    public Map<String, Integer> getEffects()     { return effects; }
    public String getSuccessMessage()            { return successMessage; }
    public Map<String, Integer> getItemEffects() { return itemEffects; }

    public List<EventChoice> getChoices()        { return choices; }
    public boolean hasChoices()                  { return choices != null && !choices.isEmpty(); }

    public double getFailChance()                { return failChance; }
    public Map<String, Integer> getFailEffects() { return failEffects; }
    public String getFailMessage()               { return failMessage; }

    public String getQuestId()                   { return questId; }
    public int getQuestStage()                   { return questStage; }
    public int getTurnsUntilNext()               { return turnsUntilNext; }
    public boolean isLocalQuest()                { return localQuest; }
    public boolean isAllowWait()                 { return allowWait; }

    public boolean isHiddenEffects()             { return hiddenEffects; }
    public String getRevealMessage()             { return revealMessage; }

    public String getLowMoraleLabel()           { return lowMoraleLabel; }
    public String getLowMoraleSuccessMessage()  { return lowMoraleSuccessMessage; }
    public String getLowMoraleFailMessage()     { return lowMoraleFailMessage; }

    public boolean isAvailableAt(int hour) {
        if (minHour <= maxHour) return hour >= minHour && hour <= maxHour;
        return hour >= minHour || hour <= maxHour;
    }

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