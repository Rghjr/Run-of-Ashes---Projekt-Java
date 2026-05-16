package com.runofashes;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    @Test
    public void testClampStats() {
        Player player = new Player();

        player.setHealth(150);
        assertEquals(100, player.getHealth(), "Zdrowie nie powinno przekroczyć 100");

        player.setHunger(-20);
        assertEquals(0, player.getHunger(), "Głód nie powinien spaść poniżej 0");
    }

    @Test
    public void testGameOverDetection() {
        Player player = new Player();

        assertNull(player.getDeadStat(), "Gracz z pełnym zdrowiem nie powinien być martwy");

        player.setEnergy(0);
        assertEquals("energy", player.getDeadStat(), "Silnik powinien zwrócić 'energy' jako powód śmierci");
    }
}