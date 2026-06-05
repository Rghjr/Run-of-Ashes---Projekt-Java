package com.runofashes.engine;

import com.runofashes.model.ItemType;
import com.runofashes.model.Player;

import java.util.*;

public class Inventory {

    private final Map<ItemType, Integer> items = new EnumMap<>(ItemType.class);

    // ── Dodawanie itemów ──────────────────────────────────────────────────────

    public int add(ItemType type, int amount) {
        if (amount <= 0) return 0;
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

    // ── Zabieranie itemów (ujemne itemEffects w JSON) ─────────────────────────

    /**
     * Zabiera {@code amount} sztuk przedmiotu z ekwipunku.
     * Jeśli gracz ma mniej niż {@code amount}, zabiera wszystko co ma.
     *
     * @return ile faktycznie zabrano (0 jeśli gracz nie miał nic)
     */
    public int consume(ItemType type, int amount) {
        if (amount <= 0) return 0;
        int current = items.getOrDefault(type, 0);
        if (current <= 0) return 0;
        int toRemove = Math.min(amount, current);
        int remaining = current - toRemove;
        if (remaining == 0) items.remove(type);
        else                items.put(type, remaining);
        return toRemove;
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

    public void loadFromMap(Map<ItemType, Integer> itemsMap) {
        this.items.clear();
        this.items.putAll(itemsMap);
    }

    // ── Prywatne helpers ──────────────────────────────────────────────────────

    private void applyEffects(Player player, Map<String, Integer> effects) {
        if (effects != null) {
            effects.forEach(player::modifyStat);
        }
    }
}