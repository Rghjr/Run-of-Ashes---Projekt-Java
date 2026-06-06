package com.runofashes;

import com.runofashes.engine.AchievementManager;
import com.runofashes.model.Achievement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AchievementManagerTest {

    private AchievementManager manager;

    @BeforeEach
    public void setUp() {
        manager = new AchievementManager();
        manager.loadAchievements();
    }

    @Test
    public void testLoadAchievements() {
        assertEquals(100, manager.getTotalCount(), "Powinno załadować dokładnie 100 osiągnięć z pliku");

        assertEquals(0, manager.getUnlockedCount(), "Na starcie odblokowane powinno być 0 osiągnięć");
    }

    @Test
    public void testUnlockAchievement() {
        boolean firstTry = manager.unlockAchievement("wed_1");

        assertTrue(firstTry, "Pierwsze odblokowanie 'wed_1' powinno zwrócić true");
        assertEquals(1, manager.getUnlockedCount(), "Licznik odblokowanych powinien wzrosnąć do 1");

        boolean secondTry = manager.unlockAchievement("wed_1");

        assertFalse(secondTry, "Ponowne odblokowanie tego samego osiągnięcia powinno zwrócić false");
        assertEquals(1, manager.getUnlockedCount(), "Licznik nie powinien rosnąć przy powtórnym odblokowaniu");
    }

    @Test
    public void testUnlockNonExistentAchievement() {
        boolean result = manager.unlockAchievement("nieistniejace_id");
        assertFalse(result, "Odblokowanie nieistniejącego ID powinno zwrócić false");
        assertEquals(0, manager.getUnlockedCount(), "Licznik odblokowanych nie powinien wzrosnąć");
    }

    @Test
    public void testGetAchievementsByGroup() {
        Map<String, List<Achievement>> grouped = manager.getAchievementsByGroup();

        assertTrue(grouped.containsKey("Wędrowiec"), "Powinna istnieć grupa 'Wędrowiec'");
        assertTrue(grouped.containsKey("Ocalały"), "Powinna istnieć grupa 'Ocalały'");
        assertTrue(grouped.containsKey("Szczęściarz"), "Powinna istnieć grupa 'Szczęściarz'");

        assertEquals(10, grouped.get("Wędrowiec").size(), "Grupa Wędrowiec powinna mieć dokładnie 10 poziomów");
    }

    @Test
    public void testAchievementObjectState() {
        manager.unlockAchievement("szc_10");

        List<Achievement> all = manager.getAllAchievements();
        Achievement chałkokoń = all.stream()
                .filter(a -> a.getId().equals("szc_10"))
                .findFirst()
                .orElse(null);

        assertNotNull(chałkokoń, "Osiągnięcie 'szc_10' musi istnieć");
        assertTrue(chałkokoń.isUnlocked(), "Osiągnięcie powinno mieć status isUnlocked() = true");
        assertEquals("Tajemnica Chałko-konia", chałkokoń.getTitle(), "Tytuł powinien się zgadzać z JSONem");
    }
}
