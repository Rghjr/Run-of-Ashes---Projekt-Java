package com.runofashes.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runofashes.model.Achievement;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class AchievementManager {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Map<String, Achievement> achievements = new LinkedHashMap<>();

    /**
     * Wczytuje osiągnięcia z pliku JSON przy użyciu biblioteki Jackson.
     */
    public void loadAchievements() {
        String path = "/com/runofashes/achievements.json";

        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Nie znaleziono pliku: " + path);
            }

            List<Achievement> loadedList = MAPPER.readValue(is, new TypeReference<List<Achievement>>() {});

            for (Achievement a : loadedList) {
                achievements.put(a.getId(), a);
            }
            System.out.println("Załadowano " + achievements.size() + " osiągnięć.");

        } catch (Exception e) {
            System.err.println("Błąd podczas ładowania osiągnięć z " + path + ": " + e.getMessage());
        }
    }

    /**
     * Odblokowuje osiągnięcie o podanym ID.
     * Zwraca true, jeśli osiągnięcie zostało WŁAŚNIE odblokowane.
     */
    public boolean unlockAchievement(String id) {
        Achievement a = achievements.get(id);
        if (a != null && !a.isUnlocked()) {
            a.unlock();
            System.out.println("🏆 ODBLOKOWANO OSIĄGNIĘCIE: " + a.getTitle() + " (" + a.getGroup() + ")");
            // TODO: Zapisz stan do pliku konfiguracyjnego gracza
            return true;
        }
        return false;
    }

    public Set<String> getUnlockedIds() {
        return achievements.values().stream()
                .filter(Achievement::isUnlocked)
                .map(Achievement::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Zwraca wszystkie osiągnięcia w formie płaskiej listy.
     */
    public List<Achievement> getAllAchievements() {
        return new ArrayList<>(achievements.values());
    }

    /**
     * Zwraca osiągnięcia pogrupowane po kategorii.
     */
    public Map<String, List<Achievement>> getAchievementsByGroup() {
        return achievements.values().stream()
                .collect(Collectors.groupingBy(
                        Achievement::getGroup,
                        LinkedHashMap::new, // Zachowuje kolejność grup
                        Collectors.toList()
                ));
    }

    /**
     * Zwraca liczbę odblokowanych osiągnięć.
     */
    public int getUnlockedCount() {
        return (int) achievements.values().stream().filter(Achievement::isUnlocked).count();
    }

    /**
     * Zwraca łączną liczbę osiągnięć w grze.
     */
    public int getTotalCount() {
        return achievements.size();
    }
}