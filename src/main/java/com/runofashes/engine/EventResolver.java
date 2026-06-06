package com.runofashes.engine;

import com.runofashes.model.Difficulty;
import com.runofashes.model.EventChoice;
import com.runofashes.model.GameEvent;
import com.runofashes.model.Player;

import java.util.Random;

public class EventResolver {

    private final Random rng;

    public EventResolver(Random rng) {
        this.rng = rng;
    }

    public EventResult resolve(GameEvent event, Player player, TraitManager traitManager, Difficulty difficulty) {
        double penalty = statPenalty(player.getEnergy())    * 0.50
                + statPenalty(player.getHunger())    * 0.25
                + statPenalty(player.getHydration()) * 0.25
                + statPenalty(player.getHealth())    * 0.15
                + statPenalty(player.getMorale())    * 0.10;
        penalty = Math.min(1.0, penalty);

        double successThreshold = 0.25 + penalty * 0.30;
        double partialThreshold = 0.05 + penalty * 0.15;

        double mod = traitManager.getSuccessMod() + difficulty.getSuccessBonus();
        successThreshold = Math.max(0.05, successThreshold - mod);
        partialThreshold = Math.max(0.01, partialThreshold - mod);

        if (event.getFailChance() > 0) {
            partialThreshold = Math.min(successThreshold - 0.01,
                    partialThreshold + event.getFailChance());
        }

        double roll = rng.nextDouble();
        if (roll >= successThreshold) return EventResult.SUCCESS;
        if (roll >= partialThreshold) return EventResult.PARTIAL;
        return EventResult.FAIL;
    }

    private static double statPenalty(int statValue) {
        if (statValue >= 30) return 0.0;
        return 1.0 - statValue / 30.0;
    }

    /**
     * Szansa powodzenia danej opcji wyboru (0.0–1.0), zależna od statystyki gracza,
     * cech oraz poziomu trudności. Deterministyczna — można jej użyć do wyświetlenia
     * graczowi procentu szansy.
     */
    public double choiceChance(EventChoice choice, Player player,
                               TraitManager traitManager, Difficulty difficulty) {
        double chance = choice.getBaseChance();
        if (choice.getStat() != null) {
            chance += choice.getStatInfluence() * (player.getStat(choice.getStat()) / 100.0);
        }
        chance += traitManager.getSuccessMod() + difficulty.getSuccessBonus();
        return Math.max(0.02, Math.min(0.98, chance));
    }

    /** Rozstrzyga opcję wyboru: true = sukces, false = porażka (brak stanu pośredniego). */
    public boolean resolveChoice(EventChoice choice, Player player,
                                 TraitManager traitManager, Difficulty difficulty) {
        return rng.nextDouble() < choiceChance(choice, player, traitManager, difficulty);
    }
}
