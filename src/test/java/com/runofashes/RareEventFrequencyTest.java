package com.runofashes;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

public class RareEventFrequencyTest {

    private boolean rareOccursInTurn(Random rnd) {
        return rnd.nextDouble() < (1.0 / 11.0);
    }

    @Test
    public void rareShouldAppearBetween8And15TurnsOnAverage() {
        Random rnd = new Random(54321);
        int trials = 1000;
        int maxTurns = 1000;
        double totalInterval = 0;
        int intervals = 0;

        for (int t = 0; t < trials; t++) {
            int turns = 0;
            for (int i = 1; i <= maxTurns; i++) {
                if (rareOccursInTurn(rnd)) {
                    turns = i;
                    break;
                }
            }
            if (turns > 0) {
                totalInterval += turns;
                intervals++;
            }
        }

        double mean = totalInterval / intervals;

        assertTrue(mean >= 8.0 && mean <= 15.0, "Średni interwał dla kart rare to 8-15 tur, wyniósł: " + mean);
    }
}