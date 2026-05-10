package com.runofashes;

public class Player {

    private int health     = 100;
    private int hunger     = 100;
    private int hydration  = 100;
    private int energy     = 100;
    private int morale     = 100;
    private int time       = 0;
    private int distance   = 4000;  // km do Krakowa

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    public int getHealth()           { return health; }
    public void setHealth(int v)     { health     = clamp(v); }

    public int getHunger()           { return hunger; }
    public void setHunger(int v)     { hunger     = clamp(v); }

    public int getHydration()        { return hydration; }
    public void setHydration(int v)  { hydration  = clamp(v); }

    public int getEnergy()           { return energy; }
    public void setEnergy(int v)     { energy     = clamp(v); }

    public int getMorale()           { return morale; }
    public void setMorale(int v)     { morale     = clamp(v); }

    public int getTime()             { return time; }
    public void addTime(int hours)   { time += Math.max(0, hours); }

    public int getDistance()         { return distance; }
    public void addDistance(int km)  { distance = Math.max(0, distance - km); }

    public String getTimeFormatted() {
        int day  = (time / 24) + 1;
        int hour = time % 24;
        return String.format("Dzień %d,  %02d:00", day, hour);
    }

    public boolean hasWon() { return distance <= 0; }

    public String getDeadStat() {
        if (health    <= 0) return "health";
        if (hunger    <= 0) return "hunger";
        if (hydration <= 0) return "hydration";
        if (energy    <= 0) return "energy";
        if (morale    <= 0) return "morale";
        return null;
    }
}