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

    /**
     * Pełny reset rozgrywki z zachowaniem wybranych cech i trudności.
     * Wywoływać po ekranie wyboru trudności/cech przed nową grą.
     */
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

    /** Ustawia trudność i cechy PRZED reset()/load(). */
    public void configure(Difficulty diff, Collection<Trait> traits) {
        this.difficulty = diff;
        this.traitManager.setTraits(traits);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Wykonanie eventu
    // ══════════════════════════════════════════════════════════════════════════

    public void executeEvent(GameEvent event) {
        // Specjalny event "przeczekaj turę" — nie robimy nic poza tickiem
        if ("WAIT_TURN".equals(event.getId())) {
            lastResult  = EventResult.SUCCESS;
            lastMessage = "Czekasz. Czas płynie. Quest jest gotowy gdy wrócisz.";
            statusManager.tick(player, turnCount);
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
                            // Każda sztuka powyżej maxStack jest od razu "wypijana/zjedzona"
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
        traitManager.tick(player);        // ← cechy per-tura

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

        // Bazowy próg: 0.25 (dobra forma) → 0.55 (kiepska forma)
        double successThreshold = 0.25 + penalty * 0.30;
        double partialThreshold = 0.05 + penalty * 0.15;

        // Modyfikatory z cech i trudności (obniżają progi → więcej sukcesów przy +, mniej przy -)
        double mod = traitManager.getSuccessMod() + difficulty.getSuccessBonus();
        successThreshold = Math.max(0.05, successThreshold - mod);
        partialThreshold = Math.max(0.01, partialThreshold - mod);

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
        fx.forEach((stat, delta) -> applySingle(stat, delta > 0 ? delta / 2 : delta));
    }

    private void applySingle(String stat, int delta) {
        // Trudność modyfikuje draining statów jedzenia/wody
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
                            event.isAllowWait()   // ← propagacja flagi z JSON
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

        // Slot 1: kontynuacja questa jeśli gotowa
        if (!readyContinuations.isEmpty()) {
            GameEvent cont = readyContinuations.get(0);
            currentCards.add(cont);
            usedIds.add(cont.getId());
        }

        // Slot 2 (opcjonalny): karta "Przeczekaj turę" jeśli jest aktywny quest
        // lokalny z allowWait=true który jeszcze nie jest gotowy
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

    /**
     * Buduje syntetyczną kartę "Przeczekaj turę przy queście" jeśli istnieje
     * aktywny quest z allowWait=true który jeszcze nie jest gotowy.
     */
    private GameEvent buildWaitCard() {
        for (QuestState qs : activeQuests.values()) {
            if (!qs.isReady() && qs.isAllowWait()) {
                // budujemy minimalny GameEvent z id WAIT_TURN
                return WaitEventFactory.create(qs.getTurnsLeft());
            }
        }
        return null;
    }

    private List<GameEvent> buildWeightedPool(int hour) {
        List<GameEvent> pool = new ArrayList<>();
        addWeighted(pool, filterByTime(foodEvents,      hour), weight("food",       player.getHunger()));
        addWeighted(pool, filterByTime(hydrationEvents, hour), weight("hydration",  player.getHydration()));
        addWeighted(pool, filterByTime(energyEvents,    hour), weight("energy",     player.getEnergy()));
        addWeighted(pool, filterByTime(moraleEvents,    hour), weight("morale",     player.getMorale()));
        addWeighted(pool, filterByTime(moveEvents,      hour), baseWeight("move",   40));
        addWeighted(pool, rareEvents,                          baseWeight("rare",   5));
        return pool;
    }

    private int weight(String category, int statValue) {
        int base = 10 + (100 - statValue);
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
        // Bonus startowy od trudności
        int bonus = difficulty.getStartStatBonus();
        if (bonus != 0) {
            player.setHealth(player.getHealth()       + bonus);
            player.setHunger(player.getHunger()       + bonus);
            player.setHydration(player.getHydration() + bonus);
            player.setEnergy(player.getEnergy()       + bonus);
            player.setMorale(player.getMorale()       + bonus);
        }
        // Bonusy startowe od cech
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