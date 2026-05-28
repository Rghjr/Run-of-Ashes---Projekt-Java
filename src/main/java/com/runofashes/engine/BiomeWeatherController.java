package com.runofashes.engine;

import com.runofashes.model.Biome;
import com.runofashes.model.Player;
import com.runofashes.model.Weather;

import java.util.Random;

public class BiomeWeatherController {

    private Biome   currentBiome   = Biome.STEPPE;
    private int     biomeStartDistance = 4000;
    private Weather currentWeather = Weather.CLEAR;
    private int     weatherTurnsLeft = 5;

    private final Random rng;

    public BiomeWeatherController(Random rng) {
        this.rng = rng;
    }

    public void reset() {
        currentBiome         = Biome.STEPPE;
        biomeStartDistance   = 4000;
        currentWeather       = Weather.CLEAR;
        weatherTurnsLeft     = 5;
    }

    public void applyPerTurnEffects(Player player) {
        currentWeather.getPerTurnEffects().forEach((stat, delta) -> {
            if (delta < 0 && (stat.equals("hunger") || stat.equals("hydration") || stat.equals("energy"))) {
                double biomeMult = currentBiome.getDecayMultiplier(stat);
                delta = (int) Math.round(delta * biomeMult);
            }
            player.modifyStat(stat, delta);
        });
    }

    public void tick(Player player) {
        weatherTurnsLeft--;
        if (weatherTurnsLeft <= 0) {
            Weather next = Weather.rollNext(currentWeather, rng);
            currentWeather   = next;
            weatherTurnsLeft = rng.nextInt(next.getMaxTurns() - next.getMinTurns() + 1) + next.getMinTurns();
        }
        checkBiomeChange(player);
    }

    private void checkBiomeChange(Player player) {
        int distanceTraveledInBiome = biomeStartDistance - player.getDistance();
        if (distanceTraveledInBiome >= 400) {
            currentBiome       = Biome.rollNext(currentBiome, rng);
            biomeStartDistance = player.getDistance();
        }
    }

    public String buildBiomeInfo(Biome biome) {
        StringBuilder sb = new StringBuilder("Wpływ środowiska:\n");
        biome.getDecayMultipliers().forEach((stat, val) -> {
            if (val != 1.0) {
                String desc = val > 1.0 ? "szybszy spadek" : "wolniejszy spadek";
                sb.append(" • ").append(statEmoji(stat)).append(" ").append(desc).append(" (x").append(val).append(")\n");
            }
        });
        biome.getEventWeightMods().forEach((cat, val) -> {
            String catName = switch (cat) {
                case "food"      -> "🍗 jedzenia";
                case "hydration" -> "💧 wody";
                case "energy"    -> "⚡ odpoczynku";
                case "morale"    -> "😊 morale";
                case "move"      -> "👣 ruchu";
                case "rare"      -> "✨ rzadkich spotkań";
                default          -> cat;
            };
            String desc = val > 0 ? "Więcej kart" : "Mniej kart";
            sb.append(" • 🃏 ").append(desc).append(" ").append(catName).append("\n");
        });
        return sb.toString().trim();
    }

    private static String statEmoji(String stat) {
        return switch (stat) {
            case "health"    -> "❤";
            case "hunger"    -> "🍗";
            case "hydration" -> "💧";
            case "energy"    -> "⚡";
            case "morale"    -> "😊";
            default          -> stat;
        };
    }

    public int weightMod(String category) {
        return currentBiome.getEventWeightMods().getOrDefault(category, 0)
                + currentWeather.getEventWeightMods().getOrDefault(category, 0);
    }

    public Biome   getCurrentBiome()   { return currentBiome; }
    public Weather getCurrentWeather() { return currentWeather; }
}
