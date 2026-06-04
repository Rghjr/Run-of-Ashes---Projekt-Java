package com.runofashes.engine;

import com.runofashes.model.*;
import com.runofashes.utils.EventLoader;

import java.util.*;

public class GameEngine {

    private static final Random RNG = new Random();

    private final EventPools             eventPools      = new EventPools();
    private final QuestTracker           questTracker    = new QuestTracker();
    private final BiomeWeatherController environment     = new BiomeWeatherController(RNG);
    private final TraitManager           traitManager    = new TraitManager();
    private final EventResolver          eventResolver   = new EventResolver(RNG);
    private final EffectApplicator       effectApplicator = new EffectApplicator(RNG);
    private CardDrawer cardDrawer;

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
        applyDifficultyAndTraits();
        addStarterItems();
        drawCards();
    }

    public void configure(Difficulty diff, Collection<Trait> traits) {
        this.difficulty = diff;
        this.traitManager.setTraits(traits);
    }

    public void executeEvent(GameEvent event) {
        if ("WAIT_TURN".equals(event.getId())) {
            lastResult  = EventResult.SUCCESS;
            lastMessage = event.getSuccessMessage() != null
                    ? event.getSuccessMessage()
                    : "Czekasz. Czas płynie. Quest jest gotowy gdy wrócisz.";
            effectApplicator.applyEffects(event.getEffects(), player,
                    environment.getCurrentBiome(), difficulty);
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
                    player.addDistance(event.getDistanceCost());
                    appendQuestCancelMessage(questTracker.cancelLocalQuests(event.getQuestId()));
                }
            }
            case PARTIAL -> {
                effectApplicator.applyEffectsPartial(
                        effectApplicator.applyHallucinations(event.getEffects(), statusManager),
                        player, biome, difficulty);
                effectApplicator.processItemEffects(event.getItemEffects(), true,
                        inventory, player, biome, difficulty);
                lastMessage = isDepressed
                        ? "Nawet gdy coś się udaje, smakuje to jak porażka."
                        : "Nie poszło idealnie — efekt był słabszy niż oczekiwałeś.";
                questTracker.handleProgress(event);
                if (event.getDistanceCost() != 0) {
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

        advanceTurn(event.getTimeCost());
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

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

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
}
