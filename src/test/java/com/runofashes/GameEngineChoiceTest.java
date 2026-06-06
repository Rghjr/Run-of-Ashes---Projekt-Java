package com.runofashes;

import com.runofashes.engine.EventResult;
import com.runofashes.engine.GameEngine;
import com.runofashes.model.GameEvent;
import com.runofashes.utils.EventLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy integracyjne rozstrzygania questa z wyborem przez GameEngine.executeChoice.
 */
public class GameEngineChoiceTest {

    private GameEvent loadBandits() throws Exception {
        return EventLoader.loadEvents("events_choice_quests.json").stream()
                .filter(e -> "quest_village_bandits_1".equals(e.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Brak questa quest_village_bandits_1"));
    }

    @Test
    public void executeChoiceAdvancesTurnAndNeverPartial() throws Exception {
        GameEngine engine = new GameEngine();
        engine.load();
        GameEvent bandits = loadBandits();

        int turnsBefore = engine.getTurnCount();
        engine.executeChoice(bandits, bandits.getChoices().get(0));

        assertEquals(turnsBefore + 1, engine.getTurnCount(), "Wybór powinien zużyć jedną turę");
        assertNotEquals(EventResult.PARTIAL, engine.getLastResult(),
                "Questy z wyborem nie mają efektu pośredniego");
        assertTrue(engine.getLastResult() == EventResult.SUCCESS
                        || engine.getLastResult() == EventResult.FAIL,
                "Wynik musi być SUCCESS albo FAIL");
        assertNotNull(engine.getLastMessage());
        assertFalse(engine.getLastMessage().isEmpty(), "Powinien pojawić się komunikat wyniku");
    }

    @Test
    public void singleStageChoiceQuestIsCompletedAfterResolution() throws Exception {
        GameEngine engine = new GameEngine();
        engine.load();
        GameEvent bandits = loadBandits();

        engine.executeChoice(bandits, bandits.getChoices().get(0));

        // turnsUntilNext = 0 -> quest kończy się w tej samej turze niezależnie od wyniku
        assertTrue(engine.getCompletedQuests().contains("village_bandits"),
                "Jednoetapowy quest z wyborem powinien trafić do ukończonych");
    }
}
