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
            assertNotEquals(current, next,
                    "Pogoda po wylosowaniu nowej nie może pozostać taka sama (" + current + ")");
            current = next;
        }
    }

    @Test
    public void testWeatherPerTurnEffects() {
        int energyDrain = Weather.STORM.getPerTurnEffects().getOrDefault("energy", 0);
        int moraleDrain = Weather.STORM.getPerTurnEffects().getOrDefault("morale", 0);

        assertEquals(-3, energyDrain, "Burza powinna odbierać 3 punkty energii co turę");
        assertEquals(-4, moraleDrain, "Burza powinna odbierać 4 punkty morale co turę");
    }

    @Test
    public void testWeatherDurationBounds() {
        Weather hot = Weather.HOT;

        assertTrue(hot.getMinTurns() > 0, "Pogoda musi trwać minimum 1 turę");
        assertTrue(hot.getMaxTurns() >= hot.getMinTurns(), "MaxTurns musi być większe/równe MinTurns");

        assertEquals(3, hot.getMinTurns());
        assertEquals(6, hot.getMaxTurns());
    }
}