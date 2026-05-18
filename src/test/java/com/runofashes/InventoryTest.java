package com.runofashes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    public void setUp() {
        inventory = new Inventory();
    }

    @Test
    public void testAddingItemsRespectsMaxStack() {
        // WATER.maxStack == 3 — próba dodania 5 powinna dodać tylko 3
        int added = inventory.add(ItemType.WATER, 5);

        assertEquals(ItemType.WATER.getMaxStack(), added,
                "Powinno dodać tylko tyle wody ile pozwala maxStack (" + ItemType.WATER.getMaxStack() + ")");
        assertTrue(inventory.isFull(ItemType.WATER),
                "Ekwipunek powinien zgłosić, że slot na wodę jest pełny");
        assertEquals(ItemType.WATER.getMaxStack(), inventory.getCount(ItemType.WATER),
                "W ekwipunku powinno być dokładnie " + ItemType.WATER.getMaxStack() + " wód");
    }

    @Test
    public void testAddingBeyondFullStackReturnsZero() {
        // Wypełnij do maxStack, potem spróbuj dodać jeszcze jeden
        inventory.add(ItemType.WATER, ItemType.WATER.getMaxStack());
        int added = inventory.add(ItemType.WATER, 1);

        assertEquals(0, added, "Nie powinno dodać nic gdy stack jest pełny");
        assertTrue(inventory.isFull(ItemType.WATER));
    }

    @Test
    public void testUsingItemConsumesIt() {
        Player dummyPlayer = new Player();
        StatusManager dummyManager = new StatusManager();

        inventory.add(ItemType.BANDAGE);
        dummyPlayer.setHealth(50);

        // BANDAGE leczy +20 (po nerfie, było +30)
        int expectedHeal = ItemType.BANDAGE.getImmediateEffects().getOrDefault("health", 0);
        int expectedHealth = Math.min(100, 50 + expectedHeal);

        boolean used = inventory.useItem(ItemType.BANDAGE, dummyPlayer, dummyManager, 1);

        assertTrue(used, "Przedmiot powinien zostać użyty");
        assertFalse(inventory.has(ItemType.BANDAGE), "Bandaż powinien zniknąć z ekwipunku");
        assertEquals(expectedHealth, dummyPlayer.getHealth(),
                "Zdrowie gracza powinno wzrosnąć o " + expectedHeal + " (aktualny efekt bandaża)");
    }

    @Test
    public void testUsingNonexistentItemReturnsFalse() {
        Player dummyPlayer = new Player();
        StatusManager dummyManager = new StatusManager();

        boolean used = inventory.useItem(ItemType.HERBS, dummyPlayer, dummyManager, 1);

        assertFalse(used, "Nie powinno się udać użyć przedmiotu którego nie ma w ekwipunku");
    }
}