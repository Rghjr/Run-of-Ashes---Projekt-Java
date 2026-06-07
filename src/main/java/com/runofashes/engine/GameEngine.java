package com.runofashes.engine;

import com.runofashes.model.*;
import com.runofashes.utils.EventLoader;

import java.util.*;
import java.util.stream.Collectors;
import java.io.File;

public class GameEngine {

    private static final Random RNG = new Random();

    private final EventPools             eventPools      = new EventPools();
    private final QuestTracker           questTracker    = new QuestTracker();
    private final BiomeWeatherController environment     = new BiomeWeatherController(RNG);
    private final TraitManager           traitManager    = new TraitManager();
    private final EventResolver          eventResolver   = new EventResolver(RNG);
    private final EffectApplicator       effectApplicator = new EffectApplicator(RNG);
    private CardDrawer cardDrawer;
    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper().configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Player player = new Player();
    private int turnCount = 0;
    private double mainQuestWeight = 1;

    private EventResult lastResult  = EventResult.SUCCESS;
    private String      lastMessage = "";
    private List<GameEvent> currentCards = new ArrayList<>();

    private Inventory     inventory     = new Inventory();
    private StatusManager statusManager = new StatusManager();
    private Difficulty difficulty    = Difficulty.NORMAL;

    private final AchievementManager     achievementManager = new AchievementManager();
    private final StatisticsManager statsManager = new StatisticsManager();

    private int consecutiveMoves = 0;

    private String currentSaveFilename = "savegame.json";
    public void setSaveFilename(String filename) {
        this.currentSaveFilename = filename;
    }
    private boolean hasUnsavedProgress = false;

    public boolean hasUnsavedProgress() {
        return hasUnsavedProgress;
    }

    public void load() throws Exception {
        eventPools.load();
        achievementManager.loadAchievements();
        questTracker.init(eventPools.getQuestEventMap(), eventPools.getQuestEvents());
        cardDrawer = new CardDrawer(RNG, eventPools, questTracker, environment, traitManager);
        applyDifficultyAndTraits();
        addStarterItems();
        drawCards();
    }

    public void reset() {
        player        = new Player();
        turnCount     = 0;
        inventory     = new Inventory();
        statusManager = new StatusManager();
        questTracker.reset();
        environment.reset();
        lastMessage = "";
        lastResult  = EventResult.SUCCESS;
        mainQuestWeight = 1;
        statsManager.startNewRun(difficulty);
        applyDifficultyAndTraits();
        addStarterItems();
        drawCards();
        hasUnsavedProgress = false;
    }

    public void configure(Difficulty diff, Collection<Trait> traits) {
        this.difficulty = diff;
        this.traitManager.setTraits(traits);
    }

    public void executeEvent(GameEvent event) {
        hasUnsavedProgress = true;
        if ("WAIT_TURN".equals(event.getId())) {
            lastResult  = EventResult.SUCCESS;
            lastMessage = event.getSuccessMessage() != null
                    ? event.getSuccessMessage()
                    : "Czekasz. Czas płynie. Quest jest gotowy gdy wrócisz.";
            Map<String, Integer> statsBefore = snapshotStats();
            Map<ItemType, Integer> itemsBefore = snapshotItems();
            effectApplicator.applyEffects(event.getEffects(), player,
                    environment.getCurrentBiome(), difficulty);
            appendEffectSummary(statsBefore, itemsBefore);
            advanceTurn(event.getTimeCost());
            return;
        }

        if (event.getQuestId() != null && event.getQuestStage() == 1 && event.getRequiredStage() != null) {
            mainQuestWeight = 5;
        }

        EventResult result = eventResolver.resolve(event, player, traitManager, difficulty);
        lastResult = result;
        boolean isDepressed = player.getMorale() < 30;
        Biome biome = environment.getCurrentBiome();

        Map<String, Integer> statsBefore = snapshotStats();
        Map<ItemType, Integer> itemsBefore = snapshotItems();

        switch (lastResult) {
            case SUCCESS -> {
                effectApplicator.applyEffects(
                        effectApplicator.applyHallucinations(event.getEffects(), statusManager),
                        player, biome, difficulty);
                effectApplicator.processItemEffects(event.getItemEffects(), false,
                        inventory, player, biome, difficulty);
                lastMessage = pickMessage(isDepressed, event.getLowMoraleSuccessMessage(), event.getSuccessMessage());
                questTracker.handleProgress(event);
                if (event.getDistanceCost() != 0) {
                    consecutiveMoves++;
                    player.addDistance(event.getDistanceCost());
                    appendQuestCancelMessage(questTracker.cancelLocalQuests(event.getQuestId()));
                }
                recordQuestCompletion(event);
            }
            case PARTIAL -> {
                effectApplicator.applyEffectsPartial(
                        effectApplicator.applyHallucinations(event.getEffects(), statusManager),
                        player, biome, difficulty);
                effectApplicator.processItemEffects(event.getItemEffects(), true,
                        inventory, player, biome, difficulty);
                lastMessage = buildPartialMessage(event, isDepressed);
                questTracker.handleProgress(event);
                if (event.getDistanceCost() != 0) {
                    consecutiveMoves++;
                    player.addDistance(event.getDistanceCost());
                    appendQuestCancelMessage(questTracker.cancelLocalQuests(event.getQuestId()));
                }
            }
            case FAIL -> {
                effectApplicator.applyEffects(event.getFailEffects(), player, biome, difficulty);
                lastMessage = pickMessage(isDepressed, event.getLowMoraleFailMessage(), event.getFailMessage());
                questTracker.onQuestFail(event);
            }
        }

        if (event.isHiddenEffects() && event.getRevealMessage() != null) {
            lastMessage = lastMessage.isEmpty()
                    ? event.getRevealMessage()
                    : lastMessage + "\n\n" + event.getRevealMessage();
        }

        if (consecutiveMoves >= 3) {
            player.addDistance(50);
            consecutiveMoves = 0;
            lastMessage += "\n\nRozpędziłeś się! -50 km.";
        } else if (event.getDistanceCost() == 0) {
            consecutiveMoves = 0;
        }

        appendEffectSummary(statsBefore, itemsBefore);
        AchievementTracker.checkEventAchievements(this, event, lastResult);
        advanceTurn(event.getTimeCost());
    }

    /** Szansa powodzenia opcji wyboru (0.0–1.0) dla aktualnego stanu gracza — do UI. */
    public double getChoiceChance(EventChoice choice) {
        return eventResolver.choiceChance(choice, player, traitManager, difficulty);
    }

    /**
     * Rozstrzyga wybraną przez gracza opcję w wydarzeniu typu "wybór".
     * Wynik to wyłącznie SUKCES albo PORAŻKA (brak efektu pośredniego).
     */
    public void executeChoice(GameEvent event, EventChoice choice) {
        hasUnsavedProgress = true;

        if (event.getQuestId() != null && event.getQuestStage() == 1 && event.getRequiredStage() != null) {
            mainQuestWeight = 5;
        }

        boolean success = eventResolver.resolveChoice(choice, player, traitManager, difficulty);
        lastResult = success ? EventResult.SUCCESS : EventResult.FAIL;
        Biome biome = environment.getCurrentBiome();

        Map<String, Integer> statsBefore = snapshotStats();
        Map<ItemType, Integer> itemsBefore = snapshotItems();

        if (success) {
            effectApplicator.applyEffects(
                    effectApplicator.applyHallucinations(choice.getEffects(), statusManager),
                    player, biome, difficulty);
            effectApplicator.processItemEffects(choice.getItemEffects(), false,
                    inventory, player, biome, difficulty);
            lastMessage = choice.getSuccessMessage() != null ? choice.getSuccessMessage() : "";
            questTracker.handleProgress(event);
            if (event.getDistanceCost() != 0) {
                consecutiveMoves++;
                player.addDistance(event.getDistanceCost());
                appendQuestCancelMessage(questTracker.cancelLocalQuests(event.getQuestId()));
            }
            recordQuestCompletion(event);
        } else {
            effectApplicator.applyEffects(choice.getFailEffects(), player, biome, difficulty);
            effectApplicator.processItemEffects(choice.getFailItemEffects(), false,
                    inventory, player, biome, difficulty);
            lastMessage = choice.getFailMessage() != null ? choice.getFailMessage() : "";
            questTracker.onQuestFail(event);
        }

        if (consecutiveMoves >= 3) {
            player.addDistance(50);
            consecutiveMoves = 0;
            lastMessage += "\n\nRozpędziłeś się! -50 km.";
        } else if (event.getDistanceCost() == 0) {
            consecutiveMoves = 0;
        }

        appendEffectSummary(statsBefore, itemsBefore);
        AchievementTracker.checkEventAchievements(this, event, lastResult);
        advanceTurn(event.getTimeCost());
    }

    /** Zlicza ukończony quest w statystykach bieżącej rozgrywki (jak {@link QuestTracker#handleProgress}). */
    private void recordQuestCompletion(GameEvent event) {
        if (!"quest".equals(event.getCategory()) || event.getTurnsUntilNext() != 0) return;
        RunStatistics run = statsManager.getCurrentRun();
        if (run == null) return;
        if (event.isLocalQuest()) run.addLocalQuest();
        else run.addGeneralQuest();
    }

    private void advanceTurn(int timeCost) {
        environment.applyPerTurnEffects(player);
        statusManager.tick(player, turnCount, difficulty);
        statusManager.rollTriggers(player);
        traitManager.tick(player, difficulty);
        player.addTime(timeCost);
        turnCount++;
        mainQuestWeight += 0.5;
        questTracker.tick();
        environment.tick(player);
        AchievementTracker.checkStateAchievements(this);
        drawCards();
    }

    public void drawCards() {
        currentCards = cardDrawer.draw(player, mainQuestWeight);
    }

    private void appendQuestCancelMessage(String cancelMsg) {
        if (cancelMsg == null) return;
        lastMessage += lastMessage.isEmpty() ? cancelMsg : "\n\n" + cancelMsg;
    }

    private static String pickMessage(boolean depressed, String lowMoraleMsg, String normalMsg) {
        String msg = (depressed && lowMoraleMsg != null) ? lowMoraleMsg : normalMsg;
        return msg != null ? msg : "";
    }

    /**
     * Buduje komunikat dla wyniku połowicznego. Każde wydarzenie może mieć własny
     * {@code partialMessage} (oraz wariant {@code lowMoralePartialMessage}) w JSON —
     * używamy go w pierwszej kolejności. Gdy go brak, korzystamy z uniwersalnego zdania.
     */
    private static String buildPartialMessage(GameEvent event, boolean depressed) {
        String custom = pickMessage(depressed,
                event.getLowMoralePartialMessage(), event.getPartialMessage());
        if (custom != null && !custom.isEmpty()) {
            return custom;
        }
        return depressed
                ? "Nawet gdy coś się udaje, smakuje to jak porażka."
                : "Udało się tylko częściowo — efekt jest słabszy, niż liczyłeś.";
    }

    private static final String[] TRACKED_STATS = {"health", "hunger", "hydration", "energy", "morale"};

    private Map<String, Integer> snapshotStats() {
        Map<String, Integer> snap = new LinkedHashMap<>();
        for (String stat : TRACKED_STATS) {
            snap.put(stat, player.getStat(stat));
        }
        return snap;
    }

    private Map<ItemType, Integer> snapshotItems() {
        return new HashMap<>(inventory.getAllItems());
    }

    /** Dokleja do {@code lastMessage} czytelny bilans zmian statystyk i przedmiotów. */
    private void appendEffectSummary(Map<String, Integer> statsBefore, Map<ItemType, Integer> itemsBefore) {
        StringBuilder stats = new StringBuilder();
        for (String stat : TRACKED_STATS) {
            int delta = player.getStat(stat) - statsBefore.getOrDefault(stat, 0);
            if (delta != 0) {
                stats.append(statEmoji(stat)).append(' ')
                        .append(delta > 0 ? "+" : "").append(delta).append("   ");
            }
        }

        StringBuilder items = new StringBuilder();
        Map<ItemType, Integer> itemsAfter = inventory.getAllItems();
        for (ItemType type : ItemType.values()) {
            int delta = itemsAfter.getOrDefault(type, 0) - itemsBefore.getOrDefault(type, 0);
            if (delta != 0) {
                items.append(delta > 0 ? "+" : "").append(delta).append(' ')
                        .append(type.getLabel()).append("   ");
            }
        }

        StringBuilder summary = new StringBuilder();
        if (stats.length() > 0) {
            summary.append("Bilans:  ").append(stats.toString().trim());
        }
        if (items.length() > 0) {
            if (summary.length() > 0) summary.append('\n');
            summary.append("Przedmioty:  ").append(items.toString().trim());
        }
        if (summary.length() == 0) return;

        lastMessage = lastMessage.isEmpty()
                ? summary.toString()
                : lastMessage + "\n\n" + summary;
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
        hasUnsavedProgress = true;
        AchievementTracker.checkItemUsed(this, type);
        inventory.useItem(type, player, statusManager, turnCount);
        if (statsManager.getCurrentRun() != null) {
            statsManager.getCurrentRun().addItemUsed();
        }
    }

    public String buildBiomeInfo(Biome biome) {
        return environment.buildBiomeInfo(biome);
    }

    public String getCurrentStageName() {
        return GameStage.nameForDistance(player.getDistance());
    }

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
    public Set<String> getCompletedQuests() { return questTracker.getCompletedQuests(); }
    public Set<String> getUnlockedIds() { return achievementManager.getUnlockedIds(); }

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }
    public StatisticsManager getStatsManager() { return statsManager; }

    public String getEndingText() {
        String stat = player.getDeadStat();
        return stat == null ? "" : EventLoader.pickEnding(eventPools.getEndings(), stat);
    }

    public Map<String, QuestState> getActiveQuests() {
        return questTracker.getActiveQuests();
    }

    public GameEvent getQuestEvent(String questId, int stage) {
        return questTracker.getQuestEvent(questId, stage);
    }

    public boolean hasActiveLocalQuests(String currentQuestId) {
        return questTracker.hasActiveLocalQuests(currentQuestId);
    }

    public Biome   getCurrentBiome()   { return environment.getCurrentBiome(); }
    public Weather getCurrentWeather() { return environment.getCurrentWeather(); }

    public int getTurnCount() { return turnCount; }

    public void saveGame() {
        GameState state = new GameState();
        state.stats = statsManager.getCurrentRun();

        // Dane gracza
        state.health = player.getHealth();
        state.hunger = player.getHunger();
        state.hydration = player.getHydration();
        state.energy = player.getEnergy();
        state.morale = player.getMorale();
        state.time = player.getTime();
        state.distance = player.getDistance();

        // Cechy
        state.activeTraitNames = traitManager.getActiveTraits().stream()
                .map(Enum::name).collect(Collectors.toList());

        // Ekwipunek i Questy
        state.inventoryItems = inventory.getAllItems();
        state.activeQuests = questTracker.getActiveQuests();
        state.completedQuestIds = questTracker.getCompletedQuests();
        state.unlockedAchievementIds = achievementManager.getUnlockedIds();

        try {
            File savesDir = new File("saves");
            if (!savesDir.exists()) {
                savesDir.mkdirs();
            }

            File saveFile = new File(savesDir, currentSaveFilename);
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(saveFile, state);

            hasUnsavedProgress = false;
        } catch (Exception e) {
            hasUnsavedProgress = false;
            e.printStackTrace();
        }
    }

    public void loadGame(String filename) throws Exception {
        this.currentSaveFilename = filename;

        File saveFile = new File("saves", filename);
        if (!saveFile.exists()) {
            throw new Exception("Brak pliku zapisu: " + saveFile.getPath());
        }

        GameState state = MAPPER.readValue(saveFile, GameState.class);

        player.loadFromState(state);

        inventory.loadFromMap(state.inventoryItems);

        if (state.unlockedAchievementIds != null) {
            for (String id : state.unlockedAchievementIds) {
                achievementManager.unlockAchievement(id);
            }
        }

        drawCards();
        hasUnsavedProgress = false;
    }

    public void deleteSaveFile() {
        if (currentSaveFilename != null) {
            File saveFile = new File("saves", currentSaveFilename);
            if (saveFile.exists()) {
                saveFile.delete();
                System.out.println("Zakończono bieg. Usunięto plik zapisu: " + currentSaveFilename);
            }
        }
    }
}
