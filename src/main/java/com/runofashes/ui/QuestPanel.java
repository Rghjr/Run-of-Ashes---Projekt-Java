package com.runofashes.ui;

import com.runofashes.engine.GameEngine;
import com.runofashes.model.GameEvent;
import com.runofashes.model.QuestState;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.Map;

public class QuestPanel extends VBox {

    private final GameEngine engine;

    public QuestPanel(GameEngine engine) {
        this.engine = engine;

        getStyleClass().add("quest-panel");
        setPadding(new Insets(14));
        setSpacing(14);

        refresh();
    }

    public void refresh() {
        getChildren().clear();

        Label title = new Label("📜 Aktywne Zadania");
        title.getStyleClass().add("quest-title");
        title.setFont(Font.font("Georgia", 15));

        Region sep = new Region();
        sep.setStyle("-fx-background-color: #2a2a3a;");
        sep.setPrefHeight(1);

        getChildren().addAll(title, sep);

        Map<String, QuestState> active = engine.getActiveQuests();
        if (active.isEmpty()) {
            Label empty = new Label("Brak aktywnych zadań.");
            empty.getStyleClass().add("quest-empty");
            getChildren().add(empty);
            return;
        }

        for (QuestState qs : active.values()) {
            GameEvent nextEvent = engine.getQuestEvent(qs.getQuestId(), qs.getNextStage());
            String qName = nextEvent != null ? nextEvent.getLabel() : "Zadanie w toku...";

            Label nameLbl = new Label(qName);
            nameLbl.getStyleClass().add("quest-item-name");
            nameLbl.setWrapText(true);

            Label statLbl = new Label();

            if (qs.isReady()) {
                statLbl.setText("✅ Kontynuacja dostępna w kartach!");
                statLbl.getStyleClass().add("quest-status-ready");
            } else {
                statLbl.setText("⏳ " + qs.getTurnsLeft() + " tur(y) do następnego etapu");
                if (qs.isAllowWait()) {
                    statLbl.getStyleClass().add("quest-status-wait");
                } else {
                    statLbl.getStyleClass().add("quest-status-locked");
                }

                VBox box = new VBox(4, nameLbl, statLbl);
                if (qs.isAllowWait() && !qs.isReady()) {
                    Label waitHint = new Label("💡 Możesz przeczekać turę bez straty questa");
                    waitHint.getStyleClass().add("quest-wait-hint");
                    box.getChildren().add(waitHint);
                }
                box.getStyleClass().add("quest-item-box");
                getChildren().add(box);
            }
        }
    }
}