package com.runofashes;

import com.runofashes.model.Weather;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class WeatherTest {

    @Test
    public void testRollNextNeverReturnsCurrentWeather() {
        Random rng = new Random();
        Weather current = Weather.CLEAR;
        for (int i = 0; i < 1000; i++) {
            Weather next = Weather.rollNext(current, rng);
            assertNotEquals(current, next);
            current = next;
        }
    }

    @Test
    public void testWeatherPerTurnEffects() {
        assertEquals(-3, Weather.STORM.getPerTurnEffects().getOrDefault("energy", 0));
        assertEquals(-4, Weather.STORM.getPerTurnEffects().getOrDefault("morale", 0));
    }

    @Test
    public void testWeatherDurationBounds() {
        Weather hot = Weather.HOT;
        assertTrue(hot.getMinTurns() > 0);
        assertTrue(hot.getMaxTurns() >= hot.getMinTurns());
        assertEquals(3, hot.getMinTurns());
        assertEquals(6, hot.getMaxTurns());
    }

    /**
     * Każda pogoda musi mieć wagę > 0.
     * Waga zerowa lub ujemna wykluczałaby daną pogodę z losowania całkowicie
     * albo crashowałaby rollNext() dzieląc przez 0.
     */
    @Test
    public void testAllWeatherWeightsArePositive() {
        for (Weather w : Weather.values()) {
            assertTrue(w.getWeight() > 0,
                    w + ": waga musi być > 0, była: " + w.getWeight());
        }
    }

    /**
     * CLEAR (pogodna) nie ma efektów per-tura — nie karze gracza.
     * Ładna pogoda to jedyna "neutralna" opcja w grze.
     */
    @Test
    public void testClearWeatherHasNoPerTurnEffects() {
        assertTrue(Weather.CLEAR.getPerTurnEffects().isEmpty(),
                "Pogodna pogoda nie powinna mieć żadnych per-tura efektów karnych");
    }

    /**
     * HOT (upał) zwiększa wagę eventów nawodnienia w puli kart.
     * Gracz widzi więcej szans na znalezienie wody gdy jest gorąco.
     */
    @Test
    public void testHotWeatherIncreasesHydrationEventWeight() {
        int mod = Weather.HOT.getEventWeightMods().getOrDefault("hydration", 0);
        assertTrue(mod > 0,
                "Upał powinien zwiększać wagę eventów nawodnienia, był: " + mod);
    }

    /**
     * STORM (burza) zmniejsza wagę ruchu — trudniej się przemieszczać podczas burzy.
     */
    @Test
    public void testStormWeatherReducesMoveEventWeight() {
        int mod = Weather.STORM.getEventWeightMods().getOrDefault("move", 0);
        assertTrue(mod < 0,
                "Burza powinna zmniejszać wagę eventów ruchu, był: " + mod);
    }

    /**
     * Dla każdej pogody minTurns musi być <= maxTurns.
     * Naruszenie tego warunku crashuje tickWeather() przez ujemny zakres nextInt().
     */
    @Test
    public void testAllWeatherDurationRangesAreValid() {
        for (Weather w : Weather.values()) {
            assertTrue(w.getMinTurns() > 0,
                    w + ": minTurns musi być > 0");
            assertTrue(w.getMaxTurns() >= w.getMinTurns(),
                    w + ": maxTurns (" + w.getMaxTurns() + ") musi być >= minTurns (" + w.getMinTurns() + ")");
        }
    }

    /**
     * Każda pogoda musi mieć etykietę i emoji (wyświetlane w HUD i panelu środowiska).
     */
    @Test
    public void testAllWeathersHaveLabelAndEmoji() {
        for (Weather w : Weather.values()) {
            assertNotNull(w.getLabel(), w + ": label nie może być null");
            assertFalse(w.getLabel().isBlank(), w + ": label nie może być pusty");
            assertNotNull(w.getEmoji(), w + ": emoji nie może być null");
        }
    }

    /**
     * rollNext() działa poprawnie nawet przy wielokrotnym losowaniu tej samej pogody
     * jako "current" — żaden enum nie może zablokować pętli na zawsze.
     * Testujemy każdą pogodę jako punkt startowy.
     */
    @Test
    public void testRollNextWorksForAllStartingWeathers() {
        Random rng = new Random(42);
        for (Weather start : Weather.values()) {
            for (int i = 0; i < 100; i++) {
                Weather next = Weather.rollNext(start, rng);
                assertNotEquals(start, next,
                        "rollNext z " + start + " nie może zwrócić " + start);
            }
        }
    }
}