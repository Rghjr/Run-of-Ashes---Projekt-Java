package com.runofashes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InventoryTest {

    private Inventory inventory;

    // Czysty ekwipunek
    @BeforeEach
    public void setUp() {
        inventory = new Inventory();
    }

    @Test
    public void testAddingItemsRespectsMaxStack() {
        // Próba dodania 5 sztuk wody (maksymalny stack dla wody to 1)
        int added = inventory.add(ItemType.WATER, 5);

        assertEquals(1, added, "Powinno dodać tylko 1 wodę");
        assertTrue(inventory.isFull(ItemType.WATER), "Ekwipunek powinien zgłosić, że slot na wodę jest pełny");
        assertEquals(1, inventory.getCount(ItemType.WATER), "W ekwipunku powinna być dokładnie 1 woda");
    }

    @Test
    public void testUsingItemConsumesIt() {
        Player dummyPlayer = new Player();
        StatusManager dummyManager = new StatusManager();

        inventory.add(ItemType.BANDAGE);
        dummyPlayer.setHealth(50); // Zranienie gracza

        // Użycie przedmiotu (Bandaż leczy +30)
        boolean used = inventory.useItem(ItemType.BANDAGE, dummyPlayer, dummyManager, 1);

        assertTrue(used, "Przedmiot powinien zostać użyty");
        assertFalse(inventory.has(ItemType.BANDAGE), "Bandaż powinien zniknąć z ekwipunku");
        assertEquals(80, dummyPlayer.getHealth(), "Zdrowie gracza powinno wzrosnąć o 30");
    }
}