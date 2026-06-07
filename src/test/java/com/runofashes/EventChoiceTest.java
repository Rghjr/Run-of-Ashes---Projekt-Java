package com.runofashes;

import com.runofashes.model.EventChoice;
import com.runofashes.model.GameEvent;
import com.runofashes.utils.EventLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy modelu questów z wyborem (EventChoice) oraz ich deserializacji z JSON.
 */
public class EventChoiceTest {

    @Test
    public void newEventHasNoChoices() {
        assertFalse(new GameEvent().hasChoices(),
                "Świeży GameEvent bez pola choices nie jest questem z wyborem");
    }

    @Test
    public void choiceQuestsFileLoadsAndParsesChoices() throws Exception {
        List<GameEvent> events = EventLoader.loadEvents("events_choice_quests.json");
        assertFalse(events.isEmpty(), "Plik z questami z wyborem nie powinien być pusty");

        GameEvent bandits = events.stream()
                .filter(e -> "quest_village_bandits_1".equals(e.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Brak questa quest_village_bandits_1"));

        assertTrue(bandits.hasChoices());
        assertEquals(4, bandits.getChoices().size(), "Bandyci mają 4 opcje");

        EventChoice first = bandits.getChoices().get(0);
        assertNotNull(first.getLabel());
        assertEquals("energy", first.getStat());
        assertEquals(0.30, first.getBaseChance(), 1e-9);
        assertEquals(0.45, first.getStatInfluence(), 1e-9);
        assertNotNull(first.getSuccessMessage());
        assertNotNull(first.getFailMessage());
        assertNotNull(first.getEffects());
        assertNotNull(first.getFailEffects());
    }

    @Test
    public void everyChoiceQuestIsWellFormed() throws Exception {
        List<GameEvent> events = EventLoader.loadEvents("events_choice_quests.json");
        for (GameEvent e : events) {
            assertTrue(e.hasChoices(), e.getId() + " powinien mieć opcje wyboru");
            assertTrue(e.getChoices().size() >= 2,
                    e.getId() + " powinien mieć co najmniej 2 opcje");
            for (EventChoice c : e.getChoices()) {
                assertNotNull(c.getLabel(), e.getId() + ": opcja bez etykiety");
                assertNotNull(c.getSuccessMessage(), e.getId() + ": brak successMessage");
                assertNotNull(c.getFailMessage(), e.getId() + ": brak failMessage");
                assertTrue(c.getBaseChance() >= 0.0 && c.getBaseChance() <= 1.0,
                        e.getId() + ": baseChance poza zakresem [0,1]");
            }
        }
    }

    @Test
    public void normalQuestsHaveNoChoices() throws Exception {
        List<GameEvent> events = EventLoader.loadEvents("events_quests.json");
        assertTrue(events.stream().noneMatch(GameEvent::hasChoices),
                "Zwykłe questy nie powinny mieć opcji wyboru");
    }
}
