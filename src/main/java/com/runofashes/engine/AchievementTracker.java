package com.runofashes.engine;

import com.runofashes.model.GameEvent;
import com.runofashes.model.ItemType;
import com.runofashes.model.Player;

public class AchievementTracker {

    /**
     * Sprawdza osiągnięcia ciągłe (dystans, tury, 100% gry).
     * Wywoływane co turę.
     */
    public static void checkStateAchievements(GameEngine engine) {
        AchievementManager am = engine.getAchievementManager();
        Player p = engine.getPlayer();

        if (engine.getTurnCount() >= 1) am.unlockAchievement("wet_1");

        int traveled = 4000 - p.getDistance();
        if (traveled >= 50)   am.unlockAchievement("wed_1");
        if (traveled >= 250)  am.unlockAchievement("wed_2");
        if (traveled >= 500)  am.unlockAchievement("wed_3");
        if (traveled >= 1000) am.unlockAchievement("wed_4");
        if (traveled >= 2000) am.unlockAchievement("wed_5");
        if (traveled >= 2500) am.unlockAchievement("wed_6");
        if (traveled >= 3000) am.unlockAchievement("wed_7");
        if (traveled >= 3500) am.unlockAchievement("wed_8");
        if (traveled >= 3900) am.unlockAchievement("wed_9");

        int turns = engine.getTurnCount();
        if (turns >= 12)   am.unlockAchievement("oca_1");
        if (turns >= 84)   am.unlockAchievement("oca_2");
        if (turns >= 180)  am.unlockAchievement("oca_3");
        if (turns >= 360)  am.unlockAchievement("oca_4");
        if (turns >= 600)  am.unlockAchievement("oca_5");
        if (turns >= 900)  am.unlockAchievement("oca_6");
        if (turns >= 1200) am.unlockAchievement("oca_7");
        if (turns >= 1800) am.unlockAchievement("oca_8");
        if (turns >= 2400) am.unlockAchievement("oca_9");
        if (turns >= 4380) am.unlockAchievement("oca_10");

        if (am.getUnlockedCount() == 99 && !am.getAllAchievements().stream().filter(a -> a.getId().equals("wet_10")).findFirst().get().isUnlocked()) {
            am.unlockAchievement("wet_10");
        }
    }

    /**
     * Sprawdza powiązania wydarzeń. Wywoływane zaraz po rozpatrzeniu karty.
     */
    public static void checkEventAchievements(GameEngine engine, GameEvent event, EventResult result) {
        AchievementManager am = engine.getAchievementManager();
        String id = event.getId();

        if (result == EventResult.SUCCESS || result == EventResult.PARTIAL) {
            switch (id) {
                // --- ZBIERACZ ---
                case "drink_well":       am.unlockAchievement("zbi_1"); break;
                case "hunt_bow":         am.unlockAchievement("zbi_2"); break;
                case "forage_herbs":     am.unlockAchievement("zbi_3"); break;
                case "find_orchard":     am.unlockAchievement("zbi_4"); break;
                case "sleep_ruins":      am.unlockAchievement("zbi_5"); break;
                case "eu_zaraza_1":      am.unlockAchievement("zbi_6"); break;
                case "am_wrak_1":        am.unlockAchievement("zbi_7"); break;
                case "gory_pustelnik_1": am.unlockAchievement("zbi_8"); break;
                case "eu_ruiny_wawel_1":
                    am.unlockAchievement("zbi_9");
                    am.unlockAchievement("eks_9");
                    am.unlockAchievement("med_8");
                    break;
                case "gory_kopalnia_1":  am.unlockAchievement("zbi_10"); break;

                case "buy_village":         am.unlockAchievement("han_1"); break;
                case "barter_bread":        am.unlockAchievement("han_2"); break;
                case "trade_wine":          am.unlockAchievement("han_3"); break;
                case "trade_salt_for_food": am.unlockAchievement("han_4"); break;
                case "trade_item_water":    am.unlockAchievement("han_5"); break;
                case "trade_food":          am.unlockAchievement("han_6"); break;
                case "eu_krakow_patrol_1":  am.unlockAchievement("han_7"); break;
                case "eu_handlarz_1":       am.unlockAchievement("han_8"); break;
                case "rare_mongol_rider":
                    am.unlockAchievement("han_9");
                    am.unlockAchievement("szc_4");
                    break;
                case "move_caravan":        am.unlockAchievement("han_10"); break;

                case "quest_village_warn_2":   am.unlockAchievement("pos_1"); break;
                case "quest_map_merchant_2":   am.unlockAchievement("pos_2"); break;
                case "quest_letter_bishop_2":  am.unlockAchievement("pos_3"); break;
                case "quest_cave_2":           am.unlockAchievement("pos_4"); break;
                case "quest_wounded_soldier_2":am.unlockAchievement("pos_5"); break;
                case "quest_sick_child_2":     am.unlockAchievement("pos_6"); break;
                case "quest_burned_village_2": am.unlockAchievement("pos_7"); break;
                case "quest_runaway_monk_2":   am.unlockAchievement("pos_8"); break;
                case "quest_lone_guard_2":     am.unlockAchievement("pos_9"); break;
                case "quest_mystery_letter_2": am.unlockAchievement("pos_10"); break;

                case "am_woda_1":        am.unlockAchievement("eks_1"); break;
                case "am_karawana_2":    am.unlockAchievement("eks_2"); break;
                case "am_kultyci_1":     am.unlockAchievement("eks_3"); break;
                case "gory_lawina_1":    am.unlockAchievement("eks_4"); break;
                case "gory_szczelina_1": am.unlockAchievement("eks_5"); break;
                case "gory_namiot_1":    am.unlockAchievement("eks_6"); break;
                case "eu_kordon_1":
                case "eu_kordon_2":      am.unlockAchievement("eks_7"); break;
                case "eu_zaraza_2":      am.unlockAchievement("eks_8"); break;
                case "eu_ostatni_oddech_1": am.unlockAchievement("eks_10"); break;

                case "herb_tea":         am.unlockAchievement("med_2"); break;
                case "tend_blisters":    am.unlockAchievement("med_3"); break;
                case "cold_water_feet":
                case "cold_stream":      am.unlockAchievement("med_4"); break;
                case "nap_tree":         am.unlockAchievement("med_5"); break;
                case "candle_meditation":am.unlockAchievement("med_6"); break;
                case "rare_wise_woman":
                    am.unlockAchievement("med_7");
                    am.unlockAchievement("szc_5");
                    break;
                case "gory_wilki_1":     am.unlockAchievement("med_9"); break;
                case "drink_river_risky":am.unlockAchievement("med_10"); break;

                case "pray_cross":       am.unlockAchievement("nie_1"); break;
                case "watch_sunrise":    am.unlockAchievement("nie_2"); break;
                case "count_stars":      am.unlockAchievement("nie_3"); break;
                case "remember_family":  am.unlockAchievement("nie_4"); break;
                case "find_compatriot":  am.unlockAchievement("nie_5"); break;
                case "read_breviary":    am.unlockAchievement("nie_6"); break;
                case "build_altar":      am.unlockAchievement("nie_7"); break;
                case "carve_name_tree":  am.unlockAchievement("nie_8"); break;
                case "draw_map_in_dirt": am.unlockAchievement("nie_9"); break;
                case "sing_old_song":    am.unlockAchievement("nie_10"); break;

                case "rare_plague_doctor":  am.unlockAchievement("szc_1"); break;
                case "rare_dying_monk":     am.unlockAchievement("szc_2"); break;
                case "rare_abandoned_cart": am.unlockAchievement("szc_3"); break;
                case "rare_ghost_village":  am.unlockAchievement("szc_6"); break;
                case "rare_mad_baker":
                    am.unlockAchievement("szc_7");
                    am.unlockAchievement("szc_8");
                    break;
                case "find_old_coin":       am.unlockAchievement("szc_9"); break;
            }
        }
    }

    /**
     * Weryfikuje osiągnięcia polegające na bezpośrednim wykorzystaniu zasobu ekwipunku.
     */
    public static void checkItemUsed(GameEngine engine, ItemType type) {
        if (type.name().equals("BANDAGE")) {
            engine.getAchievementManager().unlockAchievement("med_1");
        }
    }

    /**
     * Sprawdza osiągnięcia weterana po dojściu gry do punktu końcowego.
     */
    public static void checkEndGame(GameEngine engine, boolean isWin) {
        AchievementManager am = engine.getAchievementManager();

        if (!isWin) {
            am.unlockAchievement("wet_2");
        } else {
            am.unlockAchievement("wed_10");
            am.unlockAchievement("wet_3");

            String diffName = engine.getDifficulty().name();
            if (diffName.equals("NORMAL") || diffName.equals("HARD")) {
                am.unlockAchievement("wet_4");
            }
            if (diffName.equals("HARD")) {
                am.unlockAchievement("wet_7");
            }

            if (engine.getTurnCount() < 100) {
                am.unlockAchievement("wet_8");
            }

            if (!engine.getTraitManager().getActiveTraits().isEmpty()) {
                am.unlockAchievement("wet_6");
            }
        }
    }
}