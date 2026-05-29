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
            assertNotEquals(current, next);
            current = next;
        }
    }

    @Test
    public void testDecayMultipliers() {
        assertEquals(1.6, Biome.DESERT.getDecayMultiplier("hydration"), 0.001);
        assertEquals(1.5, Biome.MOUNTAINS.getDecayMultiplier("energy"), 0.001);
        assertEquals(1.0, Biome.STEPPE.getDecayMultiplier("health"), 0.001);
    }

    @Test
    public void testEventWeightMods() {
        int waterWeightMod = Biome.DESERT.getEventWeightMods().getOrDefault("hydration", 0);
        assertEquals(40, waterWeightMod);

        int moveWeightMod = Biome.MOUNTAINS.getEventWeightMods().getOrDefault("move", 0);
        assertTrue(moveWeightMod < 0);
    }

    /**
     * Nieistniejący klucz statu w decayMultipliers zwraca 1.0 (brak modyfikacji).
     * Sprawdza wszystkie biomy — żaden nie powinien crashować przy nieznanym stacie.
     */
    @Test
    public void testDecayMultiplierDefaultsToOneForUnknownStat() {
        for (Biome biome : Biome.values()) {
            assertEquals(1.0, biome.getDecayMultiplier("nieznany_stat"), 0.001,
                    biome + ": nieznany stat powinien dać mnożnik 1.0");
        }
    }

    /**
     * Każdy biom musi mieć niepustą wiadomość wejścia (entryMessage).
     * Używana w panelu środowiska — null lub pusty string powoduje pusty UI.
     */
    @Test
    public void testAllBiomesHaveEntryMessages() {
        for (Biome biome : Biome.values()) {
            assertNotNull(biome.getEntryMessage(),
                    biome + ": entryMessage nie może być null");
            assertFalse(biome.getEntryMessage().isBlank(),
                    biome + ": entryMessage nie może być pusty");
        }
    }

    /**
     * Wszystkie mnożniki decay muszą być dodatnie.
     * Ujemny lub zerowy mnożnik odwróciłby działanie efektów (np. pustynia leczyłaby zamiast niszczyć).
     */
    @Test
    public void testAllDecayMultipliersArePositive() {
        for (Biome biome : Biome.values()) {
            biome.getDecayMultipliers().forEach((stat, mult) ->
                    assertTrue(mult > 0,
                            biome + ": mnożnik dla '" + stat + "' musi być > 0, był: " + mult));
        }
    }

    /**
     * Każdy biom musi mieć etykietę i emoji (wyświetlane w HUD i panelu środowiska).
     */
    @Test
    public void testAllBiomesHaveLabelAndEmoji() {
        for (Biome biome : Biome.values()) {
            assertNotNull(biome.getLabel(), biome + ": label nie może być null");
            assertFalse(biome.getLabel().isBlank(), biome + ": label nie może być pusty");
            assertNotNull(biome.getEmoji(), biome + ": emoji nie może być null");
        }
    }

    /**
     * PLAINS powinny mieć mnożnik energii ≤ 1.0 — w terenie równinnym energia nie spada szybciej.
     * MOUNTAINS muszą mieć mnożnik energii > 1.0 — wspinaczka kosztuje więcej.
     */
    @Test
    public void testBiomeEnergyDecayLogic() {
        assertTrue(Biome.MOUNTAINS.getDecayMultiplier("energy") > 1.0,
                "Góry powinny zwiększać zużycie energii (mnożnik > 1.0)");
        assertTrue(Biome.PLAINS.getDecayMultiplier("energy") <= 1.0,
                "Równiny nie powinny zwiększać zużycia energii (mnożnik ≤ 1.0)");
    }
}