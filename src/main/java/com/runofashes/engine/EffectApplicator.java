package com.runofashes.engine;

import com.runofashes.model.Biome;
import com.runofashes.model.Difficulty;
import com.runofashes.model.ItemType;
import com.runofashes.model.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EffectApplicator {

    private final Random rng;

    public EffectApplicator(Random rng) {
        this.rng = rng;
    }

    public Map<String, Integer> applyHallucinations(Map<String, Integer> fx, StatusManager statusManager) {
        if (fx == null || !statusManager.hasHallucinations()) return fx;
        Map<String, Integer> hallFx = new HashMap<>(fx);
        hallFx.replaceAll((k, v) -> rng.nextBoolean() ? v : (v > 0 ? -v / 2 : v * 2));
        return hallFx;
    }

    public void applyEffects(Map<String, Integer> fx, Player player, Biome biome, Difficulty difficulty) {
        if (fx == null) return;
        fx.forEach((stat, delta) -> applySingle(stat, delta, player, biome, difficulty));
    }

    public void applyEffectsPartial(Map<String, Integer> fx, Player player, Biome biome, Difficulty difficulty) {
        if (fx == null) return;
        fx.forEach((stat, delta) -> applySingle(stat, delta > 0 ? Math.max(1, delta / 2) : delta, player, biome, difficulty));
    }

    public void processItemEffects(Map<String, Integer> items, boolean isPartial,
                                   Inventory inventory, Player player, Biome biome, Difficulty difficulty) {
        if (items == null) return;
        items.forEach((itemName, amount) -> {
            if (isPartial && !rng.nextBoolean()) return;
            try {
                ItemType type = ItemType.valueOf(itemName);
                if (amount > 0) {
                    int added    = inventory.add(type, amount);
                    int overflow = amount - added;
                    if (overflow > 0) {
                        Map<String, Integer> itemFx = type.getImmediateEffects();
                        if (itemFx != null) {
                            for (int i = 0; i < overflow; i++) {
                                applyEffects(itemFx, player, biome, difficulty);
                            }
                        }
                    }
                } else if (amount < 0) {
                    inventory.consume(type, -amount);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Błąd: Nieznany przedmiot w JSON: " + itemName);
            }
        });
    }

    private static void applySingle(String stat, int delta, Player player, Biome biome, Difficulty difficulty) {
        if (delta < 0 && (stat.equals("hunger") || stat.equals("hydration") || stat.equals("energy"))) {
            double biomeMult = biome.getDecayMultiplier(stat);
            double diffMult  = difficulty.getDrainMultiplier();
            if (stat.equals("energy")) {
                delta = (int) Math.round(delta * biomeMult);
            } else {
                delta = (int) Math.round(delta * biomeMult * diffMult);
            }
        }
        player.modifyStat(stat, delta);
    }
}
