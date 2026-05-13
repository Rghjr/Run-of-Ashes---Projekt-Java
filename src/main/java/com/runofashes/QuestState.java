package com.runofashes;

public class QuestState {

    private final String questId;
    private final int nextStage;
    private int turnsLeft;
    private final boolean local;

    public QuestState(String questId, int nextStage, int turnsLeft, boolean local) {
        this.questId   = questId;
        this.nextStage = nextStage;
        this.turnsLeft = turnsLeft;
        this.local = local;
    }

    public String getQuestId()  { return questId; }
    public int getNextStage()   { return nextStage; }
    public int getTurnsLeft()   { return turnsLeft; }
    public boolean isLocal()    { return local; }
    public boolean isReady()    { return turnsLeft <= 0; }
    public void tick()          { if (turnsLeft > 0) turnsLeft--; }
}