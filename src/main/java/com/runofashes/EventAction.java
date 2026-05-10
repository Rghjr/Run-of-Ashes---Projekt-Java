package com.runofashes;

import java.util.HashMap;
import java.util.Map;

enum StatType{
    HEALTH, HUNGER, HYDRATION, ENERGY, MORALE
}

public class EventAction {
    private String label;
    private int timeCost;
    private double risk;
    private Map<StatType, Integer> statEffects = new HashMap<>();
    private Map<String, Integer> itemEffects = new HashMap<>(); //w przyszłości string do podmiany na klase Item (addItemEffect)

    public EventAction(String label, int timeCost, double risk) {
        this.label= label;
        this.timeCost = timeCost;
        this.risk = risk;
    }

    public String getLabel() {
        return label;
    }

    public int getTimeCost() {
        return timeCost;
    }

    public double getRisk() {
        return risk;
    }

    public Map<StatType, Integer> getStatEffects() {
        return statEffects;
    }

    public Map<String, Integer> getItemEffects() {
        return itemEffects;
    }

    public void addStatEffect(StatType statType, int value){
        statEffects.put(statType, value);
    }

    public void addItemEffect(String item, int value){
        itemEffects.put(item, value);
    }

    @Override
    public String toString(){
        return "Action: " + label + " [Time: " + timeCost + " min, Risk: " + (risk * 100) + "%]";
    }
}
