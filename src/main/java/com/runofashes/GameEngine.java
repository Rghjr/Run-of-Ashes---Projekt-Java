package com.runofashes;

import java.util.*;
import java.util.stream.Collectors;

public class GameEngine {

    public enum EventResult { SUCCESS, PARTIAL, FAIL }

    private Player player = new Player();
    private int turnCount = 0;

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
    private TraitManager  traitManager  = new TraitManager();
    private Difficulty    difficulty    = Difficulty.NORMAL;

    // ══════════════════════════════════════════════════════════════════════════
    //  Init
    // ══════════════════════════════════════════════════════════════════════════

    public void load() throws Exception {
        foodEvents      = EventLoader.loadEvents("events_food.json");
        hydrationEvents = EventLoader.loadEvents("events_hydration.json");
        energyEvents    = EventLoader.loadEvents("events_energy.json");
        moraleEvents    = EventLoader.loadEvents("events_morale.json");
        moveEvents      = EventLoader.loadEvents("events_move.json");
        questEvents     = EventLoader.loadEvents("events_quests.json");
        rareEvents      = EventLoader.loadEvents("events_rare.json");
        endings         = EventLoader.loadEndings();

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
        // Specjalny event "przeczekaj turę"
        if ("WAIT_TURN".equals(event.getId())) {
            lastResult  = EventResult.SUCCESS;
            lastMessage = event.getSuccessMessage() != null
                    ? event.getSuccessMessage()
                    : "Czekasz. Czas płynie. Quest jest gotowy gdy wrócisz.";
            applyEffects(event.getEffects());
            statusManager.tick(player, turnCount);
            // BUG FIX 3: rollTriggers musi działać też podczas oczekiwania —
            // gracz siedzący w miejscu może dostać odwodnienie, gorączkę itp.
            statusManager.rollTriggers(player);
            traitManager.tick(player);
            player.addTime(event.getTimeCost());
            turnCount++;
            tickQuests();
            drawCards();
            return;
        }

        EventResult result = resolveResult(event);
        lastResult = result;

        switch (result) {
            case SUCCESS -> {
                Map<String, Integer> fx = event.getEffects();
                if (statusManager.hasHallucinations() && fx != null) {
                    Map<String, Integer> hallFx = new HashMap<>(fx);
                    hallFx.replaceAll((k, v) -> RNG.nextBoolean() ? v : (v > 0 ? -v / 2 : v * 2));
                    applyEffects(hallFx);
                } else {
                    applyEffects(fx);
                }

                if (event.getItemEffects() != null) {
                    event.getItemEffects().forEach((itemName, amount) -> {
                        try {
                            ItemType type  = ItemType.valueOf(itemName);
                            int      added = inventory.add(type, amount);
                            int overflow = amount - added;
                            if (overflow > 0) {
                                Map<String, Integer> itemFx = type.getImmediateEffects();
                                if (itemFx != null) {
                                    for (int i = 0; i < overflow; i++) applyEffects(itemFx);
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            System.out.println("Błąd: Nieznany przedmiot w JSON: " + itemName);
                        }
                    });
                }

                lastMessage = event.getSuccessMessage() != null ? event.getSuccessMessage() : "";
                handleQuestProgress(event);
            }
            case PARTIAL -> {
                Map<String, Integer> fx = event.getEffects();
                if (statusManager.hasHallucinations() && fx != null) {
                    Map<String, Integer> hallFx = new HashMap<>(fx);
                    hallFx.replaceAll((k, v) -> RNG.nextBoolean() ? v : (v > 0 ? -v / 2 : v * 2));
                    applyEffectsPartial(hallFx);
                } else {
                    applyEffectsPartial(fx);
                }

                if (event.getItemEffects() != null) {
                    event.getItemEffects().forEach((itemName, amount) -> {
                        if (RNG.nextBoolean()) {
                            try {
                                ItemType type     = ItemType.valueOf(itemName);
                                int      added    = inventory.add(type, amount);
                                int      overflow = amount - added;
                                if (overflow > 0) {
                                    Map<String, Integer> itemFx = type.getImmediateEffects();
                                    if (itemFx != null) {
                                        for (int i = 0; i < overflow; i++) applyEffects(itemFx);
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                System.out.println("Błąd: Nieznany przedmiot w JSON: " + itemName);
                            }
                        }
                    });
                }

                lastMessage = "Nie poszło idealnie — efekt był słabszy niż oczekiwałeś.";
                handleQuestProgress(event);
            }
            case FAIL -> {
                applyEffects(event.getFailEffects());
                lastMessage = event.getFailMessage() != null ? event.getFailMessage() : "";
                if (event.getQuestId() != null && event.getTurnsUntilNext() == 0) {
                    activeQuests.remove(event.getQuestId());
                    completedQuests.add(event.getQuestId());
                }
            }
        }

        if (event.isHiddenEffects() && event.getRevealMessage() != null) {
            lastMessage = lastMessage.isEmpty()
                    ? event.getRevealMessage()
                    : lastMessage + "\n\n" + event.getRevealMessage();
        }

        if (event.getDistanceCost() > 0) {
            player.addDistance(event.getDistanceCost());
            cancelLocalQuests(event.getQuestId());
        }

        statusManager.tick(player, turnCount);
        statusManager.rollTriggers(player);
        traitManager.tick(player);

        player.addTime(event.getTimeCost());
        turnCount++;
        tickQuests();
        drawCards();
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

        // BUG FIX 2: event.getFailChance() podwyższa próg PARTIAL/FAIL,
        // zwiększając szansę na porażkę dla ryzykownych eventów.
        // Clampujemy żeby próg PARTIAL nie przekroczył progu SUCCESS.
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
        if (delta < 0 && (stat.equals("hunger") || stat.equals("hydration"))) {
            delta = (int) Math.round(delta * difficulty.getDrainMultiplier());
        }
        switch (stat) {
            case "health"    -> player.setHealth(player.getHealth()       + delta);
            case "hunger"    -> player.setHunger(player.getHunger()       + delta);
            case "hydration" -> player.setHydration(player.getHydration() + delta);
            case "energy"    -> player.setEnergy(player.getEnergy()       + delta);
            case "morale"    -> player.setMorale(player.getMorale()       + delta);
        }
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
        addWeighted(pool, availableNewQuests, 6);

        Collections.shuffle(pool, RNG);

        currentCards = new ArrayList<>();
        Set<String> usedIds = new HashSet<>();

        // BUG FIX 5: dodajemy WSZYSTKIE gotowe kontynuacje questów (do limitu 4),
        // nie tylko pierwszą — przy wielu równoczesnych questach gracz mógł utknąć.
        for (GameEvent cont : readyContinuations) {
            if (currentCards.size() >= 4) break;
            currentCards.add(cont);
            usedIds.add(cont.getId());
        }

        // Karta "Przeczekaj turę" jeśli quest z allowWait jeszcze nie gotowy
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
        // BUG FIX 4: używamy rzeczywistego max gracza per stat zamiast hardcodowanego 100.
        // Bez tego gracz z maxHunger=80 (HARD+GLUTTON) przy pełnym głodzie (80/80)
        // dostawał wagę 10+(100-80)=30 → masę kart jedzenia mimo że jest najedzony.
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
        return Math.max(5, base + traitManager.getWeightMod(category));
    }

    private int baseWeight(String category, int base) {
        return Math.max(5, base + traitManager.getWeightMod(category));
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
        // BUG FIX 1: inicjalizacja per-stat maksimów MUSI być pierwsza.
        // Bez tego wszystkie settery w tej metodzie clampują do domyślnego 100
        // zamiast do wartości wynikającej z trudności i cech.
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
    public int           getTurnCount()      { return turnCount; }
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

    public List<GameEvent> getVisibleQuestEvents() {
        List<GameEvent> all = new ArrayList<>(getReadyContinuations());
        all.addAll(getAvailableNewQuests());
        return all;
    }

    public Map<String, QuestState> getActiveQuests() {
        return Collections.unmodifiableMap(activeQuests);
    }

    public Set<String> getCompletedQuests() {
        return Collections.unmodifiableSet(completedQuests);
    }

    public GameEvent getQuestEvent(String questId, int stage) {
        return questEventMap.get(questId + "_" + stage);
    }

    public boolean hasActiveLocalQuests(String currentQuestId) {
        return activeQuests.values().stream()
                .anyMatch(qs -> qs.isLocal() && !qs.getQuestId().equals(currentQuestId));
    }

    public List<GameEvent> getFoodEvents()      { return foodEvents; }
    public List<GameEvent> getHydrationEvents() { return hydrationEvents; }
    public List<GameEvent> getEnergyEvents()    { return energyEvents; }
    public List<GameEvent> getMoraleEvents()    { return moraleEvents; }
    public List<GameEvent> getMoveEvents()      { return moveEvents; }
    public List<GameEvent> getRareEvents()      { return rareEvents; }
}