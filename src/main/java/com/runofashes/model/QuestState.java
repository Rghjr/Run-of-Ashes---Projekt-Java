package com.runofashes.model;

public class QuestState {

    private String questId;
    private int nextStage;
    private int turnsLeft;
    private boolean local;
    private boolean allowWait;

    public QuestState(){
    }

    public QuestState(String questId, int nextStage, int turnsLeft, boolean local) {
        this(questId, nextStage, turnsLeft, local, false);
    }

    public QuestState(String questId, int nextStage, int turnsLeft, boolean local, boolean allowWait) {
        this.questId    = questId;
        this.nextStage  = nextStage;
        this.turnsLeft  = turnsLeft;
        this.local      = local;
        this.allowWait  = allowWait;
    }

    public String getQuestId()  { return questId; }
    public int getNextStage()   { return nextStage; }
    public int getTurnsLeft()   { return turnsLeft; }
    public boolean isLocal()    { return local; }
    public boolean isReady()    { return turnsLeft <= 0; }
    public boolean isAllowWait(){ return allowWait; }
    public void tick()          { if (turnsLeft > 0) turnsLeft--; }
}