package com.runofashes;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CardDrawerWeightTest {

    private int calculateCopies(int statValue, int maxStat) {
        int baseWeight = 10 + (maxStat - statValue);
        int weight = Math.max(5, baseWeight);
        return Math.max(1, weight / 10);
    }

    @Test
    public void stat0ShouldYield10xMoreCopiesThanStat100() {
        int copiesStat100 = calculateCopies(100, 100);
        int copiesStat0   = calculateCopies(0, 100);

        assertEquals(1, copiesStat100);
        assertEquals(11, copiesStat0);

        assertTrue(copiesStat0 >= copiesStat100 * 10, "Karty przy stat=0 powinny pojawiać się >10x częściej niż przy stat=100");
    }
}