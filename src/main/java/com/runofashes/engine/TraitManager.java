package com.runofashes.engine;

import com.runofashes.model.Difficulty;
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
    public void tick(Player player, Difficulty difficulty) {
        for (Trait t : activeTraits) {
            t.getPerTurnMods().forEach((stat, delta) -> applyStat(player, stat, delta, difficulty));
        }
    }

    private void applyStat(Player player, String stat, int delta, Difficulty difficulty) {
        if (delta < 0 && (stat.equals("hunger") || stat.equals("hydration"))) {
            delta = (int) Math.round(delta * difficulty.getDrainMultiplier());
        }
        player.modifyStat(stat, delta);
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