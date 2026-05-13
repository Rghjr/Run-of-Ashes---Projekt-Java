package com.runofashes;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QuestStateTest {

    @Test
    public void testQuestTickAndReady() {
        // Quest potrzebuje 2 tur, żeby być gotowym
        QuestState quest = new QuestState("szukanie_leku", 2, 2, false);

        assertFalse(quest.isReady(), "Zadanie NIE powinno być gotowe na starcie");

        quest.tick(); // mija tura 1
        assertFalse(quest.isReady(), "Zadanie NIE powinno być gotowe po 1 turze");

        quest.tick(); // mija tura 2
        assertTrue(quest.isReady(), "Zadanie POWINNO być gotowe po odczekaniu 2 tur");

        quest.tick(); // mija tura 3 (test, czy wartości nie uciekają na minus)
        assertEquals(0, quest.getTurnsLeft(), "Licznik tur nie powinien spadać poniżej 0");
    }

    @Test
    public void testQuestLocalFlag() {
        // Zadanie lokalne (przypisane do miasta)
        QuestState localQuest = new QuestState("pomoc_w_miescie", 2, 1, true);
        assertTrue(localQuest.isLocal(), "Zadanie powinno być oznaczone jako lokalne");

        // Zadanie globalne (np. dostarczenie listu)
        QuestState globalQuest = new QuestState("list_od_biskupa", 2, 1, false);
        assertFalse(globalQuest.isLocal(), "Zadanie powinno być oznaczone jako globalne");
    }
}
