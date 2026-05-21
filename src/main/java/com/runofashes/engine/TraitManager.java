package com.runofashes.engine;

import com.runofashes.model.Player;
import com.runofashes.model.Trait;

import java.util.*;

/**
 * Zarządza cechami aktywnej rozgrywki.
 * Odpowiada za:
 *  - aplikowanie bonusów startowych na Player
 *  - per-turowy tick modyfikatorów statów
 *  - eksponowanie modyfikatorów wag dla GameEngine.buildWeightedPool()
 *  - eksponowanie sumarycznego successMod dla GameEngine.resolveResult()
 */
public class TraitManager {

    private final List<Trait> activeTraits = new ArrayList<>();

    // ─── API ─────────────────────────────────────────────────────────────────

    public void setTraits(Collection<Trait> traits) {
        activeTraits.clear();
        activeTraits.addAll(traits);
    }

    public List<Trait> getActiveTraits() {
        return Collections.unmodifiableList(activeTraits);
    }

    /** Aplikuje startowe bonusy na gracza. Wywołać raz po reset(). */
    public void applyStartBonuses(Player player) {
        for (Trait t : activeTraits) {
            t.getStartBonus().forEach(player::modifyStat);
        }
    }

    /**
     * Tick per-turowych modyfikatorów statów.
     * Wywołać po executeEvent(), razem z StatusManager.tick().
     */
    public void tick(Player player) {
        for (Trait t : activeTraits) {
            t.getPerTurnMods().forEach(player::modifyStat);
        }
    }

    /**
     * Zwraca sumaryczny modyfikator wagi dla danej kategorii eventów.
     * GameEngine dodaje tę wartość do bazowej wagi.
     */
    public int getWeightMod(String category) {
        int sum = 0;
        for (Trait t : activeTraits) {
            sum += t.getWeightMods().getOrDefault(category, 0);
        }
        return sum;
    }

    /**
     * Zwraca sumaryczny modyfikator szansy sukcesu.
     * Zakres bezpieczny: suma nie powinna przekraczać ±0.25.
     */
    public double getSuccessMod() {
        double sum = 0.0;
        for (Trait t : activeTraits) {
            sum += t.getSuccessMod();
        }
        return Math.max(-0.25, Math.min(0.25, sum));
    }
}