package com.runofashes;

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
        // Dodanie opóźnionego efektu: +20 energii po 2 turach.
        // Symulacja, że obecna tura to 5. Efekt powinien wejść w turze 7.
        manager.addDelayedEffect(Map.of("energy", 20), 5, 2);

        player.setEnergy(50);

        // Mijają tury, ale to jeszcze nie tura 7
        manager.tick(player, 6);
        assertEquals(50, player.getEnergy(), "Energia nie powinna wzrosnąć przed czasem");

        // Wchodzi tura 7 - leki zaczynają działać
        manager.tick(player, 7);
        assertEquals(70, player.getEnergy(), "Energia powinna wzrosnąć o 20 w docelowej turze");
    }

    @Test
    public void testRollTriggersActivatesDehydration() {
        // Zmuszenie gracza do skrajnego pragnienia (próg dla odwodnienia to 15)
        player.setHydration(10);

        // Symulujemy upływ czasu (np. 50 tur), upewniając się, że w końcu złapie odpowiedni status.
        for (int i = 0; i < 50; i++) {
            manager.rollTriggers(player);

            // Przerwanie pętli TYLKO wtedy, gdy złapie Odwodnienie
            if (manager.isActive(StatusEffect.DEHYDRATION)) {
                break;
            }
        }

        assertTrue(manager.isActive(StatusEffect.DEHYDRATION), "Gracz powinien z czasem złapać Odwodnienie przy bardzo niskim nawodnieniu");
    }

    @Test
    public void testStatusPerTurnEffects() {
        // Narzucenie graczowi gorączki
        manager.activate(StatusEffect.FEVER); // Gorączka zabiera m.in. 8 punktów zdrowia co turę
        player.setHealth(100);

        // Symulacja upływu jednej tury
        manager.tick(player, 1);

        assertEquals(92, player.getHealth(), "Gorączka powinna odebrać 8 punktów zdrowia w tej turze");
        assertTrue(manager.isActive(StatusEffect.FEVER), "Gorączka powinna być nadal aktywna");
    }
}