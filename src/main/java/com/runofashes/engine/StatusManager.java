package com.runofashes.engine;

import com.runofashes.model.Player;
import com.runofashes.model.StatusEffect;

import java.util.*;

public class StatusManager {

    private final Map<StatusEffect, Integer> activeStatuses = new EnumMap<>(StatusEffect.class);

    private final Map<Integer, Map<String, Integer>> delayedEffects = new TreeMap<>();

    private StatusEffect lastTriggered = null;

    private static final Random RNG = new Random();

    // ── Publiczne API ─────────────────────────────────────────────────────────

    public void tick(Player player, int currentTurn) {
        // Statusy per-tura
        Iterator<Map.Entry<StatusEffect, Integer>> it = activeStatuses.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<StatusEffect, Integer> entry = it.next();
            applyEffects(player, entry.getKey().getPerTurnEffects());
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) it.remove();
            else entry.setValue(remaining);
        }

        // Opóźnione efekty itemów
        Map<String, Integer> due = delayedEffects.remove(currentTurn);
        if (due != null) applyEffects(player, due);
    }

    public boolean rollTriggers(Player player) {
        lastTriggered = null;

        for (StatusEffect se : StatusEffect.values()) {
            if (activeStatuses.containsKey(se)) continue; // już aktywny

            double chance = baseChance(se, player);
            if (chance <= 0) continue;

            if (RNG.nextDouble() < chance) {
                activate(se);
                lastTriggered = se;
                return true; // max jeden na turę
            }
        }
        return false;
    }

    public void activate(StatusEffect se) {
        activeStatuses.put(se, se.getDefaultDuration());
    }

    public void addDelayedEffect(Map<String, Integer> effects, int currentTurn, int turnsDelay) {
        int targetTurn = currentTurn + turnsDelay;
        delayedEffects.merge(targetTurn, new HashMap<>(effects), (existing, incoming) -> {
            Map<String, Integer> merged = new HashMap<>(existing);
            incoming.forEach((k, v) -> merged.merge(k, v, Integer::sum));
            return merged;
        });
    }

    // ── Gettery ───────────────────────────────────────────────────────────────

    public boolean isActive(StatusEffect se)                { return activeStatuses.containsKey(se); }
    public Map<StatusEffect, Integer> getActiveStatuses()   { return Collections.unmodifiableMap(activeStatuses); }
    public StatusEffect getLastTriggered()                  { return lastTriggered; }

    public boolean hasHallucinations()                      { return isActive(StatusEffect.HALLUCINATIONS); }

    // ── Prywatne helpers ──────────────────────────────────────────────────────

    private double baseChance(StatusEffect se, Player player) {
        if (!se.hasStatTrigger()) {
            // Pozytywne statusy losowe — niska, stała szansa
            return 0.03;
        }

        int statValue = player.getStat(se.getTriggerStat());
        if (statValue > se.getTriggerThreshold()) return 0.0;

        // Im niższy stat poniżej progu, tym wyższa szansa (max 40%)
        double ratio = 1.0 - (double) statValue / se.getTriggerThreshold();
        return ratio * 0.40;
    }

    private void applyEffects(Player player, Map<String, Integer> effects) {
        if (effects != null) {
            effects.forEach(player::modifyStat);
        }
    }
}