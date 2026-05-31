package com.runofashes.model;

/**
 * Poziomy trudności definiują ile i jakich cech może wybrać gracz.
 *
 * EASY   – do 5 cech pozytywnych, 0 negatywnych
 * NORMAL – tyle samo plusów co minusów (0+0, 1+1 ... 5+5)
 * HARD   – więcej negatywnych niż pozytywnych (saldo ujemne), bez górnego limitu par
 */
public enum Difficulty {

    EASY(
            "Łatwy",
            "fas-dove",
            "Do 5 cech pozytywnych, żadnych negatywnych. Gra jest bardziej wyrozumiała.",
            5, 0,
            10,
            0.10,
            0.7
    ),

    NORMAL(
            "Normalny",
            "fas-balance-scale",
            "Tyle samo plusów co minusów — od 0+0 do 5+5.",
            5, 5,
            0,
            0.0,
            1.0
    ),

    HARD(
            "Trudny",
            "fas-skull",
            "Więcej negatywnych niż pozytywnych. Każdy krok kosztuje więcej.",
            5, 5,
            -10,
            -0.10,
            1.3
    );

    // ─── Pola ────────────────────────────────────────────────────────────────

    private final String label;
    private final String emoji;
    private final String description;

    private final int maxPositive;
    private final int maxNegative;

    private final int    startStatBonus;
    private final double successBonus;
    private final double drainMultiplier;

    // ─── Konstruktor ─────────────────────────────────────────────────────────

    Difficulty(String label, String emoji, String description,
               int maxPositive, int maxNegative,
               int startStatBonus, double successBonus, double drainMultiplier) {
        this.label           = label;
        this.emoji           = emoji;
        this.description     = description;
        this.maxPositive     = maxPositive;
        this.maxNegative     = maxNegative;
        this.startStatBonus  = startStatBonus;
        this.successBonus    = successBonus;
        this.drainMultiplier = drainMultiplier;
    }

    // ─── Gettery ─────────────────────────────────────────────────────────────

    public String getLabel()           { return label; }
    public String getEmoji()           { return emoji; }
    public String getDescription()     { return description; }
    public int    getMaxPositive()     { return maxPositive; }
    public int    getMaxNegative()     { return maxNegative; }
    public int    getStartStatBonus()  { return startStatBonus; }
    public double getSuccessBonus()    { return successBonus; }
    public double getDrainMultiplier() { return drainMultiplier; }

    /**
     * Waliduje wybór cech:
     * EASY:   dowolna liczba pozytywnych (<=5), zero negatywnych
     * NORMAL: positiveCount == negativeCount, oba <= 5
     * HARD:   negativeCount > positiveCount, oba <= 5
     *
     * 0+0 zawsze legalne na każdym poziomie.
     */
    public boolean isValidSelection(int positiveCount, int negativeCount) {
        if (positiveCount == 0 && negativeCount == 0) return true;
        return switch (this) {
            case EASY   -> negativeCount == 0 && positiveCount <= maxPositive;
            case NORMAL -> positiveCount == negativeCount
                    && positiveCount <= maxPositive;
            case HARD   -> negativeCount > positiveCount
                    && negativeCount <= maxNegative
                    && positiveCount <= maxPositive;
        };
    }

    public String getRulesText() {
        return switch (this) {
            case EASY   -> "Do 5 cech pozytywnych. Żadnych negatywnych.";
            case NORMAL -> "Tyle samo plusów co minusów (0+0, 1+1, 2+2 ... 5+5).";
            case HARD   -> "Więcej negatywnych niż pozytywnych (np. 0+1, 1+2, 2+5 itp.).";
        };
    }
}