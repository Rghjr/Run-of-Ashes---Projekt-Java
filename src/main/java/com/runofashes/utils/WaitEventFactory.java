package com.runofashes.utils;

import com.runofashes.model.GameEvent;

import java.util.Map;

/**
 * Tworzy syntetyczny GameEvent reprezentujący akcję "przeczekaj turę przy queście".
 * Ten event nigdy nie pochodzi z JSON — jest budowany dynamicznie przez GameEngine
 * gdy aktywny quest ma flagę allowWait=true i jeszcze nie jest gotowy.
 */
public class WaitEventFactory {

    public static GameEvent create(int turnsLeft) {
        return new WaitGameEvent(turnsLeft);
    }

    // ── Wewnętrzna implementacja ──────────────────────────────────────────────

    private static class WaitGameEvent extends GameEvent {

        private final int turnsLeft;

        WaitGameEvent(int turnsLeft) {
            this.turnsLeft = turnsLeft;
        }

        @Override public String getId()        { return "WAIT_TURN"; }

        @Override public String getLabel() {
            return "Przeczekaj turę w pobliżu questa ("
                    + turnsLeft + (turnsLeft == 1 ? " tura" : " tury/tur") + ")";
        }

        @Override public int    getTimeCost()     { return 2; }
        @Override public int    getDistanceCost() { return 0; }
        @Override public String getCategory()     { return "wait"; }

        @Override public Map<String, Integer> getEffects() {
            // Małe koszty za siedzenie w miejscu
            return Map.of("hunger", -6, "hydration", -4, "energy", -5);
        }

        @Override public String getSuccessMessage() {
            return "Czekasz przy queście. Czas mija powoli, ale quest jest coraz bliżej.";
        }

        @Override public Map<String, Integer> getFailEffects() { return Map.of(); }
        @Override public double getFailChance()                 { return 0.0; }
        @Override public boolean isHiddenEffects()              { return false; }

        @Override public String buildEffectsString() {
            return "-6 🍗  -4 💧  -5 ⚡";
        }
    }
}