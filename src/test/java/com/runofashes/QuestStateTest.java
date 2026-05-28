package com.runofashes;

import com.runofashes.model.QuestState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QuestStateTest {

    @Test
    public void testQuestTickAndReady() {
        QuestState quest = new QuestState("szukanie_leku", 2, 2, false);

        assertFalse(quest.isReady());
        quest.tick();
        assertFalse(quest.isReady());
        quest.tick();
        assertTrue(quest.isReady());
        quest.tick();
        assertEquals(0, quest.getTurnsLeft());
    }

    @Test
    public void testQuestLocalFlag() {
        assertTrue(new QuestState("pomoc_w_miescie", 2, 1, true).isLocal());
        assertFalse(new QuestState("list_od_biskupa", 2, 1, false).isLocal());
    }

    @Test
    public void testAllowWaitFlag() {
        QuestState waitQuest = new QuestState("jaskinia", 2, 3, true, true);
        assertTrue(waitQuest.isAllowWait());
        assertFalse(waitQuest.isReady());

        QuestState noWaitQuest = new QuestState("list", 2, 3, false, false);
        assertFalse(noWaitQuest.isAllowWait());
    }

    // ── Nowe testy ────────────────────────────────────────────────────────────

    /**
     * Quest z turnsLeft=0 powinien być natychmiast gotowy bez wywołania tick().
     * Dotyczy jednoetapowych questów które finalizują się w tej samej turze.
     */
    @Test
    public void testQuestWithZeroTurnsIsImmediatelyReady() {
        QuestState quest = new QuestState("am_kultyci", 2, 0, false);
        assertTrue(quest.isReady(), "Quest z turnsLeft=0 powinien być od razu gotowy");
        assertEquals(0, quest.getTurnsLeft());
    }

    /**
     * tick() nigdy nie schodzi poniżej zera — wielokrotne wywołania są bezpieczne.
     * Zabezpiecza przed potencjalnym integer overflow lub nieoczekiwanym zachowaniem.
     */
    @Test
    public void testTickNeverGoesBelowZero() {
        QuestState quest = new QuestState("test", 2, 1, false);
        quest.tick(); // turnsLeft = 0, isReady
        quest.tick(); // nie powinno zejść do -1
        quest.tick();
        assertEquals(0, quest.getTurnsLeft(), "turnsLeft nie może być ujemny");
        assertTrue(quest.isReady());
    }

    /**
     * getNextStage() zwraca questStage + 1 — czyli etap który pojawi się jako kontynuacja.
     * Używane przez QuestPanel i getReadyContinuations() w GameEngine.
     */
    @Test
    public void testNextStageGetter() {
        QuestState quest = new QuestState("village_warn", 2, 3, false);
        assertEquals(2, quest.getNextStage(),
                "nextStage powinno wynosić 2 (etap kontynuacji)");

        QuestState stage3 = new QuestState("multi_stage", 4, 1, false);
        assertEquals(4, stage3.getNextStage());
    }

    /**
     * getQuestId() zwraca dokładnie ten sam string który był przekazany w konstruktorze.
     * Używane jako klucz w activeQuests (LinkedHashMap) — musi być spójne.
     */
    @Test
    public void testQuestIdGetter() {
        String id = "bishop_letter";
        QuestState quest = new QuestState(id, 2, 5, false, true);
        assertEquals(id, quest.getQuestId(), "QuestId powinno być identyczne z przekazanym");
    }

    /**
     * Dwa questy z tym samym questId ale różnymi parametrami są niezależne —
     * tick() jednego nie wpływa na drugiego.
     */
    @Test
    public void testTwoQuestsAreIndependent() {
        QuestState q1 = new QuestState("cave", 2, 3, true);
        QuestState q2 = new QuestState("cave", 2, 3, true);

        q1.tick();
        q1.tick();
        q1.tick();

        assertTrue(q1.isReady(), "q1 powinien być gotowy po 3 tickach");
        assertFalse(q2.isReady(), "q2 nie powinien być gotowy — tick() q1 go nie dotknął");
    }
}