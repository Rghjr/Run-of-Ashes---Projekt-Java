package com.runofashes.engine;

import com.runofashes.model.GameEvent;
import com.runofashes.model.QuestState;
import com.runofashes.utils.WaitEventFactory;

import java.util.*;

public class QuestTracker {

    private final Map<String, QuestState> activeQuests    = new LinkedHashMap<>();
    private final Set<String> completedQuests = new HashSet<>();
    private Map<String, GameEvent> questEventMap = Map.of();
    private List<GameEvent> questEvents = List.of();


    public void init(Map<String, GameEvent> questEventMap, List<GameEvent> questEvents) {
        this.questEventMap = questEventMap;
        this.questEvents   = questEvents;
    }

    public void reset() {
        activeQuests.clear();
        completedQuests.clear();
    }

    public void handleProgress(GameEvent event) {
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

    public void onQuestFail(GameEvent event) {
        if (event.getQuestId() != null && event.getTurnsUntilNext() == 0) {
            activeQuests.remove(event.getQuestId());
            completedQuests.add(event.getQuestId());
        }
    }

    /** Zwraca komunikat do dołączenia do lastMessage, lub null. */
    public String cancelLocalQuests(String currentQuestId) {
        boolean removedAny = false;
        Iterator<Map.Entry<String, QuestState>> it = activeQuests.entrySet().iterator();
        while (it.hasNext()) {
            QuestState qs = it.next().getValue();
            if (qs.isLocal() && !qs.getQuestId().equals(currentQuestId)) {
                it.remove();
                removedAny = true;
            }
        }
        if (!removedAny) return null;
        return "Opuściłeś lokację. Inne lokalne zadania zostały anulowane.";
    }

    public void tick() {
        activeQuests.values().forEach(QuestState::tick);
    }

    public List<GameEvent> getReadyContinuations() {
        List<GameEvent> visible = new ArrayList<>();
        for (QuestState qs : activeQuests.values()) {
            if (qs.isReady()) {
                GameEvent next = questEventMap.get(qs.getQuestId() + "_" + qs.getNextStage());
                if (next != null) visible.add(next);
            }
        }
        return visible;
    }

    public List<GameEvent> getAvailableNewQuests() {
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

    public GameEvent buildWaitCard() {
        for (QuestState qs : activeQuests.values()) {
            if (!qs.isReady() && qs.isAllowWait()) {
                return WaitEventFactory.create(qs.getTurnsLeft());
            }
        }
        return null;
    }

    public Map<String, QuestState> getActiveQuests() {
        return Collections.unmodifiableMap(activeQuests);
    }
    public GameEvent getQuestEvent(String questId, int stage) {
        return questEventMap.get(questId + "_" + stage);
    }
    public Set<String> getCompletedQuests() {
        return completedQuests;
    }

    public boolean hasActiveLocalQuests(String currentQuestId) {
        return activeQuests.values().stream()
                .anyMatch(qs -> qs.isLocal() && !qs.getQuestId().equals(currentQuestId));
    }
}
