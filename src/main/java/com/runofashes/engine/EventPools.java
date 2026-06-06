package com.runofashes.engine;

import com.runofashes.model.GameEvent;
import com.runofashes.utils.EventLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventPools {

    private List<GameEvent> foodEvents;
    private List<GameEvent> hydrationEvents;
    private List<GameEvent> energyEvents;
    private List<GameEvent> moraleEvents;
    private List<GameEvent> moveEvents;
    private List<GameEvent> rareEvents;
    private List<GameEvent> questEvents;
    private Map<String, List<String>> endings;
    private Map<String, GameEvent> questEventMap;

    public void load() throws Exception {
        foodEvents      = EventLoader.loadEvents("events_food.json");
        hydrationEvents = EventLoader.loadEvents("events_hydration.json");
        energyEvents    = EventLoader.loadEvents("events_energy.json");
        moraleEvents    = EventLoader.loadEvents("events_morale.json");
        moveEvents      = EventLoader.loadEvents("events_move.json");
        rareEvents      = EventLoader.loadEvents("events_rare.json");
        endings         = EventLoader.loadEndings();

        questEvents = new ArrayList<>(EventLoader.loadEvents("events_quests.json"));
        questEvents.addAll(EventLoader.loadEvents("events_stages_quests.json"));
        questEvents.addAll(EventLoader.loadEvents("events_choice_quests.json"));

        questEventMap = new HashMap<>();
        for (GameEvent e : questEvents) {
            if (e.getQuestId() != null) {
                questEventMap.put(e.getQuestId() + "_" + e.getQuestStage(), e);
            }
        }
    }

    public List<GameEvent> getFoodEvents()      { return foodEvents; }
    public List<GameEvent> getHydrationEvents() { return hydrationEvents; }
    public List<GameEvent> getEnergyEvents()    { return energyEvents; }
    public List<GameEvent> getMoraleEvents()    { return moraleEvents; }
    public List<GameEvent> getMoveEvents()      { return moveEvents; }
    public List<GameEvent> getRareEvents()      { return rareEvents; }
    public List<GameEvent> getQuestEvents()     { return questEvents; }
    public Map<String, GameEvent> getQuestEventMap() { return questEventMap; }
    public Map<String, List<String>> getEndings()    { return endings; }
}
