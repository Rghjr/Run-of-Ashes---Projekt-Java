package com.runofashes;

import com.runofashes.model.Biome;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class BiomeTest {

    @Test
    public void testRollNextNeverReturnsCurrentBiome() {
        Random rng = new Random();
        Biome current = Biome.STEPPE;

        for (int i = 0; i < 1000; i++) {
            Biome next = Biome.rollNext(current, rng);
            assertNotEquals(current, next,
                    "Nowy biom po wylosowaniu nie może być taki sam jak poprzedni (" + current + ")");
            current = next;
        }
    }

    @Test
    public void testDecayMultipliers() {
        assertEquals(1.6, Biome.DESERT.getDecayMultiplier("hydration"), 0.001,
                "Na pustyni woda powinna spadać z mnożnikiem 1.6");

        assertEquals(1.5, Biome.MOUNTAINS.getDecayMultiplier("energy"), 0.001,
                "W górach energia powinna spadać z mnożnikiem 1.5");

        assertEquals(1.0, Biome.STEPPE.getDecayMultiplier("health"), 0.001,
                "Nieistniejący modyfikator statystyki powinien wynosić domyślnie 1.0");
    }

    @Test
    public void testEventWeightMods() {
        // Na pustyni waga wydarzeń z wodą rośnie o 40
        int waterWeightMod = Biome.DESERT.getEventWeightMods().getOrDefault("hydration", 0);
        assertEquals(40, waterWeightMod,
                "Pustynia powinna wymuszać więcej eventów związanych z nawodnieniem (+40)");

        // W górach waga ruchu spada
        int moveWeightMod = Biome.MOUNTAINS.getEventWeightMods().getOrDefault("move", 0);
        assertTrue(moveWeightMod < 0,
                "W górach powinno być mniej kart szybkiego ruchu");
    }
}