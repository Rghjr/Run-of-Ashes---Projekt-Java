package com.runofashes.ui;

import com.runofashes.engine.GameEngine;
import com.runofashes.model.GameEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public final class EventCardView {

    private EventCardView() {}

    public static void fill(VBox card, GameEvent event, GameEngine engine, Consumer<GameEvent> onClick) {
        card.getChildren().clear();

        card.getStyleClass().removeAll("event-card-wait", "event-card-main-quest", "event-card-quest", "event-card-rare", "event-card-normal", "event-card-empty");
        card.getStyleClass().add("event-card");
        card.setOnMouseEntered(null);
        card.setOnMouseExited(null);

        boolean isWait  = "WAIT_TURN".equals(event.getId());
        boolean rare    = event.isHiddenEffects();
        boolean isQuest = event.getQuestId() != null;

        boolean isMainQuest = isQuest && (event.getRequiredStage() != null
                || event.getQuestId().startsWith("am_")
                || event.getQuestId().startsWith("gory_")
                || event.getQuestId().startsWith("eu_"));

        String badgeText = "";
        String badgeClass = "";

        if (isWait) {
            card.getStyleClass().add("event-card-wait");
            badgeText = "⏳ przeczekanie"; badgeClass = "badge-wait";
        } else if (isMainQuest) {
            card.getStyleClass().add("event-card-main-quest");
            badgeText = event.getQuestStage() > 1 ? "🚩 kontynuacja wątku" : "🚩 główny wątek";
            badgeClass = "badge-main-quest";
        } else if (isQuest) {
            card.getStyleClass().add("event-card-quest");
            badgeText = event.getQuestStage() > 1 ? "📜 kontynuacja poboczna" : "📜 zadanie poboczne";
            badgeClass = "badge-quest";
        } else if (rare) {
            card.getStyleClass().add("event-card-rare");
            badgeText = "✨ niezwykłe spotkanie"; badgeClass = "badge-rare";
        } else {
            card.getStyleClass().add("event-card-normal");
        }

        String fg = isWait ? "#f0c040" : rare ? "#ffaa44" : "#eee";

        boolean isDepressed = engine.getPlayer().getMorale() < 30;
        String displayedLabel = (isDepressed && event.getLowMoraleLabel() != null)
                ? event.getLowMoraleLabel()
                : event.getLabel();

        Label lbl = new Label(displayedLabel);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-text-fill: " + fg + "; -fx-font-size: 14px;");
        lbl.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(lbl, Priority.ALWAYS);

        String fxStr = event.buildEffectsString();
        Label fxLabel = new Label(fxStr.isEmpty() ? "" : fxStr);
        fxLabel.setWrapText(true);
        fxLabel.setStyle("-fx-text-fill: " + (isWait ? "#f0c040" : "#7ec8a0") + "; -fx-font-size: 13px;");

        String distText = event.getDistanceCost() > 0 ? "   📍 -" + event.getDistanceCost() + " km" : "";
        Label metaLbl = new Label("⏱ " + event.getTimeCost() + "h" + distText);
        metaLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        card.getChildren().addAll(lbl, fxLabel, metaLbl);

        if (!badgeText.isEmpty()) {
            Label badge = new Label(badgeText);
            badge.getStyleClass().addAll("badge-label", badgeClass);
            card.getChildren().add(badge);
        }

        if (event.getDistanceCost() > 0 && engine.hasActiveLocalQuests(event.getQuestId())) {
            Label warnLbl = new Label("Ruch anuluje lokalne zadania!");
            warnLbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
            card.getChildren().add(warnLbl);
        }

        card.setOnMouseClicked(e -> onClick.accept(event));
    }

    public static void fillEmpty(VBox card) {
        card.getChildren().clear();
        card.getStyleClass().setAll("event-card-empty");
        card.setOnMouseClicked(null);
        card.setOnMouseEntered(null);
        card.setOnMouseExited(null);
    }
}