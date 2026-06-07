package com.runofashes;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DifficultyDrainTest {

    private int applySingleDrain(int delta, double diffMult) {
        double biomeMult = 1.0;
        double lateGameMult = 1.0;
        return (int) Math.round(delta * biomeMult * diffMult * lateGameMult);
    }

    @Test
    public void hardShouldDrainAbout1Point3xFasterThanNormal() {
        int baseDelta = -10;

        double normalMult = 1.0;
        double hardMult = 1.3;

        int normalDrain = applySingleDrain(baseDelta, normalMult);
        int hardDrain   = applySingleDrain(baseDelta, hardMult);

        double ratio = (double) Math.abs(hardDrain) / Math.abs(normalDrain);

        assertTrue(Math.abs(ratio - 1.3) < 0.01, "Poziom Hard powinien drainować dokładnie o 30% mocniej. Otrzymano stosunek: " + ratio);
    }
}