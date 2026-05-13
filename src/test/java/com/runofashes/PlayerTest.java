package com.runofashes;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    @Test
    public void testClampStats() {
        Player player = new Player();

        // Próba ustawienia wartości powyżej 100
        player.setHealth(150);
        assertEquals(100, player.getHealth(), "Zdrowie nie powinno przekroczyć 100");

        // Próba ustawienia wartości poniżej 0
        player.setHunger(-20);
        assertEquals(0, player.getHunger(), "Głód nie powinien spaść poniżej 0");
    }

    @Test
    public void testGameOverDetection() {
        Player player = new Player();

        // Na starcie gracz żyje
        assertNull(player.getDeadStat(), "Gracz z pełnym zdrowiem nie powinien być martwy");

        // Zabicie gracza brakiem energii
        player.setEnergy(0);
        assertEquals("energy", player.getDeadStat(), "Silnik powinien zwrócić 'energy' jako powód śmierci");
    }
}