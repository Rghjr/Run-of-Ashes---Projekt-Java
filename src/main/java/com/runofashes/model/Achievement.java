package com.runofashes.model;

public class Achievement {

    private String id;
    private String group;
    private int level;
    private String title;
    private String description;
    private boolean unlocked;

    public Achievement() {
        this.unlocked = false;
    }

    public Achievement(String id, String group, int level, String title, String description) {
        this.id = id;
        this.group = group;
        this.level = level;
        this.title = title;
        this.description = description;
        this.unlocked = false;
    }

    // --- Gettery ---
    public String getId() { return id; }
    public String getGroup() { return group; }
    public int getLevel() { return level; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isUnlocked() { return unlocked; }

    // --- Metody do zarządzania statusem ---
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public void unlock() {
        this.unlocked = true;
    }
}