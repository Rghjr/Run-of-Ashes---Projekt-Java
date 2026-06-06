package com.runofashes.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RunStatistics {

    private String date;
    private String difficulty;
    private boolean isWin;
    private String causeOfDeath;

    private int distanceTraveled;
    private int totalDistance = 4000;
    private int turnsSurvived;

    private int generalQuestsCompleted;
    private int localQuestsCompleted;
    private int itemsUsed;

    public RunStatistics() {
        // Pusty konstruktor dla Jacksona (wczytywanie z JSON)
    }

    public RunStatistics(Difficulty difficulty) {
        this.difficulty = difficulty.name();
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.distanceTraveled = 0;
        this.turnsSurvived = 0;
        this.generalQuestsCompleted = 0;
        this.localQuestsCompleted = 0;
        this.itemsUsed = 0;
        this.isWin = false;
        this.causeOfDeath = "Nieznany";
    }

    public void addGeneralQuest() { this.generalQuestsCompleted++; }
    public void addLocalQuest() { this.localQuestsCompleted++; }
    public void addItemUsed() { this.itemsUsed++; }

    public void setEndGameState(boolean isWin, String causeOfDeath, com.runofashes.model.Player player) {
        this.isWin = isWin;
        this.causeOfDeath = causeOfDeath;

        this.distanceTraveled = 4000 - player.getDistance();

        this.turnsSurvived = player.getTime();
    }

    public int getCompletionPercentage() {
        return (int) (((double) distanceTraveled / totalDistance) * 100);
    }

    public int getDaysSurvived() {
        return turnsSurvived / 24;
    }

    public int calculateScore() {
        int score = distanceTraveled / 10;
        score += generalQuestsCompleted * 50;
        score += localQuestsCompleted * 20;
        if (isWin) score += 1000;

        if (difficulty.equals("HARD")) score = (int)(score * 1.5);

        return score;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public boolean isWin() { return isWin; }
    public void setWin(boolean win) { isWin = win; }
    public String getCauseOfDeath() { return causeOfDeath; }
    public void setCauseOfDeath(String causeOfDeath) { this.causeOfDeath = causeOfDeath; }
    public int getDistanceTraveled() { return distanceTraveled; }
    public void setDistanceTraveled(int distanceTraveled) { this.distanceTraveled = distanceTraveled; }
    public int getTurnsSurvived() { return turnsSurvived; }
    public void setTurnsSurvived(int turnsSurvived) { this.turnsSurvived = turnsSurvived; }
    public int getGeneralQuestsCompleted() { return generalQuestsCompleted; }
    public void setGeneralQuestsCompleted(int generalQuestsCompleted) { this.generalQuestsCompleted = generalQuestsCompleted; }
    public int getLocalQuestsCompleted() { return localQuestsCompleted; }
    public void setLocalQuestsCompleted(int localQuestsCompleted) { this.localQuestsCompleted = localQuestsCompleted; }
    public int getItemsUsed() { return itemsUsed; }
    public void setItemsUsed(int itemsUsed) { this.itemsUsed = itemsUsed; }
}