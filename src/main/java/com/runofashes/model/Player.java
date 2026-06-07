package com.runofashes.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Player {

    private int health    = 100;
    private int hunger    = 100;
    private int hydration = 100;
    private int energy    = 100;
    private int morale    = 100;
    private int time      = 0;
    private int distance  = 4000;

    /** Absolutny sufit — żaden stat nie może przekroczyć tej wartości niezależnie od bonusów. */
    public static final int ABSOLUTE_MAX = 130;

    // Per-stat maksima — obliczane przez initMaxStats() na podstawie trudności + cech.
    // Domyślnie 100; GameEngine wywołuje initMaxStats() przed startem gry.
    private int maxHealth    = 100;
    private int maxHunger    = 100;
    private int maxHydration = 100;
    private int maxEnergy    = 100;
    private int maxMorale    = 100;

    // ── Inicjalizacja ─────────────────────────────────────────────────────────

    /**
     * Oblicza per-stat maksima na podstawie poziomu trudności i wybranych cech.
     * Musi być wywołane raz przed applyStartBonuses() i startem gry.
     *
     * max[stat] = clamp(100 + diffBonus + sum(traitStartBonus[stat]), 1, ABSOLUTE_MAX)
     */
    public void initMaxStats(Difficulty difficulty, Collection<Trait> traits) {
        int diffBonus = difficulty.getStartStatBonus();

        Map<String, Integer> traitBonus = new HashMap<>();
        for (Trait t : traits) {
            t.getStartBonus().forEach((stat, val) ->
                    traitBonus.merge(stat, val, Integer::sum));
        }

        maxHealth    = computeMax(diffBonus + traitBonus.getOrDefault("health",    0));
        maxHunger    = computeMax(diffBonus + traitBonus.getOrDefault("hunger",    0));
        maxHydration = computeMax(diffBonus + traitBonus.getOrDefault("hydration", 0));
        maxEnergy    = computeMax(diffBonus + traitBonus.getOrDefault("energy",    0));
        maxMorale    = computeMax(diffBonus + traitBonus.getOrDefault("morale",    0));
    }

    private int computeMax(int bonus) {
        return Math.max(1, Math.min(ABSOLUTE_MAX, 100 + bonus));
    }

    private int clamp(int value, int max) {
        return Math.max(0, Math.min(max, value));
    }

    // ── Gettery maksimów ──────────────────────────────────────────────────────

    public int getMaxHealth()    { return maxHealth; }
    public int getMaxHunger()    { return maxHunger; }
    public int getMaxHydration() { return maxHydration; }
    public int getMaxEnergy()    { return maxEnergy; }
    public int getMaxMorale()    { return maxMorale; }

    // ── Staty ─────────────────────────────────────────────────────────────────

    public int getHealth()           { return health; }
    public void setHealth(int v)     { health    = clamp(v, maxHealth); }

    public int getHunger()           { return hunger; }
    public void setHunger(int v)     { hunger    = clamp(v, maxHunger); }

    public int getHydration()        { return hydration; }
    public void setHydration(int v)  { hydration = clamp(v, maxHydration); }

    public int getEnergy()           { return energy; }
    public void setEnergy(int v)     { energy    = clamp(v, maxEnergy); }

    public int getMorale()           { return morale; }
    public void setMorale(int v)     { morale    = clamp(v, maxMorale); }

    // ── Czas i dystans ────────────────────────────────────────────────────────

    public int getTime()             { return time; }
    public void addTime(int hours)   { time += Math.max(0, hours); }

    public int getDistance()         { return distance; }
    public void addDistance(int km)  { distance = Math.max(0, distance - km); }

    public String getTimeFormatted() {
        int day  = (time / 24) + 1;
        int hour = time % 24;
        return String.format("Dzień %d,  %02d:00", day, hour);
    }

    // ── Stan gracza ───────────────────────────────────────────────────────────

    public boolean hasWon() { return distance <= 0; }

    public String getDeadStat() {
        if (health    <= 0) return "health";
        if (hunger    <= 0) return "hunger";
        if (hydration <= 0) return "hydration";
        if (energy    <= 0) return "energy";
        if (morale    <= 0) return "morale";
        return null;
    }

    public void modifyStat(String stat, int delta) {
        switch (stat) {
            case "health"    -> setHealth(getHealth() + delta);
            case "hunger"    -> setHunger(getHunger() + delta);
            case "hydration" -> setHydration(getHydration() + delta);
            case "energy"    -> setEnergy(getEnergy() + delta);
            case "morale"    -> setMorale(getMorale() + delta);
        }
    }

    public int getStat(String stat) {
        return switch (stat) {
            case "health"    -> getHealth();
            case "hunger"    -> getHunger();
            case "hydration" -> getHydration();
            case "energy"    -> getEnergy();
            case "morale"    -> getMorale();
            default          -> 100;
        };
    }

    public void loadFromState(GameState state) {
        this.health = state.health;
        this.hunger = state.hunger;
        this.hydration = state.hydration;
        this.energy = state.energy;
        this.morale = state.morale;
        this.time = state.time;
        this.distance = state.distance;
    }
}