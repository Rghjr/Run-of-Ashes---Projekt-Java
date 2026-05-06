package com.runofashes;

public class Player {

    // pola reprezentujące statystyki
    private int health = 100;
    private int hunger = 100;
    private int hydration = 100;
    private int energy = 100;
    private int morale = 100;

    // metoda do clampowania (ograniczanie do 0-100)
    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = clamp(health); }

    public int getHunger() { return hunger; }
    public void setHunger(int hunger) { this.hunger = clamp(hunger); }

    public int getHydration() { return hydration; }
    public void setHydration(int hydration) { this.hydration = clamp(hydration); }

    public int getEnergy() { return energy; }
    public void setEnergy(int energy) { this.energy = clamp(energy); }

    public int getMorale() { return morale; }
    public void setMorale(int morale) { this.morale = clamp(morale); }

    // wyświetlanie statusu w konsoli
    public void showStatus() {
        System.out.println("\n--- STATUS GRACZA ---");
        System.out.println("❤️ Zdrowie:     " + health + "/100");
        System.out.println("🍗 Głód:        " + hunger + "/100");
        System.out.println("💧 Nawodnienie: " + hydration + "/100");
        System.out.println("⚡ Energia:     " + energy + "/100");
        System.out.println("😊 Morale:      " + morale + "/100");
        System.out.println("---------------------\n");
    }
}