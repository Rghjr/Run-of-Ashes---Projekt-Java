package com.runofashes.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameState {
    public RunStatistics stats;
    public int health, hunger, hydration, energy, morale;
    public int time, distance;
    public List<String> activeTraitNames;
    public Map<ItemType, Integer> inventoryItems;
    public Map<String, QuestState> activeQuests;
    public Set<String> completedQuestIds;
    public Set<String> unlockedAchievementIds;
    public int consecutiveMoves;

    public GameState() {
    }
}