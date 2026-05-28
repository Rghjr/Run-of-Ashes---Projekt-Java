package com.runofashes.engine;

import com.runofashes.model.GameEvent;
import com.runofashes.model.Player;

import java.util.*;
import java.util.stream.Collectors;

public class CardDrawer {

    private final Random rng;
    private final EventPools pools;
    private final QuestTracker quests;
    private final BiomeWeatherController environment;
    private final TraitManager traitManager;

    public CardDrawer(Random rng, EventPools pools, QuestTracker quests,
                      BiomeWeatherController environment, TraitManager traitManager) {
        this.rng          = rng;
        this.pools        = pools;
        this.quests       = quests;
        this.environment  = environment;
        this.traitManager = traitManager;
    }

    public List<GameEvent> draw(Player player, double mainQuestWeight) {
        int currentHour = player.getTime() % 24;

        List<GameEvent> readyContinuations = quests.getReadyContinuations();
        List<GameEvent> availableNewQuests = quests.getAvailableNewQuests();
        List<GameEvent> pool = buildWeightedPool(player, currentHour);

        List<GameEvent> mainQuests = new ArrayList<>();
        List<GameEvent> sideQuests = new ArrayList<>();
        String currentStage = GameStage.nameForDistance(player.getDistance());

        for (GameEvent eq : availableNewQuests) {
            if (eq.getRequiredStage() != null) {
                if (eq.getRequiredStage().equals(currentStage)) {
                    mainQuests.add(eq);
                }
            } else {
                sideQuests.add(eq);
            }
        }
        addWeighted(pool, sideQuests, 6);
        addWeighted(pool, mainQuests, (int) mainQuestWeight);

        Collections.shuffle(pool, rng);

        List<GameEvent> currentCards = new ArrayList<>();
        Set<String> usedIds = new HashSet<>();

        for (GameEvent cont : readyContinuations) {
            if (currentCards.size() >= 4) break;
            currentCards.add(cont);
            usedIds.add(cont.getId());
        }

        GameEvent waitCard = quests.buildWaitCard();
        if (waitCard != null && currentCards.size() < 4) {
            currentCards.add(waitCard);
            usedIds.add(waitCard.getId());
        }

        for (GameEvent e : pool) {
            if (currentCards.size() >= 4) break;
            if (!usedIds.contains(e.getId())) {
                currentCards.add(e);
                usedIds.add(e.getId());
            }
        }
        return currentCards;
    }

    private List<GameEvent> buildWeightedPool(Player player, int hour) {
        List<GameEvent> pool = new ArrayList<>();
        addWeighted(pool, filterByTime(pools.getFoodEvents(), hour),
                weight("food", player.getHunger(), player.getMaxHunger()));
        addWeighted(pool, filterByTime(pools.getHydrationEvents(), hour),
                weight("hydration", player.getHydration(), player.getMaxHydration()));
        addWeighted(pool, filterByTime(pools.getEnergyEvents(), hour),
                weight("energy", player.getEnergy(), player.getMaxEnergy()));
        addWeighted(pool, filterByTime(pools.getMoraleEvents(), hour),
                weight("morale", player.getMorale(), player.getMaxMorale()));
        addWeighted(pool, filterByTime(pools.getMoveEvents(), hour), baseWeight("move", 40));
        addWeighted(pool, pools.getRareEvents(), baseWeight("rare", 5));
        return pool;
    }

    private int weight(String category, int statValue, int maxStat) {
        int base = 10 + (maxStat - statValue);
        int mod  = traitManager.getWeightMod(category) + environment.weightMod(category);
        return Math.max(5, base + mod);
    }

    private int baseWeight(String category, int base) {
        int mod = traitManager.getWeightMod(category) + environment.weightMod(category);
        return Math.max(5, base + mod);
    }

    private static List<GameEvent> filterByTime(List<GameEvent> events, int hour) {
        if (events == null) return List.of();
        return events.stream()
                .filter(e -> e.isAvailableAt(hour))
                .collect(Collectors.toList());
    }

    private static void addWeighted(List<GameEvent> pool, List<GameEvent> events, int weight) {
        if (events == null || events.isEmpty()) return;
        int copies = Math.max(1, weight / 10);
        for (int i = 0; i < copies; i++) pool.addAll(events);
    }
}
