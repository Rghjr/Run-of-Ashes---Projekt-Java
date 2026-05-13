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

    private Inventory inventory = new Inventory();
    private StatusManager statusManager = new StatusManager();
    // ── Init ─────────────────────────────────────────────────────────────────

    public void load() throws Exception {
        foodEvents       = EventLoader.loadEvents("events_food.json");
        hydrationEvents  = EventLoader.loadEvents("events_hydration.json");
        energyEvents     = EventLoader.loadEvents("events_energy.json");
        moraleEvents     = EventLoader.loadEvents("events_morale.json");
        moveEvents       = EventLoader.loadEvents("events_move.json");
        questEvents      = EventLoader.loadEvents("events_quests.json");
        rareEvents       = EventLoader.loadEvents("events_rare.json");
        endings          = EventLoader.loadEndings();
        addStarterItems();

        questEventMap = new HashMap<>();
        for (GameEvent e : questEvents) {
            if (e.getQuestId() != null) {
                questEventMap.put(e.getQuestId() + "_" + e.getQuestStage(), e);
            }
        }
        drawCards();
    }

    public void reset() {
        player = new Player();
        turnCount = 0;
        activeQuests.clear();
        completedQuests.clear();
        lastMessage = "";
        lastResult = EventResult.SUCCESS;

        inventory = new Inventory();
        statusManager = new StatusManager();
        addStarterItems();

        drawCards();
    }

    // ── Wykonanie eventu ─────────────────────────────────────────────────────

    public void executeEvent(GameEvent event) {
        EventResult result = resolveResult(event);
        lastResult = result;

        switch (result) {
            case SUCCESS -> {
                Map<String, Integer> fx = event.getEffects();
                if (statusManager.hasHallucinations() && fx != null) {
                    Map<String, Integer> hallucinatedEffects = new HashMap<>(fx);
                    hallucinatedEffects.replaceAll((k, v) -> RNG.nextBoolean() ? v : (v > 0 ? -v / 2 : v * 2));
                    applyEffects(hallucinatedEffects);
                } else {
                    applyEffects(fx);
                }

                if (event.getItemEffects() != null) {
                    event.getItemEffects().forEach((itemName, amount) -> {
                        try {
                            ItemType type = ItemType.valueOf(itemName);
                            int added = inventory.add(type, amount);
                            if (added == 0) {
                                // ekwipunek pełny — przelicz na stat bezpośrednio
                                Map<String, Integer> overflow = type.getImmediateEffects();
                                if (overflow != null) applyEffects(overflow);
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
                    Map<String, Integer> hallucinatedEffects = new HashMap<>(fx);
                    hallucinatedEffects.replaceAll((k, v) -> RNG.nextBoolean() ? v : (v > 0 ? -v / 2 : v * 2));
                    applyEffectsPartial(hallucinatedEffects);
                } else {
                    applyEffectsPartial(fx);
                }

                // Przy partial: item zdobyć można, ale z 50% szansą
                if (event.getItemEffects() != null) {
                    event.getItemEffects().forEach((itemName, amount) -> {
                        if (RNG.nextBoolean()) {
                            try {
                                ItemType type = ItemType.valueOf(itemName);
                                inventory.add(type, amount);
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
            if (lastMessage.isEmpty()) {
                lastMessage = event.getRevealMessage();
            } else {
                lastMessage += "\n\n" + event.getRevealMessage();
            }
        }

        if (event.getDistanceCost() > 0) {
            player.addDistance(event.getDistanceCost());
            cancelLocalQuests(event.getQuestId());
        }

        statusManager.tick(player, turnCount);
        statusManager.rollTriggers(player);

        player.addTime(event.getTimeCost());
        turnCount++;
        tickQuests();
        drawCards();
    }

    /**
     * Trzy wyniki: SUCCESS / PARTIAL / FAIL.
     *
     * Normalne statystyki (>30):  ~75% sukces, ~20% partial,  ~5% fail
     * Niska energia (≈0):         ~60% sukces, ~30% partial, ~10% fail
     * Wszystko na dnie:           ~45% sukces, ~35% partial, ~20% fail
     */
    private EventResult resolveResult(GameEvent event) {
        // Energia i podstawowe przeżycie to główne czynniki
        double penalty = statPenalty(player.getEnergy())    * 0.50
                + statPenalty(player.getHunger())    * 0.25
                + statPenalty(player.getHydration()) * 0.25
                + statPenalty(player.getHealth())    * 0.15
                + statPenalty(player.getMorale())    * 0.10;
        penalty = Math.min(1.0, penalty);

        // successThreshold: 0.25 → 0.55 (im wyższy próg, tym mniej sukcesów)
        // partialThreshold: 0.05 → 0.20
        double successThreshold = 0.25 + penalty * 0.30;
        double partialThreshold = 0.05 + penalty * 0.15;

        double roll = RNG.nextDouble();
        if (roll >= successThreshold) return EventResult.SUCCESS;  // 75% szans
        if (roll >= partialThreshold) return EventResult.PARTIAL;
        return EventResult.FAIL;
    }

    /** Kara rośnie gdy stat < 30: 0.0 przy stat=30, 1.0 przy stat=0. */
    private double statPenalty(int statValue) {
        if (statValue >= 30) return 0.0;
        return 1.0 - statValue / 30.0;
    }

    private void applyEffects(Map<String, Integer> fx) {
        if (fx == null) return;
        fx.forEach(this::applySingle);
    }

    private void applyEffectsPartial(Map<String, Integer> fx) {
        if (fx == null) return;
        fx.forEach((stat, delta) -> applySingle(stat, delta > 0 ? delta / 2 : delta));
    }

    private void applySingle(String stat, int delta) {
        switch (stat) {
            case "health"    -> player.setHealth(player.getHealth()       + delta);
            case "hunger"    -> player.setHunger(player.getHunger()       + delta);
            case "hydration" -> player.setHydration(player.getHydration() + delta);
            case "energy"    -> player.setEnergy(player.getEnergy()       + delta);
            case "morale"    -> player.setMorale(player.getMorale()       + delta);
        }
    }

    private void handleQuestProgress(GameEvent event) {
        if (event.getQuestId() == null) return;
        if (event.getTurnsUntilNext() > 0) {
            activeQuests.put(event.getQuestId(),
                    new QuestState(event.getQuestId(),
                            event.getQuestStage() + 1,
                            event.getTurnsUntilNext(),
                            event.isLocalQuest()));
        } else {
            // quest ukończony — zapamiętaj żeby się nie powtórzył
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
            if (lastMessage.isEmpty()) {
                lastMessage = "Opuściłeś lokację. Inne lokalne zadania zostały anulowane.";
            } else {
                lastMessage += "\n\nOpuściłeś lokację. Inne lokalne zadania zostały anulowane.";
            }
        }
    }

    private void tickQuests() {
        activeQuests.values().forEach(QuestState::tick);
    }

    // ── Losowanie kart ────────────────────────────────────────────────────────

    public void drawCards() {
        int currentHour = player.getTime() % 24;

        // Kontynuacje questów — zawsze widoczne gdy gotowe (gracz się zobowiązał)
        List<GameEvent> readyContinuations = getReadyContinuations();

        // Nowe questy (stage=1) wpadają do puli z BARDZO niską wagą — rzadka szansa
        List<GameEvent> availableNewQuests = getAvailableNewQuests();

        // Pula podstawowa z filtrowaniem po porze dnia
        List<GameEvent> pool = buildWeightedPool(currentHour);

        // Nowe questy: waga 6 → ~5% szans na pojawienie się w puli
        addWeighted(pool, availableNewQuests, 6);

        Collections.shuffle(pool, RNG);

        currentCards = new ArrayList<>();
        Set<String> usedIds = new HashSet<>();

        // Kontynuacja questa zawsze zajmuje slot 1 (gracz musi wiedzieć że czas)
        if (!readyContinuations.isEmpty()) {
            GameEvent cont = readyContinuations.get(0);
            currentCards.add(cont);
            usedIds.add(cont.getId());
        }

        for (GameEvent e : pool) {
            if (currentCards.size() >= 4) break;
            if (!usedIds.contains(e.getId())) {
                currentCards.add(e);
                usedIds.add(e.getId());
            }
        }
    }

    private List<GameEvent> buildWeightedPool(int hour) {
        List<GameEvent> pool = new ArrayList<>();
        addWeighted(pool, filterByTime(foodEvents,      hour), weight(player.getHunger()));
        addWeighted(pool, filterByTime(hydrationEvents, hour), weight(player.getHydration()));
        addWeighted(pool, filterByTime(energyEvents,    hour), weight(player.getEnergy()));
        addWeighted(pool, filterByTime(moraleEvents,    hour), weight(player.getMorale()));
        addWeighted(pool, filterByTime(moveEvents,      hour), 40);
        addWeighted(pool, rareEvents,                          5);
        return pool;
    }

    private List<GameEvent> filterByTime(List<GameEvent> events, int hour) {
        if (events == null) return List.of();
        return events.stream()
                .filter(e -> e.isAvailableAt(hour))
                .collect(Collectors.toList());
    }

    private int weight(int statValue) {
        return 10 + (100 - statValue);
    }

    private void addWeighted(List<GameEvent> pool, List<GameEvent> events, int weight) {
        if (events == null || events.isEmpty()) return;
        int copies = Math.max(1, weight / 10);
        for (int i = 0; i < copies; i++) pool.addAll(events);
    }

    // ── Quest helpers ─────────────────────────────────────────────────────────

    /** Kontynuacje aktywnych questów które są już gotowe (odczekały tury). */
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

    /** Nowe questy (stage=1) których gracz jeszcze nie zaczął i nie ukończył. */
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

    private void addStarterItems() {
        inventory.add(ItemType.WATER,      1);
        inventory.add(ItemType.DRIED_MEAT, 1);
        inventory.add(ItemType.BANDAGE,    1);
    }

    public void useItem(ItemType type) {
        inventory.useItem(type, player, statusManager, turnCount);
    }
    // ── Gettery ───────────────────────────────────────────────────────────────

    public Player       getPlayer()         { return player; }
    public int          getTurnCount()      { return turnCount; }
    public EventResult  getLastResult()     { return lastResult; }
    public String       getLastMessage()    { return lastMessage; }
    public boolean      isGameOver()        { return player.getDeadStat() != null; }
    public boolean      hasWon()            { return player.hasWon(); }

    public List<GameEvent> getCurrentCards() { return Collections.unmodifiableList(currentCards); }

    public String getEndingText() {
        String stat = player.getDeadStat();
        return stat == null ? "" : EventLoader.pickEnding(endings, stat);
    }

    /** Publiczne API dla UI — zwraca kontynuacje + dostępne nowe questy. */
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

    public List<GameEvent> getFoodEvents()       { return foodEvents; }
    public List<GameEvent> getHydrationEvents()  { return hydrationEvents; }
    public List<GameEvent> getEnergyEvents()     { return energyEvents; }
    public List<GameEvent> getMoraleEvents()     { return moraleEvents; }
    public List<GameEvent> getMoveEvents()       { return moveEvents; }
    public List<GameEvent> getRareEvents()       { return rareEvents; }

    public GameEvent getQuestEvent(String questId, int stage) {
        return questEventMap.get(questId + "_" + stage);
    }

    public boolean hasActiveLocalQuests(String currentQuestId) {
        return activeQuests.values().stream()
                .anyMatch(qs -> qs.isLocal() && !qs.getQuestId().equals(currentQuestId));
    }

    public Inventory     getInventory()     { return inventory; }
    public StatusManager getStatusManager() { return statusManager; }
}