package com.runofashes.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runofashes.model.RunStatistics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StatisticsManager {

    private static final String STATS_FILE_PATH = System.getProperty("user.home") + "/runofashes_stats.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private List<RunStatistics> allRuns = new ArrayList<>();
    private RunStatistics currentRun;

    public StatisticsManager() {
        loadHistory();
    }

    public void startNewRun(com.runofashes.model.Difficulty diff) {
        currentRun = new RunStatistics(diff);
    }

    public RunStatistics getCurrentRun() {
        return currentRun;
    }

    public void finalizeAndSaveRun(boolean isWin, String causeOfDeath, com.runofashes.model.Player player) {
        if (currentRun != null) {
            currentRun.setEndGameState(isWin, causeOfDeath, player);
            allRuns.add(currentRun);
            saveHistory();
        }
    }

    public List<RunStatistics> getAllRuns() {
        return allRuns;
    }

    private void loadHistory() {
        try {
            File file = new File(STATS_FILE_PATH);
            if (file.exists()) {
                allRuns = MAPPER.readValue(file, new TypeReference<List<RunStatistics>>() {});
            }
        } catch (Exception e) {
            System.err.println("Nie udało się wczytać statystyk: " + e.getMessage());
        }
    }

    private void saveHistory() {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(new File(STATS_FILE_PATH), allRuns);
        } catch (Exception e) {
            System.err.println("Nie udało się zapisać statystyk: " + e.getMessage());
        }
    }
}