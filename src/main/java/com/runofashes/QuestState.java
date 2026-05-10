package com.runofashes;

public class QuestState {

    private final String questId;
    private final int nextStage;
    private int turnsLeft;

    public QuestState(String questId, int nextStage, int turnsLeft) {
        this.questId   = questId;
        this.nextStage = nextStage;
        this.turnsLeft = turnsLeft;
    }

    public String getQuestId()  { return questId; }
    public int getNextStage()   { return nextStage; }
    public int getTurnsLeft()   { return turnsLeft; }
    public boolean isReady()    { return turnsLeft <= 0; }
    public void tick()          { if (turnsLeft > 0) turnsLeft--; }
}