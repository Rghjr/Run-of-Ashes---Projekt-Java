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

        setStyle("-fx-background-color: #111122; -fx-background-radius: 8;");
        setPadding(new Insets(14));
        setSpacing(14);
        setMinWidth(260);
        setMaxWidth(260);

        refresh();
    }

    public void refresh() {
        getChildren().clear();

        Label title = new Label("📜 Aktywne Zadania");
        title.setStyle("-fx-text-fill: #f0c040; -fx-font-size: 15px;");
        title.setFont(Font.font("Georgia", 15));

        Region sep = new Region();
        sep.setStyle("-fx-background-color: #2a2a3a;");
        sep.setPrefHeight(1);

        getChildren().addAll(title, sep);

        Map<String, QuestState> active = engine.getActiveQuests();
        if (active.isEmpty()) {
            Label empty = new Label("Brak aktywnych zadań.");
            empty.setStyle("-fx-text-fill: #555; -fx-font-style: italic; -fx-font-size: 13px;");
            getChildren().add(empty);
            return;
        }

        for (QuestState qs : active.values()) {
            GameEvent nextEvent = engine.getQuestEvent(qs.getQuestId(), qs.getNextStage());
            String qName = nextEvent != null ? nextEvent.getLabel() : "Zadanie w toku...";

            Label nameLbl = new Label(qName);
            nameLbl.setStyle("-fx-text-fill: #ddd; -fx-font-size: 13px;");
            nameLbl.setWrapText(true);

            String status;
            String color;
            if (qs.isReady()) {
                status = "✅ Kontynuacja dostępna w kartach!";
                color  = "#7ec8a0";
            } else {
                status = "⏳ " + qs.getTurnsLeft() + " tur(y) do następnego etapu";
                color  = qs.isAllowWait() ? "#f0c040" : "#888";
            }

            Label statLbl = new Label(status);
            statLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");

            VBox box = new VBox(4, nameLbl, statLbl);
            if (qs.isAllowWait() && !qs.isReady()) {
                Label waitHint = new Label("💡 Możesz przeczekać turę bez straty questa");
                waitHint.setStyle("-fx-text-fill: #f0c040; -fx-font-size: 11px; -fx-font-style: italic;");
                box.getChildren().add(waitHint);
            }
            box.setStyle("-fx-background-color: #16213e; -fx-background-radius: 6; -fx-padding: 8;");
            getChildren().add(box);
        }
    }
}