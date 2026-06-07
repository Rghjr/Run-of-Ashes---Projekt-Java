package com.runofashes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runofashes.engine.EventResolver;
import com.runofashes.engine.TraitManager;
import com.runofashes.model.Difficulty;
import com.runofashes.model.EventChoice;
import com.runofashes.model.Player;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy logiki rozstrzygania opcji wyboru (EventResolver.choiceChance / resolveChoice).
 * Wynik to wyłącznie sukces albo porażka — brak efektu pośredniego.
 */
public class EventResolverChoiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private EventChoice choice(String json) throws Exception {
        return MAPPER.readValue(json, EventChoice.class);
    }

    /** Resolver z deterministycznym rzutem kostką (nextDouble zawsze zwraca fixedRoll). */
    private EventResolver resolverReturning(double fixedRoll) {
        return new EventResolver(new Random() {
            @Override public double nextDouble() { return fixedRoll; }
        });
    }

    @Test
    public void choiceChanceScalesWithStat() throws Exception {
        EventChoice c = choice("{\"baseChance\":0.5,\"stat\":\"energy\",\"statInfluence\":0.4}");
        Player p = new Player(); // energy = 100
        double chance = new EventResolver(new Random())
                .choiceChance(c, p, new TraitManager(), Difficulty.NORMAL);
        assertEquals(0.9, chance, 1e-9, "0.5 + 0.4*(100/100) = 0.9");
    }

    @Test
    public void lowerStatLowersChance() throws Exception {
        EventChoice c = choice("{\"baseChance\":0.5,\"stat\":\"energy\",\"statInfluence\":0.4}");
        Player p = new Player();
        p.setEnergy(50);
        double chance = new EventResolver(new Random())
                .choiceChance(c, p, new TraitManager(), Difficulty.NORMAL);
        assertEquals(0.7, chance, 1e-9, "0.5 + 0.4*(50/100) = 0.7");
    }

    @Test
    public void noStatMeansPureBaseChance() throws Exception {
        EventChoice c = choice("{\"baseChance\":0.42}");
        Player p = new Player();
        double chance = new EventResolver(new Random())
                .choiceChance(c, p, new TraitManager(), Difficulty.NORMAL);
        assertEquals(0.42, chance, 1e-9, "Bez statystyki liczy się tylko baseChance");
    }

    @Test
    public void chanceIsClampedToUpperBound() throws Exception {
        EventChoice c = choice("{\"baseChance\":0.99,\"stat\":\"energy\",\"statInfluence\":0.5}");
        Player p = new Player(); // 0.99 + 0.5 = 1.49 -> clamp 0.98
        double chance = new EventResolver(new Random())
                .choiceChance(c, p, new TraitManager(), Difficulty.NORMAL);
        assertEquals(0.98, chance, 1e-9);
    }

    @Test
    public void chanceIsClampedToLowerBound() throws Exception {
        EventChoice c = choice("{\"baseChance\":0.0}");
        Player p = new Player();
        double chance = new EventResolver(new Random())
                .choiceChance(c, p, new TraitManager(), Difficulty.NORMAL);
        assertEquals(0.02, chance, 1e-9);
    }

    @Test
    public void difficultyBonusShiftsChance() throws Exception {
        EventChoice c = choice("{\"baseChance\":0.5}");
        Player p = new Player();
        EventResolver r = new EventResolver(new Random());
        assertEquals(0.60, r.choiceChance(c, p, new TraitManager(), Difficulty.EASY), 1e-9,
                "EASY daje +0.10 do szansy");
        assertEquals(0.40, r.choiceChance(c, p, new TraitManager(), Difficulty.HARD), 1e-9,
                "HARD daje -0.10 do szansy");
    }

    @Test
    public void resolveChoiceSucceedsWhenRollBelowChance() throws Exception {
        EventChoice c = choice("{\"baseChance\":0.5,\"stat\":\"energy\",\"statInfluence\":0.4}"); // 0.9
        Player p = new Player();
        EventResolver r = resolverReturning(0.1);
        assertTrue(r.resolveChoice(c, p, new TraitManager(), Difficulty.NORMAL),
                "Rzut 0.1 < szansa 0.9 => sukces");
    }

    @Test
    public void resolveChoiceFailsWhenRollAboveChance() throws Exception {
        EventChoice c = choice("{\"baseChance\":0.0}"); // clamp -> 0.02
        Player p = new Player();
        EventResolver r = resolverReturning(0.5);
        assertFalse(r.resolveChoice(c, p, new TraitManager(), Difficulty.NORMAL),
                "Rzut 0.5 >= szansa 0.02 => porażka");
    }
}
