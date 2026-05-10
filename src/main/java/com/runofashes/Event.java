package com.runofashes;

import java.util.ArrayList;
import java.util.List;

enum EventTag{
    HUNGER, HYDRATION, ENERGY, MORALE, HEALTH
}

public class Event {
    private String title;
    private String description;
    private List<EventAction> actions = new ArrayList<EventAction>();
    private List<EventTag> tags = new ArrayList<EventTag>();
    private int baseWeight;

    public Event(String title, String description) {
        this.title = title;
        this.description = description;
        this.baseWeight = 1;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getbaseWeight() { return baseWeight; }
    public List<EventAction> getActions() { return actions; }
    public List<EventTag> getTags() { return tags; }

    public int calculateWeight(Player player) {
        int currentWeight = this.baseWeight;
        if (tags.contains(EventTag.HUNGER) && player.getHunger() < 30){
            currentWeight+=1;
        }else if (tags.contains(EventTag.HYDRATION) && player.getHydration() < 30){
            currentWeight+=1;
        }else if (tags.contains(EventTag.ENERGY) && player.getEnergy() < 30){
            currentWeight+=1;
        }else if (tags.contains(EventTag.MORALE) && player.getMorale() < 30){
            currentWeight+=1;
        }else if (tags.contains(EventTag.HEALTH) && player.getHealth() < 30){
            currentWeight+=1;
        }
        return currentWeight+=1;
    }

    public void addAction(EventAction action){
        actions.add(action);
    }
    public void addTag(EventTag  tag){
        tags.add(tag);
    }

    @Override
    public String toString() {
        return "EVENT: " + title + "\n" +
                "Opis: " + description + "\n" +
                "Dostępne akcje: " + actions.size();
    }
}
