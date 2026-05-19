package com.runofashes;

import com.runofashes.model.QuestState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QuestStateTest {

    @Test
    public void testQuestTickAndReady() {
        QuestState quest = new QuestState("szukanie_leku", 2, 2, false);

        assertFalse(quest.isReady(), "Zadanie NIE powinno być gotowe na starcie");

        quest.tick();
        assertFalse(quest.isReady(), "Zadanie NIE powinno być gotowe po 1 turze");

        quest.tick();
        assertTrue(quest.isReady(), "Zadanie POWINNO być gotowe po odczekaniu 2 tur");

        quest.tick(); // sprawdzenie czy nie schodzi poniżej 0
        assertEquals(0, quest.getTurnsLeft(), "Licznik tur nie powinien spadać poniżej 0");
    }

    @Test
    public void testQuestLocalFlag() {
        QuestState localQuest = new QuestState("pomoc_w_miescie", 2, 1, true);
        assertTrue(localQuest.isLocal(), "Zadanie powinno być oznaczone jako lokalne");

        QuestState globalQuest = new QuestState("list_od_biskupa", 2, 1, false);
        assertFalse(globalQuest.isLocal(), "Zadanie powinno być oznaczone jako globalne");
    }

    @Test
    public void testAllowWaitFlag() {
        // Nowy konstruktor 5-argumentowy z allowWait
        QuestState waitQuest = new QuestState("jaskinia", 2, 3, true, true);
        assertTrue(waitQuest.isAllowWait(), "Quest powinien mieć flagę allowWait=true");
        assertFalse(waitQuest.isReady(), "Quest z turnsLeft=3 nie powinien być gotowy");

        QuestState noWaitQuest = new QuestState("list", 2, 3, false, false);
        assertFalse(noWaitQuest.isAllowWait(), "Quest powinien mieć flagę allowWait=false");
    }
}