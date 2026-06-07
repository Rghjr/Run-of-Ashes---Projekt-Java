package com.runofashes.model;

import java.util.Map;

/**
 * Pojedyncza opcja w wydarzeniu typu "wybór" (np. spotkanie z bandytami).
 * Każda opcja ma swoją szansę powodzenia zależną od statystyki gracza —
 * wynik to wyłącznie SUKCES albo PORAŻKA (brak efektów pośrednich).
 *
 * Pola JSON:
 *   "label"          – tekst opcji widoczny dla gracza
 *   "stat"           – statystyka wpływająca na szansę ("energy", "morale", ...);
 *                      pominięta = czysta szansa bazowa
 *   "baseChance"     – bazowa szansa powodzenia 0.0–1.0
 *   "statInfluence"  – ile pełna statystyka (100) dodaje do szansy (np. 0.4)
 *   "effects"        – zmiany statów przy sukcesie
 *   "itemEffects"    – przedmioty przy sukcesie (+ dodaje, − zabiera)
 *   "failEffects"    – zmiany statów przy porażce
 *   "failItemEffects"– utrata/zysk przedmiotów przy porażce (np. -1 utrata)
 *   "successMessage" / "failMessage" – opisy wyniku
 */
public class EventChoice {

    private String label;
    private String stat;
    private double baseChance = 0.5;
    private double statInfluence = 0.0;

    private Map<String, Integer> effects;
    private Map<String, Integer> itemEffects;

    private Map<String, Integer> failEffects;
    private Map<String, Integer> failItemEffects;

    private String successMessage;
    private String failMessage;

    public String getLabel()                         { return label; }
    public String getStat()                          { return stat; }
    public double getBaseChance()                    { return baseChance; }
    public double getStatInfluence()                 { return statInfluence; }

    public Map<String, Integer> getEffects()         { return effects; }
    public Map<String, Integer> getItemEffects()     { return itemEffects; }

    public Map<String, Integer> getFailEffects()     { return failEffects; }
    public Map<String, Integer> getFailItemEffects() { return failItemEffects; }

    public String getSuccessMessage()                { return successMessage; }
    public String getFailMessage()                   { return failMessage; }
}
