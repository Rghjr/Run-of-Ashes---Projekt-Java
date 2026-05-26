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
        // +20 energii po 2 turach, zaczynając od tury 5 → zadziała w turze 7
        manager.addDelayedEffect(Map.of("energy", 20), 5, 2);
        player.setEnergy(50);

        manager.tick(player, 6, Difficulty.NORMAL);
        assertEquals(50, player.getEnergy(), "Energia nie powinna wzrosnąć przed czasem");

        manager.tick(player, 7, Difficulty.NORMAL);
        assertEquals(70, player.getEnergy(), "Energia powinna wzrosnąć o 20 w docelowej turze");
    }

    @Test
    public void testRollTriggersActivatesDehydration() {
        // Hydration poniżej progu DEHYDRATION (triggerThreshold=15)
        player.setHydration(10);

        for (int i = 0; i < 50; i++) {
            manager.rollTriggers(player);
            if (manager.isActive(StatusEffect.DEHYDRATION)) break;
        }

        assertTrue(manager.isActive(StatusEffect.DEHYDRATION),
                "Gracz powinien z czasem złapać Odwodnienie przy bardzo niskim nawodnieniu");
    }

    @Test
    public void testStatusPerTurnEffects() {
        manager.activate(StatusEffect.FEVER);
        player.setHealth(100);

        manager.tick(player, 1, Difficulty.NORMAL);

        // FEVER: -8 health, -5 energy, -3 morale per tura
        int expectedHealth = 100 + StatusEffect.FEVER.getPerTurnEffects().getOrDefault("health", 0);
        assertEquals(expectedHealth, player.getHealth(),
                "Gorączka powinna odebrać " + Math.abs(StatusEffect.FEVER.getPerTurnEffects().get("health"))
                        + " punktów zdrowia w tej turze");
        assertTrue(manager.isActive(StatusEffect.FEVER), "Gorączka powinna być nadal aktywna");
    }
}