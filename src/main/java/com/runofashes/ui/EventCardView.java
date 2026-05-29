package com.runofashes.ui;

import com.runofashes.engine.GameEngine;
import com.runofashes.model.GameEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public final class EventCardView {

    private EventCardView() {}

    public static void fill(VBox card, GameEvent event, GameEngine engine, Consumer<GameEvent> onClick) {
        card.getChildren().clear();

        boolean isWait  = "WAIT_TURN".equals(event.getId());
        boolean rare    = event.isHiddenEffects();
        boolean isQuest = event.getQuestId() != null;

        boolean isMainQuest = isQuest && (event.getRequiredStage() != null
                || event.getQuestId().startsWith("am_")
                || event.getQuestId().startsWith("gory_")
                || event.getQuestId().startsWith("eu_"));

        String bg, bgHo, badgeText, badgeColor;

        if (isWait) {
            bg = "#1a1a0e"; bgHo = "#2a2a18";
            badgeText = "⏳ przeczekanie"; badgeColor = "#f0c040";
        } else if (isMainQuest) {
            bg = "#2a1515"; bgHo = "#3d1e1e";
            badgeText = event.getQuestStage() > 1 ? "🚩 kontynuacja wątku" : "🚩 główny wątek";
            badgeColor = "#e74c3c";
        } else if (isQuest) {
            bg = "#1a2e1a"; bgHo = "#1f3d1f";
            badgeText = event.getQuestStage() > 1 ? "📜 kontynuacja poboczna" : "📜 zadanie poboczne";
            badgeColor = "#8bc48b";
        } else if (rare) {
            bg = "#2c1a0e"; bgHo = "#4a2a10";
            badgeText = "✨ niezwykłe spotkanie"; badgeColor = "#ffaa44";
        } else {
            bg = "#16213e"; bgHo = "#1a2a50";
            badgeText = ""; badgeColor = "";
        }

        String fg = isWait ? "#f0c040" : rare ? "#ffaa44" : "#eee";

        boolean isDepressed = engine.getPlayer().getMorale() < 30;
        String displayedLabel = (isDepressed && event.getLowMoraleLabel() != null)
                ? event.getLowMoraleLabel()
                : event.getLabel();

        card.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8; -fx-cursor: hand;");

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
        metaLbl.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        card.getChildren().addAll(lbl, fxLabel, metaLbl);

        if (!badgeText.isEmpty()) {
            Label badge = new Label(badgeText);
            badge.setStyle("-fx-text-fill: " + badgeColor + "; -fx-font-size: 12px;");
            card.getChildren().add(badge);
        }

        if (event.getDistanceCost() > 0 && engine.hasActiveLocalQuests(event.getQuestId())) {
            Label warnLbl = new Label("Ruch anuluje lokalne zadania!");
            warnLbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
            card.getChildren().add(warnLbl);
        }

        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace(bg, bgHo)));
        card.setOnMouseExited(e  -> card.setStyle(card.getStyle().replace(bgHo, bg)));
        card.setOnMouseClicked(e -> onClick.accept(event));
    }

    public static void fillEmpty(VBox card) {
        card.getChildren().clear();
        card.setOnMouseClicked(null);
        card.setOnMouseEntered(null);
        card.setOnMouseExited(null);
        card.setStyle("-fx-background-color: #0f0f1a; -fx-background-radius: 8;");
    }
}
