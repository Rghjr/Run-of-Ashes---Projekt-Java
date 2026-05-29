package com.runofashes;

import com.runofashes.engine.StatusManager;
import com.runofashes.model.Difficulty;
import com.runofashes.model.Player;
import com.runofashes.model.StatusEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class StatusManagerTest {

    /** Zawsze przechodzi roll RNG (< dowolnej dodatniej szansy). */
    private static final class AlwaysHitRandom extends Random {
        @Override
        public double nextDouble() {
            return 0.0;
        }
    }

    private StatusManager manager;
    private Player player;

    @BeforeEach
    public void setUp() {
        manager = new StatusManager();
        player = new Player();
    }

    @Test
    public void testDelayedEffectsAppliedAfterCorrectTurns() {
        manager.addDelayedEffect(Map.of("energy", 20), 5, 2);
        player.setEnergy(50);

        manager.tick(player, 6, Difficulty.NORMAL);
        assertEquals(50, player.getEnergy());

        manager.tick(player, 7, Difficulty.NORMAL);
        assertEquals(70, player.getEnergy());
    }

    @Test
    public void testRollTriggersActivatesDehydration() {
        StatusManager deterministic = new StatusManager(new AlwaysHitRandom());
        player.setHydration(10);

        assertTrue(deterministic.rollTriggers(player));
        assertTrue(deterministic.isActive(StatusEffect.DEHYDRATION));
        assertEquals(StatusEffect.DEHYDRATION, deterministic.getLastTriggered());
    }

    @Test
    public void testStatusPerTurnEffects() {
        manager.activate(StatusEffect.FEVER);
        player.setHealth(100);

        manager.tick(player, 1, Difficulty.NORMAL);

        int expectedHealth = 100 + StatusEffect.FEVER.getPerTurnEffects().getOrDefault("health", 0);
        assertEquals(expectedHealth, player.getHealth());
        assertTrue(manager.isActive(StatusEffect.FEVER));
    }

    @Test
    public void testStatusExpiresAfterDefaultDuration() {
        manager.activate(StatusEffect.FEVER);
        int duration = StatusEffect.FEVER.getDefaultDuration();

        for (int i = 1; i <= duration; i++) {
            assertTrue(manager.isActive(StatusEffect.FEVER),
                    "Gorączka powinna być aktywna w turze " + i);
            manager.tick(player, i, Difficulty.NORMAL);
        }

        assertFalse(manager.isActive(StatusEffect.FEVER),
                "Gorączka powinna wygasnąć po " + duration + " turach");
    }

    @Test
    public void testActivatingAlreadyActiveStatusResetsItsDuration() {
        manager.activate(StatusEffect.CRAMPS);
        manager.tick(player, 1, Difficulty.NORMAL);

        manager.activate(StatusEffect.CRAMPS);

        int turns = manager.getActiveStatuses().get(StatusEffect.CRAMPS);
        assertEquals(StatusEffect.CRAMPS.getDefaultDuration(), turns,
                "Ponowne activate() powinno zresetować czas trwania statusu");
    }

    @Test
    public void testMultipleStatusesCanBeActiveSimultaneously() {
        manager.activate(StatusEffect.FEVER);
        manager.activate(StatusEffect.CRAMPS);
        manager.activate(StatusEffect.ADRENALINE);

        assertTrue(manager.isActive(StatusEffect.FEVER));
        assertTrue(manager.isActive(StatusEffect.CRAMPS));
        assertTrue(manager.isActive(StatusEffect.ADRENALINE));
        assertEquals(3, manager.getActiveStatuses().size(),
                "Wszystkie trzy statusy powinny być aktywne jednocześnie");
    }

    @Test
    public void testHasHallucinations() {
        assertFalse(manager.hasHallucinations(), "Na starcie brak halucynacji");

        manager.activate(StatusEffect.HALLUCINATIONS);
        assertTrue(manager.hasHallucinations(), "Po aktywacji halucynacje są aktywne");

        for (int i = 0; i < StatusEffect.HALLUCINATIONS.getDefaultDuration(); i++) {
            manager.tick(player, i, Difficulty.NORMAL);
        }
        assertFalse(manager.hasHallucinations(), "Halucynacje powinny wygasnąć po czasie");
    }

    @Test
    public void testLastTriggeredIsSetAfterSuccessfulRoll() {
        StatusManager deterministic = new StatusManager(new AlwaysHitRandom());
        player.setHydration(5);

        assertTrue(deterministic.rollTriggers(player));
        assertEquals(StatusEffect.DEHYDRATION, deterministic.getLastTriggered());
    }

    @Test
    public void testConsumeLastTriggeredClearsPendingNotification() {
        StatusManager deterministic = new StatusManager(new AlwaysHitRandom());
        player.setHydration(10);

        deterministic.rollTriggers(player);
        assertEquals(StatusEffect.DEHYDRATION, deterministic.consumeLastTriggered());
        assertNull(deterministic.getLastTriggered());
        assertNull(deterministic.consumeLastTriggered());
    }

    @Test
    public void testRollTriggersSkipsAlreadyActiveStatuses() {
        manager.activate(StatusEffect.FEVER);
        int durationBefore = manager.getActiveStatuses().get(StatusEffect.FEVER);

        player.setHealth(5);
        for (int i = 0; i < 20; i++) {
            manager.rollTriggers(player);
        }

        int durationAfter = manager.getActiveStatuses().get(StatusEffect.FEVER);
        assertEquals(durationBefore, durationAfter,
                "rollTriggers() nie powinien nadpisywać aktywnego statusu");
    }

    @Test
    public void testDelayedEffectsOnSameTurnMerge() {
        player.setEnergy(30);
        player.setHealth(50);

        manager.addDelayedEffect(Map.of("energy", 10), 0, 3);
        manager.addDelayedEffect(Map.of("health", 15), 0, 3);

        manager.tick(player, 3, Difficulty.NORMAL);

        assertEquals(40, player.getEnergy(), "Energia powinna wzrosnąć o 10 w turze 3");
        assertEquals(65, player.getHealth(), "Zdrowie powinno wzrosnąć o 15 w turze 3");
    }
}
