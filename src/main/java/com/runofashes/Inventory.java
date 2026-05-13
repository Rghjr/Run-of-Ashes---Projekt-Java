package com.runofashes;

import java.util.*;

public class Inventory {

    private final Map<ItemType, Integer> items = new EnumMap<>(ItemType.class);

    // ── Dodawanie itemów ──────────────────────────────────────────────────────

    public int add(ItemType type, int amount) {
        int current = items.getOrDefault(type, 0);
        int canAdd  = type.getMaxStack() - current;
        if (canAdd <= 0) return 0;
        int toAdd = Math.min(amount, canAdd);
        items.put(type, current + toAdd);
        return toAdd;
    }

    public boolean add(ItemType type) {
        return add(type, 1) > 0;
    }

    // ── Używanie itemów ───────────────────────────────────────────────────────

    public boolean useItem(ItemType type, Player player, StatusManager statusManager, int currentTurn) {
        int count = items.getOrDefault(type, 0);
        if (count <= 0) return false;

        if (count == 1) items.remove(type);
        else items.put(type, count - 1);

        applyEffects(player, type.getImmediateEffects());

        if (type.hasDelayedEffect()) {
            statusManager.addDelayedEffect(type.getDelayedEffects(), currentTurn, type.getDelayedTurns());
        }

        return true;
    }

    public void clear() { items.clear(); }

    // ── Odpytywanie stanu ─────────────────────────────────────────────────────

    public int getCount(ItemType type) {
        return items.getOrDefault(type, 0);
    }

    public boolean has(ItemType type) {
        return getCount(type) > 0;
    }

    public boolean isFull(ItemType type) {
        return getCount(type) >= type.getMaxStack();
    }

    public Map<ItemType, Integer> getAllItems() {
        return Collections.unmodifiableMap(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    // ── Prywatne helpers ──────────────────────────────────────────────────────

    private void applyEffects(Player player, Map<String, Integer> effects) {
        if (effects == null) return;
        effects.forEach((stat, delta) -> {
            switch (stat) {
                case "health"    -> player.setHealth(player.getHealth()       + delta);
                case "hunger"    -> player.setHunger(player.getHunger()       + delta);
                case "hydration" -> player.setHydration(player.getHydration() + delta);
                case "energy"    -> player.setEnergy(player.getEnergy()       + delta);
                case "morale"    -> player.setMorale(player.getMorale()       + delta);
            }
        });
    }
}