package com.runofashes;

import com.runofashes.engine.Inventory;
import com.runofashes.engine.StatusManager;
import com.runofashes.model.ItemType;
import com.runofashes.model.Player;
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
        int added = inventory.add(ItemType.WATER, 5);
        assertEquals(ItemType.WATER.getMaxStack(), added);
        assertTrue(inventory.isFull(ItemType.WATER));
        assertEquals(ItemType.WATER.getMaxStack(), inventory.getCount(ItemType.WATER));
    }

    @Test
    public void testAddingBeyondFullStackReturnsZero() {
        inventory.add(ItemType.WATER, ItemType.WATER.getMaxStack());
        int added = inventory.add(ItemType.WATER, 1);
        assertEquals(0, added);
        assertTrue(inventory.isFull(ItemType.WATER));
    }

    @Test
    public void testUsingItemConsumesIt() {
        Player dummyPlayer = new Player();
        StatusManager dummyManager = new StatusManager();
        inventory.add(ItemType.BANDAGE);
        dummyPlayer.setHealth(50);

        int expectedHeal = ItemType.BANDAGE.getImmediateEffects().getOrDefault("health", 0);
        int expectedHealth = Math.min(100, 50 + expectedHeal);

        boolean used = inventory.useItem(ItemType.BANDAGE, dummyPlayer, dummyManager, 1);

        assertTrue(used);
        assertFalse(inventory.has(ItemType.BANDAGE));
        assertEquals(expectedHealth, dummyPlayer.getHealth());
    }

    @Test
    public void testUsingNonexistentItemReturnsFalse() {
        Player dummyPlayer = new Player();
        StatusManager dummyManager = new StatusManager();
        boolean used = inventory.useItem(ItemType.HERBS, dummyPlayer, dummyManager, 1);
        assertFalse(used);
    }

    // ── Nowe testy ────────────────────────────────────────────────────────────

    /**
     * Nowy ekwipunek powinien być pusty.
     * isEmpty() musi zwracać true, has() false dla każdego przedmiotu.
     */
    @Test
    public void testNewInventoryIsEmpty() {
        assertTrue(inventory.isEmpty(), "Nowy ekwipunek powinien być pusty");
        assertFalse(inventory.has(ItemType.WATER), "Nowy ekwipunek nie ma wody");
        assertEquals(0, inventory.getCount(ItemType.WATER));
    }

    /**
     * consume() usuwa podaną liczbę przedmiotów z ekwipunku.
     * Testuje głównie naprawiony mechanizm ujemnych itemEffects z JSON
     * (np. BANDAGE: -1 w am_karawana_1).
     */
    @Test
    public void testConsumeRemovesItem() {
        inventory.add(ItemType.BANDAGE);
        assertTrue(inventory.has(ItemType.BANDAGE));

        int removed = inventory.consume(ItemType.BANDAGE, 1);

        assertEquals(1, removed, "consume() powinno zwrócić ile faktycznie zabrano");
        assertFalse(inventory.has(ItemType.BANDAGE), "Bandaż powinien zniknąć po consume()");
        assertTrue(inventory.isEmpty(), "Ekwipunek powinien być pusty po zabraniu jedynego przedmiotu");
    }

    /**
     * consume() gdy przedmiotu nie ma w ekwipunku zwraca 0
     * i nie tworzy ujemnych stanów (to był oryginalny bug z add(-1)).
     */
    @Test
    public void testConsumeWhenEmptyReturnsZero() {
        int removed = inventory.consume(ItemType.DRIED_MEAT, 1);

        assertEquals(0, removed, "Nie można zabrać czegoś czego nie ma");
        assertEquals(0, inventory.getCount(ItemType.DRIED_MEAT),
                "Count nie może być ujemny po consume() na pustym slocie");
        assertTrue(inventory.isEmpty());
    }

    /**
     * consume() zabiera maksymalnie tyle ile jest dostępne.
     * Przy próbie zabrania więcej niż jest — zabiera wszystko i zwraca faktyczną ilość.
     */
    @Test
    public void testConsumePartialWhenNotEnough() {
        inventory.add(ItemType.SALT); // maxStack=1, dodajemy 1
        assertEquals(1, inventory.getCount(ItemType.SALT));

        int removed = inventory.consume(ItemType.SALT, 5); // próba zabrania 5

        assertEquals(1, removed, "Powinno zabrać tylko 1 (tyle ile było)");
        assertEquals(0, inventory.getCount(ItemType.SALT), "Salt powinien być 0 po consume");
    }

    /**
     * add() z ujemną wartością musi zwrócić 0 i nie modyfikować ekwipunku.
     * Fix dla bugu gdzie add(-1) tworzyło ujemny stan przedmiotu.
     */
    @Test
    public void testAddNegativeAmountReturnsZeroAndDoesNothing() {
        int result = inventory.add(ItemType.WATER, -1);

        assertEquals(0, result, "add() z ujemną wartością powinno zwrócić 0");
        assertEquals(0, inventory.getCount(ItemType.WATER),
                "Ujemna wartość add() nie może zmodyfikować ekwipunku");
        assertTrue(inventory.isEmpty());
    }

    /**
     * add() z wartością 0 też powinno zwrócić 0 i nic nie dodawać.
     */
    @Test
    public void testAddZeroAmountReturnsZero() {
        int result = inventory.add(ItemType.WATER, 0);
        assertEquals(0, result, "add(0) powinno zwrócić 0");
        assertFalse(inventory.has(ItemType.WATER));
    }

    /**
     * isEmpty() przechodzi z true na false po dodaniu przedmiotu
     * i z powrotem na true po jego zabraniu przez consume().
     */
    @Test
    public void testIsEmptyTransitionsCorrectly() {
        assertTrue(inventory.isEmpty(), "Przed dodaniem: pusty");

        inventory.add(ItemType.GRAPES);
        assertFalse(inventory.isEmpty(), "Po dodaniu: nie pusty");

        inventory.consume(ItemType.GRAPES, 1);
        assertTrue(inventory.isEmpty(), "Po consume: znowu pusty");
    }

    /**
     * useItem() z przedmiotem mającym delayed effects (np. HERBS: +7 health po 2 turach)
     * rejestruje opóźniony efekt w StatusManager bez natychmiastowego aplikowania go.
     */
    @Test
    public void testUsingItemWithDelayedEffectRegistersDelay() {
        Player player = new Player();
        StatusManager manager = new StatusManager();
        player.setHealth(50);
        inventory.add(ItemType.HERBS);

        int healthBefore = player.getHealth();
        inventory.useItem(ItemType.HERBS, player, manager, 5);

        // Natychmiastowy efekt HERBS: +4 health
        int immediateHeal = ItemType.HERBS.getImmediateEffects().getOrDefault("health", 0);
        assertEquals(healthBefore + immediateHeal, player.getHealth(),
                "Natychmiastowy efekt ziół powinien być +4 health");

        // Opóźniony efekt jeszcze nie zadziałał
        manager.tick(player, 6, com.runofashes.model.Difficulty.NORMAL);
        assertEquals(healthBefore + immediateHeal, player.getHealth(),
                "Opóźniony efekt NIE powinien zadziałać przed docelową turą");

        // W turze 7 (5 + 2) opóźniony efekt +7 health odpala
        manager.tick(player, 7, com.runofashes.model.Difficulty.NORMAL);
        int delayedHeal = ItemType.HERBS.getDelayedEffects().getOrDefault("health", 0);
        assertEquals(healthBefore + immediateHeal + delayedHeal, player.getHealth(),
                "Opóźniony efekt ziół (+7 health) powinien zadziałać w turze 7");
    }
}