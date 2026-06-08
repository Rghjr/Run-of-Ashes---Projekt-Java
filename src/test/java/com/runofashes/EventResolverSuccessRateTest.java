package com.runofashes;

import com.runofashes.engine.EventResolver;
import com.runofashes.engine.TraitManager;
import com.runofashes.engine.EventResult;
import com.runofashes.model.Difficulty;
import com.runofashes.model.GameEvent;
import com.runofashes.model.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

public class EventResolverSuccessRateTest {

    @Test
    public void fullStatsShouldGiveApprox80_15_5Distribution() {
        Random rnd = new Random(98765);
        EventResolver resolver = new EventResolver(rnd);
        TraitManager traitManager = new TraitManager();
        Difficulty difficulty = Difficulty.NORMAL;

        Player dummyPlayer = new Player();

        dummyPlayer.setEnergy(100);
        dummyPlayer.setHunger(100);
        dummyPlayer.setHydration(100);
        dummyPlayer.setHealth(100);
        dummyPlayer.setMorale(100);

        GameEvent dummyEvent = new GameEvent();
        dummyEvent.setFailChance(0.0);

        int trials = 10000;
        int s = 0, p = 0, f = 0;

        for (int i = 0; i < trials; i++) {
            EventResult result = resolver.resolve(dummyEvent, dummyPlayer, traitManager, difficulty);
            switch (result) {
                case SUCCESS -> s++;
                case PARTIAL -> p++;
                case FAIL    -> f++;
            }
        }

        double pS = s / (double) trials;
        double pP = p / (double) trials;
        double pF = f / (double) trials;

        assertTrue(Math.abs(pS - 0.65) < 0.05, "Oczekiwano SUCCESS ~65%, jest: " + (pS * 100) + "%");
        assertTrue(Math.abs(pP - 0.25) < 0.05, "Oczekiwano PARTIAL ~25%, jest: " + (pP * 100) + "%");
        assertTrue(Math.abs(pF - 0.10) < 0.05, "Oczekiwano FAIL ~10%, jest: " + (pF * 100) + "%");
    }
}