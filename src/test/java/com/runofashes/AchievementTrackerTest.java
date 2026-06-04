package com.runofashes;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runofashes.engine.AchievementManager;
import com.runofashes.engine.AchievementTracker;
import com.runofashes.engine.EventResult;
import com.runofashes.engine.GameEngine;
import com.runofashes.model.GameEvent;
import com.runofashes.model.ItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class AchievementTrackerTest {

    private GameEngine engine;
    private AchievementManager manager;
    private ObjectMapper mapper;

    @BeforeEach
    public void setUp() throws Exception {
        engine = new GameEngine();
        engine.load();
        manager = engine.getAchievementManager();

        mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private GameEvent mockEvent(String id) throws Exception {
        return mapper.readValue("{\"id\":\"" + id + "\"}", GameEvent.class);
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    private boolean isUnlocked(String achievementId) {
        return manager.getAllAchievements().stream()
                .filter(a -> a.getId().equals(achievementId))
                .findFirst()
                .map(a -> a.isUnlocked())
                .orElse(false);
    }
    @Test
    public void testWędrowiec_Dystans() throws Exception {
        setPrivateField(engine.getPlayer(), "distance", 3500);
        AchievementTracker.checkStateAchievements(engine);
        assertTrue(isUnlocked("wed_3"), "Przejście 500 km powinno odblokować 'wed_3'");

        setPrivateField(engine.getPlayer(), "distance", 1500);
        AchievementTracker.checkStateAchievements(engine);
        assertTrue(isUnlocked("wed_6"), "Przejście 2500 km powinno odblokować 'wed_6'");
    }

    @Test
    public void testOcalały_CzasPrzetrwania() throws Exception {
        setPrivateField(engine, "turnCount", 84);
        AchievementTracker.checkStateAchievements(engine);
        assertTrue(isUnlocked("oca_2"), "Przetrwanie 84 tur powinno odblokować 'oca_2'");

        setPrivateField(engine, "turnCount", 600);
        AchievementTracker.checkStateAchievements(engine);
        assertTrue(isUnlocked("oca_5"), "Przetrwanie 600 tur powinno odblokować 'oca_5'");
    }

    @Test
    public void testZbieracz_Wydarzenia() throws Exception {
        AchievementTracker.checkEventAchievements(engine, mockEvent("hunt_bow"), EventResult.SUCCESS);
        assertTrue(isUnlocked("zbi_2"), "Zapolowanie z łukiem powinno odblokować 'zbi_2'");

        AchievementTracker.checkEventAchievements(engine, mockEvent("gory_pustelnik_1"), EventResult.SUCCESS);
        assertTrue(isUnlocked("zbi_8"), "Wypicie wywaru w górach powinno odblokować 'zbi_8'");
    }

    @Test
    public void testHandlowiec_Wydarzenia() throws Exception {
        AchievementTracker.checkEventAchievements(engine, mockEvent("trade_wine"), EventResult.SUCCESS);
        assertTrue(isUnlocked("han_3"), "Wymiana na wino powinna odblokować 'han_3'");

        AchievementTracker.checkEventAchievements(engine, mockEvent("eu_handlarz_1"), EventResult.SUCCESS);
        assertTrue(isUnlocked("han_8"), "Handel na czarnym rynku pod Krakowem powinien odblokować 'han_8'");
    }

    @Test
    public void testPoszukiwaczPrzygod_Questy() throws Exception {
        AchievementTracker.checkEventAchievements(engine, mockEvent("quest_map_merchant_2"), EventResult.SUCCESS);
        assertTrue(isUnlocked("pos_2"), "Zakończenie questa kupca powinno odblokować 'pos_2'");

        AchievementTracker.checkEventAchievements(engine, mockEvent("quest_burned_village_2"), EventResult.SUCCESS);
        assertTrue(isUnlocked("pos_7"), "Odnalezienie piwnicy spalonej wioski powinno odblokować 'pos_7'");
    }

    @Test
    public void testEksplorator_Regiony() throws Exception {
        AchievementTracker.checkEventAchievements(engine, mockEvent("am_kultyci_1"), EventResult.SUCCESS);
        assertTrue(isUnlocked("eks_3"), "Ominięcie kultystów powinno odblokować 'eks_3'");

        AchievementTracker.checkEventAchievements(engine, mockEvent("gory_namiot_1"), EventResult.PARTIAL);
        assertTrue(isUnlocked("eks_6"), "Przeszukanie namiotu górskiego powinno odblokować 'eks_6'");
    }

    @Test
    public void testMedyk_ItemyIWydarzenia() throws Exception {
        AchievementTracker.checkItemUsed(engine, ItemType.BANDAGE);
        assertTrue(isUnlocked("med_1"), "Użycie bandaża w ekwipunku powinno odblokować 'med_1'");

        AchievementTracker.checkEventAchievements(engine, mockEvent("nap_tree"), EventResult.SUCCESS);
        assertTrue(isUnlocked("med_5"), "Drzemka pod drzewem powinna odblokować 'med_5'");
    }

    @Test
    public void testNiezlomny_Morale() throws Exception {
        AchievementTracker.checkEventAchievements(engine, mockEvent("watch_sunrise"), EventResult.SUCCESS);
        assertTrue(isUnlocked("nie_2"), "Spojrzenie na wschód słońca powinno odblokować 'nie_2'");

        AchievementTracker.checkEventAchievements(engine, mockEvent("draw_map_in_dirt"), EventResult.SUCCESS);
        assertTrue(isUnlocked("nie_9"), "Rysowanie mapy w ziemi powinno odblokować 'nie_9'");
    }

    @Test
    public void testSzczesciarz_ZdarzeniaRzadkie() throws Exception {
        AchievementTracker.checkEventAchievements(engine, mockEvent("rare_plague_doctor"), EventResult.SUCCESS);
        assertTrue(isUnlocked("szc_1"), "Spotkanie doktora dżumy powinno odblokować 'szc_1'");

        AchievementTracker.checkEventAchievements(engine, mockEvent("rare_mad_baker"), EventResult.SUCCESS);
        assertTrue(isUnlocked("szc_7"), "Spotkanie piekarza powinno odblokować 'szc_7'");
    }

    @Test
    public void testWeteran_KoniecGry() throws Exception {
        manager.getAllAchievements().forEach(a -> a.setUnlocked(false));

        AchievementTracker.checkEndGame(engine, false);
        assertTrue(isUnlocked("wet_2"), "Przegrana gry (śmierć) powinna odblokować 'wet_2'");

        AchievementTracker.checkEndGame(engine, true);
        assertTrue(isUnlocked("wet_3"), "Zwycięskie dotarcie do Krakowa powinno odblokować 'wet_3'");
    }
}
