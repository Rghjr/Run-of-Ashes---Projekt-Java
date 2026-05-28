package com.runofashes.engine;

import com.runofashes.utils.EventLoader;
import com.runofashes.utils.WaitEventFactory;
import com.runofashes.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class GameEngine {

    public enum EventResult { SUCCESS, PARTIAL, FAIL }

    private Player player = new Player();
    private int turnCount = 0;
    private double mainQuestWeight = 1;

    private final Map<String, QuestState> activeQuests    = new LinkedHashMap<>();
    private final Set<String>             completedQuests = new HashSet<>();
    private Map<String, GameEvent>        questEventMap;

    private List<GameEvent> foodEvents;
    private List<GameEvent> hydrationEvents;
    private List<GameEvent> energyEvents;
    private List<GameEvent> moraleEvents;
    private List<GameEvent> moveEvents;
    private List<GameEvent> questEvents;
    private List<GameEvent> rareEvents;
    private Map<String, List<String>> endings;

    private EventResult lastResult  = EventResult.SUCCESS;
    private String      lastMessage = "";

    private List<GameEvent> currentCards = new ArrayList<>();

    private static final Random RNG = new Random();

    private Inventory     inventory     = new Inventory();
    private StatusManager statusManager = new StatusManager();
    private final TraitManager  traitManager  = new TraitManager();
    private Difficulty difficulty    = Difficulty.NORMAL;

    // ── Biom i pogoda ─────────────────────────────────────────────────────────
    private Biome   currentBiome   = Biome.STEPPE;
    private int     biomeStartDistance = 4000;
    private Weather currentWeather = Weather.CLEAR;
    private int     weatherTurnsLeft = 5;
    private String  biomeChangeMessage = "";
    private String  weatherChangeMessage = "";

    // ══════════════════════════════════════════════════════════════════════════
    //  Init
    // ══════════════════════════════════════════════════════════════════════════

    public void load() throws Exception {
        foodEvents      = EventLoader.loadEvents("events_food.json");
        hydrationEvents = EventLoader.loadEvents("events_hydration.json");
        energyEvents    = EventLoader.loadEvents("events_energy.json");
        moraleEvents    = EventLoader.loadEvents("events_morale.json");
        moveEvents      = EventLoader.loadEvents("events_move.json");
        rareEvents      = EventLoader.loadEvents("events_rare.json");
        endings         = EventLoader.loadEndings();

        questEvents = new ArrayList<>(EventLoader.loadEvents("events_quests.json"));
        List<GameEvent> stageQuests = EventLoader.loadEvents("events_stages_quests.json");
        questEvents.addAll(stageQuests);

        questEventMap = new HashMap<>();
        for (GameEvent e : questEvents) {
            if (e.getQuestId() != null) {
                questEventMap.put(e.getQuestId() + "_" + e.getQuestStage(), e);
            }
        }

        applyDifficultyAndTraits();
        addStarterItems();
        drawCards();
    }

    public void reset() {
        player        = new Player();
        turnCount     = 0;
        inventory     = new Inventory();
        statusManager = new StatusManager();

        activeQuests.clear();
        completedQuests.clear();
        lastMessage = "";
        lastResult  = EventResult.SUCCESS;

        currentBiome = Biome.STEPPE;
        biomeStartDistance = 4000;
        currentWeather = Weather.CLEAR;
        weatherTurnsLeft = 5;
        biomeChangeMessage   = "";
        weatherChangeMessage = "";

        // BUG FIX: mainQuestWeight nie był resetowany między partiami
        mainQuestWeight = 1;

        applyDifficultyAndTraits();
        addStarterItems();
        drawCards();
    }

    public void configure(Difficulty diff, Collection<Trait> traits) {
        this.difficulty = diff;
        this.traitManager.setTraits(traits);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Wykonanie eventu
    // ══════════════════════════════════════════════════════════════════════════

    public void executeEvent(GameEvent event) {
        if ("WAIT_TURN".equals(event.getId())) {
            lastResult  = EventResult.SUCCESS;
            lastMessage = event.getSuccessMessage() != null
                    ? event.getSuccessMessage()
                    : "Czekasz. Czas płynie. Quest jest gotowy gdy wrócisz.";
            applyEffects(event.getEffects());
            advanceTurn(event.getTimeCost());
            return;
        }

        if (event.getQuestId() != null && event.getQuestStage() == 1 && event.getRequiredStage() != null) {
            mainQuestWeight = 5;
        }

        EventResult result = resolveResult(event);

        lastResult = result;
        boolean isDepressed = player.getMorale() < 30;

        switch (lastResult) {
            case SUCCESS -> {
                applyEffects(applyHallucinations(event.getEffects()));
                processItemEffects(event.getItemEffects(), false);
                String msg = (isDepressed && event.getLowMoraleSuccessMessage() != null)
                        ? event.getLowMoraleSuccessMessage()
                        : event.getSuccessMessage();
                lastMessage = msg != null ? msg : "";
                handleQuestProgress(event);

                // BUG FIX: distanceCost i cancelLocalQuests tylko gdy event się powiódł.
                // Eventy takie jak move_forest_path (fail: "wyszedłeś tam gdzie wszedłeś")
                // nie powinny przesuwać gracza przy FAIL.
                // BUG FIX #2: zmieniono > 0 na != 0, żeby obsłużyć ujemny distanceCost
                // (np. am_kultyci_1 z distanceCost: -20 oznacza objazd — wcześniej był ignorowany)
                if (event.getDistanceCost() != 0) {
                    player.addDistance(event.getDistanceCost());
                    cancelLocalQuests(event.getQuestId());
                }
            }
            case PARTIAL -> {
                applyEffectsPartial(applyHallucinations(event.getEffects()));
                processItemEffects(event.getItemEffects(), true);
                lastMessage = isDepressed
                        ? "Nawet gdy coś się udaje, smakuje to jak porażka."
                        : "Nie poszło idealnie — efekt był słabszy niż oczekiwałeś.";
                handleQuestProgress(event);

                // PARTIAL: gracz się ruszył, lokalnych questów nie utrzyma
                if (event.getDistanceCost() != 0) {
                    player.addDistance(event.getDistanceCost());
                    cancelLocalQuests(event.getQuestId());
                }
            }
            case FAIL -> {
                applyEffects(event.getFailEffects());
                String failMsg = (isDepressed && event.getLowMoraleFailMessage() != null)
                        ? event.getLowMoraleFailMessage()
                        : event.getFailMessage();
                lastMessage = failMsg != null ? failMsg : "";
                if (event.getQuestId() != null && event.getTurnsUntilNext() == 0) {
                    activeQuests.remove(event.getQuestId());
                    completedQuests.add(event.getQuestId());
                }
                // FAIL: gracz się NIE rusza, lokalne questy zostają
            }
        }

        if (event.isHiddenEffects() && event.getRevealMessage() != null) {
            lastMessage = lastMessage.isEmpty()
                    ? event.getRevealMessage()
                    : lastMessage + "\n\n" + event.getRevealMessage();
        }

        advanceTurn(event.getTimeCost());
    }

    // ── Tura ─────────────────────────────────────────────────────────────────

    private void advanceTurn(int timeCost) {
        applyWeatherEffects();
        statusManager.tick(player, turnCount, difficulty);
        statusManager.rollTriggers(player);
        traitManager.tick(player, difficulty);
        player.addTime(timeCost);
        turnCount++;
        mainQuestWeight += 0.5;
        tickQuests();
        tickWeather();
        checkBiomeChange();
        drawCards();
    }

    // ── Biom i pogoda ─────────────────────────────────────────────────────────

    private void applyWeatherEffects() {
        currentWeather.getPerTurnEffects().forEach((stat, delta) -> {
            if (delta < 0 && (stat.equals("hunger") || stat.equals("hydration") || stat.equals("energy"))) {
                double biomeMult = currentBiome.getDecayMultiplier(stat);
                delta = (int) Math.round(delta * biomeMult);
            }
            player.modifyStat(stat, delta);
        });
    }

    private void tickWeather() {
        weatherTurnsLeft--;
        if (weatherTurnsLeft <= 0) {
            Weather next = Weather.rollNext(currentWeather, RNG);
            currentWeather   = next;
            weatherTurnsLeft = RNG.nextInt(next.getMaxTurns() - next.getMinTurns() + 1) + next.getMinTurns();
        } else {
            weatherChangeMessage = "";
        }
    }

    private void checkBiomeChange() {
        int distanceTraveledInBiome = biomeStartDistance - player.getDistance();
        if (distanceTraveledInBiome >= 400) {
            Biome newBiome = Biome.rollNext(currentBiome, RNG);
            currentBiome = newBiome;
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

    private String buildEffectSummary(java.util.Map<String, Integer> effects) {
        StringBuilder sb = new StringBuilder();
        effects.forEach((stat, val) -> sb.append(val > 0 ? "+" : "").append(val).append(" ").append(statEmoji(stat)).append(" "));
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

    public String getCurrentStageName() {
        int d = player.getDistance();
        if (d > 2600) return "Azja Mniejsza";
        if (d > 1400) return "Góry";
        return "Europa";
    }

    // ── Halucynacje ───────────────────────────────────────────────────────────

    private Map<String, Integer> applyHallucinations(Map<String, Integer> fx) {
        if (fx == null || !statusManager.hasHallucinations()) return fx;
        Map<String, Integer> hallFx = new HashMap<>(fx);
        hallFx.replaceAll((k, v) -> RNG.nextBoolean() ? v : (v > 0 ? -v / 2 : v * 2));
        return hallFx;
    }

    // ── Itemy z kart ─────────────────────────────────────────────────────────

    /**
     * BUG FIX: ujemne wartości w itemEffects (np. BANDAGE: -1) były wcześniej przekazywane
     * do inventory.add(), co tworzyło ujemne stany lub cicho nie usuwało przedmiotu.
     * Teraz ujemne wartości wywołują inventory.consume(), a dodatnie inventory.add().
     */
    private void processItemEffects(Map<String, Integer> items, boolean isPartial) {
        if (items == null) return;
        items.forEach((itemName, amount) -> {
            if (isPartial && !RNG.nextBoolean()) return;
            try {
                ItemType type = ItemType.valueOf(itemName);
                if (amount > 0) {
                    int added    = inventory.add(type, amount);
                    int overflow = amount - added;
                    if (overflow > 0) {
                        Map<String, Integer> itemFx = type.getImmediateEffects();
                        if (itemFx != null) {
                            for (int i = 0; i < overflow; i++) applyEffects(itemFx);
                        }
                    }
                } else if (amount < 0) {
                    inventory.consume(type, -amount);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Błąd: Nieznany przedmiot w JSON: " + itemName);
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Resolving wyniku
    // ══════════════════════════════════════════════════════════════════════════

    private EventResult resolveResult(GameEvent event) {
        double penalty = statPenalty(player.getEnergy())    * 0.50
                + statPenalty(player.getHunger())    * 0.25
                + statPenalty(player.getHydration()) * 0.25
                + statPenalty(player.getHealth())    * 0.15
                + statPenalty(player.getMorale())    * 0.10;
        penalty = Math.min(1.0, penalty);

        double successThreshold = 0.25 + penalty * 0.30;
        double partialThreshold = 0.05 + penalty * 0.15;

        double mod = traitManager.getSuccessMod() + difficulty.getSuccessBonus();
        successThreshold = Math.max(0.05, successThreshold - mod);
        partialThreshold = Math.max(0.01, partialThreshold - mod);

        if (event.getFailChance() > 0) {
            partialThreshold = Math.min(successThreshold - 0.01,
                    partialThreshold + event.getFailChance());
        }

        double roll = RNG.nextDouble();
        if (roll >= successThreshold) return EventResult.SUCCESS;
        if (roll >= partialThreshold) return EventResult.PARTIAL;
        return EventResult.FAIL;
    }

    private double statPenalty(int statValue) {
        if (statValue >= 30) return 0.0;
        return 1.0 - statValue / 30.0;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Efekty
    // ══════════════════════════════════════════════════════════════════════════

    private void applyEffects(Map<String, Integer> fx) {
        if (fx == null) return;
        fx.forEach(this::applySingle);
    }

    private void applyEffectsPartial(Map<String, Integer> fx) {
        if (fx == null) return;
        fx.forEach((stat, delta) -> applySingle(stat, delta > 0 ? Math.max(1, delta / 2) : delta));
    }

    private void applySingle(String stat, int delta) {
        if (delta < 0 && (stat.equals("hunger") || stat.equals("hydration") || stat.equals("energy"))) {
            double biomeMult = currentBiome.getDecayMultiplier(stat);
            double diffMult  = difficulty.getDrainMultiplier();
            if (stat.equals("energy")) {
                delta = (int) Math.round(delta * biomeMult);
            } else {
                delta = (int) Math.round(delta * biomeMult * diffMult);
            }
        }
        player.modifyStat(stat, delta);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Questy
    // ══════════════════════════════════════════════════════════════════════════

    private void handleQuestProgress(GameEvent event) {
        if (event.getQuestId() == null) return;
        if (event.getTurnsUntilNext() > 0) {
            activeQuests.put(event.getQuestId(),
                    new QuestState(
                            event.getQuestId(),
                            event.getQuestStage() + 1,
                            event.getTurnsUntilNext(),
                            event.isLocalQuest(),
                            event.isAllowWait()
                    ));
        } else {
            activeQuests.remove(event.getQuestId());
            completedQuests.add(event.getQuestId());
        }
    }

    private void cancelLocalQuests(String currentQuestId) {
        boolean removedAny = false;
        Iterator<Map.Entry<String, QuestState>> it = activeQuests.entrySet().iterator();
        while (it.hasNext()) {
            QuestState qs = it.next().getValue();
            if (qs.isLocal() && !qs.getQuestId().equals(currentQuestId)) {
                it.remove();
                removedAny = true;
            }
        }
        if (removedAny) {
            lastMessage += lastMessage.isEmpty()
                    ? "Opuściłeś lokację. Inne lokalne zadania zostały anulowane."
                    : "\n\nOpuściłeś lokację. Inne lokalne zadania zostały anulowane.";
        }
    }

    private void tickQuests() {
        activeQuests.values().forEach(QuestState::tick);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Losowanie kart
    // ══════════════════════════════════════════════════════════════════════════

    public void drawCards() {
        int currentHour = player.getTime() % 24;

        List<GameEvent> readyContinuations = getReadyContinuations();
        List<GameEvent> availableNewQuests = getAvailableNewQuests();

        List<GameEvent> pool = buildWeightedPool(currentHour);

        List<GameEvent> mainQuests = new ArrayList<>();
        List<GameEvent> sideQuests = new ArrayList<>();
        String currentStage = getCurrentStageName();

        for (GameEvent eq : availableNewQuests) {
            if (eq.getRequiredStage() != null) {
                if (eq.getRequiredStage().equals(currentStage)) {
                    mainQuests.add(eq);
                }
            } else {
                sideQuests.add(eq);
            }
        }
        addWeighted(pool, sideQuests, 6);
        addWeighted(pool, mainQuests, (int)mainQuestWeight);

        Collections.shuffle(pool, RNG);

        currentCards = new ArrayList<>();
        Set<String> usedIds = new HashSet<>();

        for (GameEvent cont : readyContinuations) {
            if (currentCards.size() >= 4) break;
            currentCards.add(cont);
            usedIds.add(cont.getId());
        }

        GameEvent waitCard = buildWaitCard();
        if (waitCard != null && currentCards.size() < 4) {
            currentCards.add(waitCard);
            usedIds.add(waitCard.getId());
        }

        for (GameEvent e : pool) {
            if (currentCards.size() >= 4) break;
            if (!usedIds.contains(e.getId())) {
                currentCards.add(e);
                usedIds.add(e.getId());
            }
        }
    }

    private GameEvent buildWaitCard() {
        for (QuestState qs : activeQuests.values()) {
            if (!qs.isReady() && qs.isAllowWait()) {
                return WaitEventFactory.create(qs.getTurnsLeft());
            }
        }
        return null;
    }

    private List<GameEvent> buildWeightedPool(int hour) {
        List<GameEvent> pool = new ArrayList<>();
        addWeighted(pool, filterByTime(foodEvents,      hour), weight("food",      player.getHunger(),    player.getMaxHunger()));
        addWeighted(pool, filterByTime(hydrationEvents, hour), weight("hydration", player.getHydration(), player.getMaxHydration()));
        addWeighted(pool, filterByTime(energyEvents,    hour), weight("energy",    player.getEnergy(),    player.getMaxEnergy()));
        addWeighted(pool, filterByTime(moraleEvents,    hour), weight("morale",    player.getMorale(),    player.getMaxMorale()));
        addWeighted(pool, filterByTime(moveEvents,      hour), baseWeight("move",  40));
        addWeighted(pool, rareEvents,                          baseWeight("rare",  5));
        return pool;
    }

    private int weight(String category, int statValue, int maxStat) {
        int base = 10 + (maxStat - statValue);
        int mod  = traitManager.getWeightMod(category)
                + currentBiome.getEventWeightMods().getOrDefault(category, 0)
                + currentWeather.getEventWeightMods().getOrDefault(category, 0);
        return Math.max(5, base + mod);
    }

    private int baseWeight(String category, int base) {
        int mod = traitManager.getWeightMod(category)
                + currentBiome.getEventWeightMods().getOrDefault(category, 0)
                + currentWeather.getEventWeightMods().getOrDefault(category, 0);
        return Math.max(5, base + mod);
    }

    private List<GameEvent> filterByTime(List<GameEvent> events, int hour) {
        if (events == null) return List.of();
        return events.stream()
                .filter(e -> e.isAvailableAt(hour))
                .collect(Collectors.toList());
    }

    private void addWeighted(List<GameEvent> pool, List<GameEvent> events, int weight) {
        if (events == null || events.isEmpty()) return;
        int copies = Math.max(1, weight / 10);
        for (int i = 0; i < copies; i++) pool.addAll(events);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Quest helpers
    // ══════════════════════════════════════════════════════════════════════════

    private List<GameEvent> getReadyContinuations() {
        List<GameEvent> visible = new ArrayList<>();
        for (QuestState qs : activeQuests.values()) {
            if (qs.isReady()) {
                GameEvent next = questEventMap.get(qs.getQuestId() + "_" + qs.getNextStage());
                if (next != null) visible.add(next);
            }
        }
        return visible;
    }

    private List<GameEvent> getAvailableNewQuests() {
        List<GameEvent> visible = new ArrayList<>();
        for (GameEvent e : questEvents) {
            if (e.getQuestStage() == 1
                    && !activeQuests.containsKey(e.getQuestId())
                    && !completedQuests.contains(e.getQuestId())) {
                visible.add(e);
            }
        }
        return visible;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Setup helpers
    // ══════════════════════════════════════════════════════════════════════════

    private void applyDifficultyAndTraits() {
        player.initMaxStats(difficulty, traitManager.getActiveTraits());

        int bonus = difficulty.getStartStatBonus();
        if (bonus != 0) {
            player.setHealth(player.getHealth()       + bonus);
            player.setHunger(player.getHunger()       + bonus);
            player.setHydration(player.getHydration() + bonus);
            player.setEnergy(player.getEnergy()       + bonus);
            player.setMorale(player.getMorale()       + bonus);
        }
        traitManager.applyStartBonuses(player);
    }

    private void addStarterItems() {
        inventory.add(ItemType.WATER,      1);
        inventory.add(ItemType.DRIED_MEAT, 1);
        inventory.add(ItemType.BANDAGE,    1);
    }

    public void useItem(ItemType type) {
        inventory.useItem(type, player, statusManager, turnCount);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Gettery publiczne
    // ══════════════════════════════════════════════════════════════════════════

    public Player        getPlayer()         { return player; }
    public EventResult   getLastResult()     { return lastResult; }
    public String        getLastMessage()    { return lastMessage; }
    public boolean       isGameOver()        { return player.getDeadStat() != null; }
    public boolean       hasWon()            { return player.hasWon(); }
    public Difficulty    getDifficulty()     { return difficulty; }
    public TraitManager  getTraitManager()   { return traitManager; }
    public Inventory     getInventory()      { return inventory; }
    public StatusManager getStatusManager()  { return statusManager; }

    public List<GameEvent> getCurrentCards() { return Collections.unmodifiableList(currentCards); }

    public String getEndingText() {
        String stat = player.getDeadStat();
        return stat == null ? "" : EventLoader.pickEnding(endings, stat);
    }

    public Map<String, QuestState> getActiveQuests() {
        return Collections.unmodifiableMap(activeQuests);
    }

    public GameEvent getQuestEvent(String questId, int stage) {
        return questEventMap.get(questId + "_" + stage);
    }

    public boolean hasActiveLocalQuests(String currentQuestId) {
        return activeQuests.values().stream()
                .anyMatch(qs -> qs.isLocal() && !qs.getQuestId().equals(currentQuestId));
    }

    public Biome   getCurrentBiome()   { return currentBiome; }
    public Weather getCurrentWeather() { return currentWeather; }
}