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

    private Player playerWith(Difficulty difficulty, Trait... traits) {
        Player player = new Player();
        player.initMaxStats(difficulty, List.of(traits));
        return player;
    }

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
        assertEquals(expected, player.getMaxHealth());
        assertEquals(expected, player.getHealth());
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    public void testLowerBoundAlwaysZero(Difficulty difficulty) {
        Player player = playerWith(difficulty);
        player.setHunger(-999);
        assertEquals(0, player.getHunger());
    }

    // ── Cechy podnoszące max ──────────────────────────────────────────────────

    @Test
    public void testHardyOnNormal() {
        Player player = playerWith(Difficulty.NORMAL, Trait.HARDY);

        assertEquals(120, player.getMaxHealth());
        assertEquals(expectedMax(Difficulty.NORMAL, "hunger"), player.getMaxHunger());

        player.setHealth(999);
        assertEquals(120, player.getHealth());
    }

    @Test
    public void testHardyOnEasy() {
        Player player = playerWith(Difficulty.EASY, Trait.HARDY);
        assertEquals(Player.ABSOLUTE_MAX, player.getMaxHealth());
    }

    @Test
    public void testWayfarerOnEasy() {
        Player player = playerWith(Difficulty.EASY, Trait.WAYFARER);

        assertEquals(125, player.getMaxEnergy());
        assertEquals(110, player.getMaxHealth());
    }

    // ── Cechy obniżające max ──────────────────────────────────────────────────

    @Test
    public void testSicklyOnHard() {
        Player player = playerWith(Difficulty.HARD, Trait.SICKLY);
        int expected = expectedMax(Difficulty.HARD, "health", Trait.SICKLY);

        assertEquals(expected, player.getMaxHealth());
        player.setHealth(999);
        assertEquals(expected, player.getHealth());
    }

    @Test
    public void testParchedOnHard() {
        Player player = playerWith(Difficulty.HARD, Trait.PARCHED);
        int expected = expectedMax(Difficulty.HARD, "hydration", Trait.PARCHED);

        assertEquals(expected, player.getMaxHydration());
        assertEquals(expectedMax(Difficulty.HARD, "health"), player.getMaxHealth());
    }

    @Test
    public void testMelancholicOnHard() {
        Player player = playerWith(Difficulty.HARD, Trait.MELANCHOLIC);
        int expected = expectedMax(Difficulty.HARD, "morale", Trait.MELANCHOLIC);
        assertEquals(expected, player.getMaxMorale());
    }

    // ── Cechy znoszące się nawzajem ───────────────────────────────────────────

    @Test
    public void testPilgrimAndMelancholicCancelOut() {
        Player player = playerWith(Difficulty.NORMAL, Trait.PILGRIM, Trait.MELANCHOLIC);
        assertEquals(100, player.getMaxMorale());
    }

    @Test
    public void testHardyAndSicklyPartialCancel() {
        Player player = playerWith(Difficulty.NORMAL, Trait.HARDY, Trait.SICKLY);
        assertEquals(105, player.getMaxHealth());
    }

    // ── Per-stat niezależność ─────────────────────────────────────────────────

    @Test
    public void testEachStatHasIndependentMax() {
        Player player = playerWith(Difficulty.EASY, Trait.FORAGER, Trait.HARDY);

        assertEquals(expectedMax(Difficulty.EASY, "health",    Trait.FORAGER, Trait.HARDY), player.getMaxHealth());
        assertEquals(expectedMax(Difficulty.EASY, "hunger",    Trait.FORAGER, Trait.HARDY), player.getMaxHunger());
        assertEquals(expectedMax(Difficulty.EASY, "hydration", Trait.FORAGER, Trait.HARDY), player.getMaxHydration());
        assertEquals(expectedMax(Difficulty.EASY, "energy",    Trait.FORAGER, Trait.HARDY), player.getMaxEnergy());
        assertEquals(expectedMax(Difficulty.EASY, "morale",    Trait.FORAGER, Trait.HARDY), player.getMaxMorale());
    }

    // ── Game over ─────────────────────────────────────────────────────────────

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    public void testGameOverDetection(Difficulty difficulty) {
        Player player = playerWith(difficulty);

        assertNull(player.getDeadStat());

        player.setEnergy(0);
        assertEquals("energy", player.getDeadStat());
    }

    @Test
    public void testGameOverWithSicklyAtReducedMax() {
        Player player = playerWith(Difficulty.HARD, Trait.SICKLY);
        assertNull(player.getDeadStat());

        player.setHealth(0);
        assertEquals("health", player.getDeadStat());
    }

    // ── Dystans i warunek wygranej ────────────────────────────────────────────

    /**
     * Gracz zaczyna z dystansem 4000 km.
     * addDistance() odejmuje przebyte kilometry.
     * Dystans nie może spaść poniżej 0 — clamp.
     */
    @Test
    public void testAddDistanceClampAtZero() {
        Player player = new Player();
        assertEquals(4000, player.getDistance(), "Gracz powinien startować z 4000 km");

        player.addDistance(3000);
        assertEquals(1000, player.getDistance(), "Po 3000 km powinno zostać 1000 km");

        player.addDistance(9999);
        assertEquals(0, player.getDistance(), "Dystans nie może być ujemny — clamp do 0");
    }

    /**
     * hasWon() powinno zwrócić true tylko gdy dystans == 0 (dotarcie do Krakowa).
     * Przy jakimkolwiek dystansie > 0 gracz jeszcze nie wygrał.
     */
    @Test
    public void testHasWon() {
        Player player = new Player();
        assertFalse(player.hasWon(), "Nowy gracz (4000 km) nie wygrał jeszcze");

        player.addDistance(3999);
        assertFalse(player.hasWon(), "1 km do celu — wciąż nie wygrał");

        player.addDistance(1);
        assertTrue(player.hasWon(), "Dystans = 0 → gracz wygrał");
    }

    /**
     * getDeadStat() sprawdza staty w określonej kolejności: health → hunger →
     * hydration → energy → morale. Gdy kilka statów równocześnie wynosi 0,
     * powinna wrócić ta z wyższym priorytetem.
     */
    @Test
    public void testDeadStatPriorityOrder() {
        Player player = new Player();

        // Ustaw energy i hunger na 0 — health nadal 100
        player.setEnergy(0);
        player.setHunger(0);
        assertEquals("hunger", player.getDeadStat(),
                "Głód ma wyższy priorytet śmierci niż energia");

        // Teraz też health = 0 — powinno wygrać health
        player.setHealth(0);
        assertEquals("health", player.getDeadStat(),
                "Zdrowie ma najwyższy priorytet spośród wszystkich statów");
    }

    /**
     * modifyStat() z nieznanym kluczem (literówka / nieistniejący stat)
     * nie powinien rzucać wyjątku — powinien po prostu nic nie robić.
     */
    @Test
    public void testModifyStatUnknownKeyIgnored() {
        Player player = new Player();
        int healthBefore = player.getHealth();

        assertDoesNotThrow(() -> player.modifyStat("nieznany_stat", 50),
                "Nieznany klucz statu nie powinien rzucać wyjątku");
        assertEquals(healthBefore, player.getHealth(),
                "Nieznany stat nie powinien zmienić żadnej wartości gracza");
    }

    /**
     * getTimeFormatted() zwraca string w formacie "Dzień N,  HH:00".
     * Dzień = (czas / 24) + 1, godzina = czas % 24.
     */
    @Test
    public void testGetTimeFormatted() {
        Player player = new Player();
        assertEquals("Dzień 1,  00:00", player.getTimeFormatted(),
                "Na starcie powinien być Dzień 1, godzina 00:00");

        player.addTime(24);
        assertEquals("Dzień 2,  00:00", player.getTimeFormatted(),
                "Po 24h powinien być Dzień 2, godzina 00:00");

        player.addTime(13);
        assertEquals("Dzień 2,  13:00", player.getTimeFormatted(),
                "Po kolejnych 13h powinien być Dzień 2, godzina 13:00");
    }

    /**
     * addTime() z ujemną wartością nie powinno cofać czasu —
     * zabezpieczenie Math.max(0, hours) wewnątrz metody.
     */
    @Test
    public void testAddTimeIgnoresNegativeValues() {
        Player player = new Player();
        player.addTime(5);
        player.addTime(-100);
        assertEquals(5, player.getTime(), "Ujemna wartość addTime() nie powinna cofać czasu");
    }

    /**
     * Kombinacja trudności EASY (+10) i dwóch cech dających łącznie +100 do health
     * nie powinna przekroczyć ABSOLUTE_MAX = 130.
     */
    @Test
    public void testAbsoluteMaxClampsMultipleBonuses() {
        // EASY (+10) + HARDY (health +20) = 130 = ABSOLUTE_MAX
        Player player = playerWith(Difficulty.EASY, Trait.HARDY);
        assertEquals(Player.ABSOLUTE_MAX, player.getMaxHealth(),
                "Suma bonusów nie może przekroczyć ABSOLUTE_MAX=" + Player.ABSOLUTE_MAX);

        // Dodatkowe setHealth powyżej ABSOLUTE_MAX też powinno być ucięte
        player.setHealth(Player.ABSOLUTE_MAX + 50);
        assertEquals(Player.ABSOLUTE_MAX, player.getHealth(),
                "setHealth powyżej ABSOLUTE_MAX powinno zostać obcięte do " + Player.ABSOLUTE_MAX);
    }

    /**
     * getStat() z nieznanym kluczem powinno zwrócić 100 (wartość domyślna),
     * bez rzucania wyjątku.
     */
    @Test
    public void testGetStatUnknownKeyReturnsDefault() {
        Player player = new Player();
        assertEquals(100, player.getStat("nieznany"),
                "getStat z nieznanym kluczem powinno zwrócić domyślne 100");
    }
}