package com.runofashes;

import com.runofashes.model.Difficulty;
import com.runofashes.model.Player;
import com.runofashes.model.Trait;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    // ── Helpery ───────────────────────────────────────────────────────────────

    /** Tworzy gracza z obliczonymi maksimami dla danej trudności i cech. */
    private Player playerWith(Difficulty difficulty, Trait... traits) {
        Player player = new Player();
        player.initMaxStats(difficulty, List.of(traits));
        return player;
    }

    /**
     * Oczekiwane maximum dla danego statu:
     * min(ABSOLUTE_MAX, max(1, 100 + diffBonus + sum(traitBonuses[stat])))
     */
    private int expectedMax(Difficulty difficulty, String stat, Trait... traits) {
        int bonus = difficulty.getStartStatBonus();
        for (Trait t : traits) {
            bonus += t.getStartBonus().getOrDefault(stat, 0);
        }
        return Math.max(1, Math.min(Player.ABSOLUTE_MAX, 100 + bonus));
    }

    // ── Maksima per trudność (bez cech) ───────────────────────────────────────

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    public void testUpperClampWithDifficultyOnly(Difficulty difficulty) {
        Player player = playerWith(difficulty);
        int expected = expectedMax(difficulty, "health");

        player.setHealth(999);
        assertEquals(expected, player.getMaxHealth(),
                "[" + difficulty.getLabel() + "] maxHealth powinno wynosić " + expected);
        assertEquals(expected, player.getHealth(),
                "[" + difficulty.getLabel() + "] setHealth(999) powinno dać " + expected);
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    public void testLowerBoundAlwaysZero(Difficulty difficulty) {
        Player player = playerWith(difficulty);
        player.setHunger(-999);
        assertEquals(0, player.getHunger(),
                "[" + difficulty.getLabel() + "] Głód nie może spaść poniżej 0");
    }

    // ── Cechy podnoszące max ──────────────────────────────────────────────────

    @Test
    public void testHardyOnNormal() {
        // NORMAL: +0, HARDY: health +20 → maxHealth = 120; inne staty bez bonusu od HARDY
        Player player = playerWith(Difficulty.NORMAL, Trait.HARDY);

        assertEquals(120, player.getMaxHealth(), "HARDY+NORMAL: maxHealth powinno być 120");
        assertEquals(expectedMax(Difficulty.NORMAL, "hunger"), player.getMaxHunger(),
                "HARDY nie powinien wpływać na maxHunger");

        player.setHealth(999);
        assertEquals(120, player.getHealth(), "Zdrowie powinno być przycięte do 120");
    }

    @Test
    public void testHardyOnEasy() {
        // EASY: +10, HARDY: health +20 → 130 = ABSOLUTE_MAX
        Player player = playerWith(Difficulty.EASY, Trait.HARDY);

        assertEquals(Player.ABSOLUTE_MAX, player.getMaxHealth(),
                "HARDY+EASY: maxHealth powinno osiągnąć ABSOLUTE_MAX=" + Player.ABSOLUTE_MAX);
    }

    @Test
    public void testWayfarerOnEasy() {
        // EASY: +10, WAYFARER: energy +15 → maxEnergy = 125
        Player player = playerWith(Difficulty.EASY, Trait.WAYFARER);

        assertEquals(125, player.getMaxEnergy(), "WAYFARER+EASY: maxEnergy powinno być 125");
        assertEquals(110, player.getMaxHealth(), "WAYFARER nie wpływa na maxHealth");
    }

    // ── Cechy obniżające max ──────────────────────────────────────────────────

    @Test
    public void testSicklyOnHard() {
        // HARD: -10, SICKLY: health -15 → maxHealth = 75
        Player player = playerWith(Difficulty.HARD, Trait.SICKLY);
        int expected = expectedMax(Difficulty.HARD, "health", Trait.SICKLY); // 75

        assertEquals(expected, player.getMaxHealth(),
                "SICKLY+HARD: maxHealth powinno być " + expected);
        player.setHealth(999);
        assertEquals(expected, player.getHealth(),
                "Zdrowie powinno być przycięte do " + expected);
    }

    @Test
    public void testParchedOnHard() {
        // HARD: -10, PARCHED: hydration -15 → maxHydration = 75
        Player player = playerWith(Difficulty.HARD, Trait.PARCHED);
        int expected = expectedMax(Difficulty.HARD, "hydration", Trait.PARCHED); // 75

        assertEquals(expected, player.getMaxHydration(),
                "PARCHED+HARD: maxHydration powinno być " + expected);
        assertEquals(expectedMax(Difficulty.HARD, "health"), player.getMaxHealth(),
                "PARCHED nie wpływa na maxHealth");
    }

    @Test
    public void testMelancholicOnHard() {
        // HARD: -10, MELANCHOLIC: morale -20 → maxMorale = 70
        Player player = playerWith(Difficulty.HARD, Trait.MELANCHOLIC);
        int expected = expectedMax(Difficulty.HARD, "morale", Trait.MELANCHOLIC); // 70

        assertEquals(expected, player.getMaxMorale(),
                "MELANCHOLIC+HARD: maxMorale powinno być " + expected);
    }

    // ── Cechy znoszące się nawzajem ───────────────────────────────────────────

    @Test
    public void testPilgrimAndMelancholicCancelOut() {
        // PILGRIM: morale +20, MELANCHOLIC: morale -20 → na NORMAL: max = 100
        Player player = playerWith(Difficulty.NORMAL, Trait.PILGRIM, Trait.MELANCHOLIC);

        assertEquals(100, player.getMaxMorale(),
                "Pielgrzym i Melancholik powinny się znosić — maxMorale = 100");
    }

    @Test
    public void testHardyAndSicklyPartialCancel() {
        // NORMAL: +0, HARDY: health +20, SICKLY: health -15 → maxHealth = 105
        Player player = playerWith(Difficulty.NORMAL, Trait.HARDY, Trait.SICKLY);

        assertEquals(105, player.getMaxHealth(),
                "HARDY+SICKLY+NORMAL: maxHealth powinno być 105");
    }

    // ── Per-stat niezależność ─────────────────────────────────────────────────

    @Test
    public void testEachStatHasIndependentMax() {
        // EASY: +10 globalnie, FORAGER: hunger +10, HARDY: health +20
        Player player = playerWith(Difficulty.EASY, Trait.FORAGER, Trait.HARDY);

        assertEquals(expectedMax(Difficulty.EASY, "health",    Trait.FORAGER, Trait.HARDY), player.getMaxHealth());    // 130
        assertEquals(expectedMax(Difficulty.EASY, "hunger",    Trait.FORAGER, Trait.HARDY), player.getMaxHunger());    // 120
        assertEquals(expectedMax(Difficulty.EASY, "hydration", Trait.FORAGER, Trait.HARDY), player.getMaxHydration()); // 110
        assertEquals(expectedMax(Difficulty.EASY, "energy",    Trait.FORAGER, Trait.HARDY), player.getMaxEnergy());    // 110
        assertEquals(expectedMax(Difficulty.EASY, "morale",    Trait.FORAGER, Trait.HARDY), player.getMaxMorale());    // 110
    }

    // ── Game over ─────────────────────────────────────────────────────────────

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    public void testGameOverDetection(Difficulty difficulty) {
        Player player = playerWith(difficulty);

        assertNull(player.getDeadStat(),
                "[" + difficulty.getLabel() + "] Nowy gracz nie powinien być martwy");

        player.setEnergy(0);
        assertEquals("energy", player.getDeadStat(),
                "[" + difficulty.getLabel() + "] Energia = 0 → powód śmierci 'energy'");
    }

    @Test
    public void testGameOverWithSicklyAtReducedMax() {
        // Nawet gdy maxHealth = 75 (SICKLY+HARD), śmierć = health ≤ 0, nie ≤ max
        Player player = playerWith(Difficulty.HARD, Trait.SICKLY);
        assertNull(player.getDeadStat(), "maxHealth=75 to nie śmierć");

        player.setHealth(0);
        assertEquals("health", player.getDeadStat(), "health=0 → śmierć z 'health'");
    }
}