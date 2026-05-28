package com.runofashes;

import com.runofashes.engine.StatusManager;
import com.runofashes.model.Difficulty;
import com.runofashes.model.Player;
import com.runofashes.model.StatusEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class StatusManagerTest {

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
        player.setHydration(10);
        for (int i = 0; i < 50; i++) {
            manager.rollTriggers(player);
            if (manager.isActive(StatusEffect.DEHYDRATION)) break;
        }
        assertTrue(manager.isActive(StatusEffect.DEHYDRATION));
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

    // ── Nowe testy ────────────────────────────────────────────────────────────

    /**
     * Status wygasa po upływie defaultDuration tur.
     * FEVER ma defaultDuration=4 — powinien zniknąć po 4 tickach.
     */
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

    /**
     * Wywołanie activate() dla już aktywnego statusu resetuje jego czas trwania.
     * Ponowna aktywacja nie stackuje czasu — zastępuje go.
     */
    @Test
    public void testActivatingAlreadyActiveStatusResetsItsDuration() {
        manager.activate(StatusEffect.CRAMPS);
        manager.tick(player, 1, Difficulty.NORMAL); // turnsLeft = 1

        manager.activate(StatusEffect.CRAMPS); // reset do defaultDuration

        int turns = manager.getActiveStatuses().get(StatusEffect.CRAMPS);
        assertEquals(StatusEffect.CRAMPS.getDefaultDuration(), turns,
                "Ponowne activate() powinno zresetować czas trwania statusu");
    }

    /**
     * Kilka statusów może być aktywnych jednocześnie — nie blokują się nawzajem.
     * Każdy tick odlicza czas wszystkich aktywnych.
     */
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

    /**
     * hasHallucinations() zwraca true tylko gdy HALLUCINATIONS jest aktywny,
     * i false po jego wygaśnięciu. Używane przez applyHallucinations() w GameEngine.
     */
    @Test
    public void testHasHallucinations() {
        assertFalse(manager.hasHallucinations(), "Na starcie brak halucynacji");

        manager.activate(StatusEffect.HALLUCINATIONS);
        assertTrue(manager.hasHallucinations(), "Po aktywacji halucynacje są aktywne");

        // Tick tyle razy ile wynosi duration, by wygasły
        for (int i = 0; i < StatusEffect.HALLUCINATIONS.getDefaultDuration(); i++) {
            manager.tick(player, i, Difficulty.NORMAL);
        }
        assertFalse(manager.hasHallucinations(), "Halucynacje powinny wygasnąć po czasie");
    }

    /**
     * getLastTriggered() zwraca ostatni status wyzwolony przez rollTriggers().
     * Po turze bez triggera powinien nadal przechowywać ostatnią wartość
     * aż do kolejnego wywołania rollTriggers().
     */
    @Test
    public void testLastTriggeredIsSetAfterSuccessfulRoll() {
        // Ustawiamy stats poniżej progów żeby zwiększyć szansę triggera
        player.setHydration(5); // poniżej progu DEHYDRATION (15)

        StatusEffect triggered = null;
        for (int i = 0; i < 100; i++) {
            manager.rollTriggers(player);
            triggered = manager.getLastTriggered();
            if (triggered != null) break;
        }

        assertNotNull(triggered,
                "Przy bardzo niskim nawodnieniu rollTriggers() powinien w końcu coś wyzwolić");
    }

    /**
     * rollTriggers() pomija statusy które już są aktywne (containsKey check).
     * Gracz nie może dostać podwójnej gorączki przez jeden roll.
     */
    @Test
    public void testRollTriggersSkipsAlreadyActiveStatuses() {
        manager.activate(StatusEffect.FEVER);
        int durationBefore = manager.getActiveStatuses().get(StatusEffect.FEVER);

        // rollTriggers z bardzo niskim health by zwiększyć szansę na FEVER
        player.setHealth(5);
        for (int i = 0; i < 20; i++) {
            manager.rollTriggers(player);
        }

        // Duration nie powinno wzrosnąć przez rollTriggers (tylko activate() to robi)
        int durationAfter = manager.getActiveStatuses().get(StatusEffect.FEVER);
        assertEquals(durationBefore, durationAfter,
                "rollTriggers() nie powinien nadpisywać aktywnego statusu");
    }

    /**
     * Dwa opóźnione efekty zaplanowane na tę samą turę powinny się zmergować
     * i obydwa zadziałać w jednym ticku.
     */
    @Test
    public void testDelayedEffectsOnSameTurnMerge() {
        player.setEnergy(30);
        player.setHealth(50);

        manager.addDelayedEffect(Map.of("energy", 10), 0, 3); // odpali na turze 3
        manager.addDelayedEffect(Map.of("health", 15), 0, 3); // też na turze 3

        manager.tick(player, 3, Difficulty.NORMAL);

        assertEquals(40, player.getEnergy(), "Energia powinna wzrosnąć o 10 w turze 3");
        assertEquals(65, player.getHealth(), "Zdrowie powinno wzrosnąć o 15 w turze 3");
    }
}