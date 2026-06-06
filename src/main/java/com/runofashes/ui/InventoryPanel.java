package com.runofashes.ui;

import com.runofashes.engine.GameEngine;
import com.runofashes.engine.Inventory;
import com.runofashes.model.ItemType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.OverrunStyle;
import javafx.scene.text.Font;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Map;

public class InventoryPanel extends VBox {

    private final GameEngine engine;
    private final Runnable onUseCallback;
    private final VBox itemList = new VBox(8);

    private int chalkoClicks = 0;
    private boolean isEggActive = false;

    // ── Konstruktor ───────────────────────────────────────────────────────────

    public InventoryPanel(GameEngine engine, Runnable onUseCallback) {
        this.engine = engine;
        this.onUseCallback = onUseCallback;
        getStyleClass().add("inventory-panel");
        setPadding(new Insets(14));
        setSpacing(14);

        HBox titleBox = new HBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        FontIcon titleIcon = new FontIcon("fas-cog");
        titleIcon.setIconSize(16);
        titleIcon.setIconColor(javafx.scene.paint.Color.web("#f0c040"));

        Label title = new Label("Ekwipunek");
        title.getStyleClass().add("inventory-title");
        title.setFont(Font.font("Georgia", 15));

        getChildren().addAll(title, itemList);
        refresh();
    }

    // ── Odświeżanie ───────────────────────────────────────────────────────────

    public void refresh() {
        buildItemList();
    }

    // ── Budowanie sekcji itemów ───────────────────────────────────────────────

    private void buildItemList() {
        itemList.getChildren().clear();

        Inventory inv = engine.getInventory();
        Map<ItemType, Integer> all = inv.getAllItems();

        if (all.isEmpty()) {
            Label empty = new Label("Brak przedmiotów.");
            empty.getStyleClass().add("empty-label");
            itemList.getChildren().add(empty);
            return;
        }

        for (Map.Entry<ItemType, Integer> entry : all.entrySet()) {
            ItemType type  = entry.getKey();
            int      count = entry.getValue();
            itemList.getChildren().add(buildItemRow(type, count));
        }
    }

    private HBox buildItemRow(ItemType type, int count) {
        FontIcon itemIcon = new FontIcon(type.getEmoji());
        itemIcon.setIconSize(16);
        itemIcon.setIconColor(javafx.scene.paint.Color.web("#ddd"));

        Label name = new Label(type.getLabel());
        name.getStyleClass().add("item-name");
        name.setWrapText(false);
        name.setTextOverrun(OverrunStyle.ELLIPSIS);

        HBox nameBox = new HBox(6, itemIcon, name);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        Label effect = new Label(type.buildEffectDescription());
        effect.getStyleClass().add("item-effect");
        effect.setWrapText(true);

        VBox textBlock = new VBox(2, name, effect);
        HBox.setHgrow(textBlock, Priority.ALWAYS);

        textBlock.setOnMouseClicked(e -> {
            if (type == ItemType.WEIRD_BREAD) {
                chalkoClicks++;
                System.out.println("Kliknięcia w Chałko-konia: " + chalkoClicks);
                if (chalkoClicks >= 5) {
                    chalkoClicks = 0;
                    showBread();
                }
            }
        });

        Label stack = new Label("×" + count + "/" + type.getMaxStack());
        stack.getStyleClass().add("item-stack");
        stack.setMinWidth(35);
        stack.setAlignment(Pos.CENTER_RIGHT);

        Button use = new Button("Użyj");
        use.getStyleClass().add("use-button");
        use.setMinWidth(Region.USE_PREF_SIZE);
        use.setOnAction(e -> {
            engine.useItem(type);
            onUseCallback.run();
        });

        HBox row = new HBox(12, textBlock, stack, use);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("item-row");

        return row;
    }






















    private void showBread() {
        engine.getAchievementManager().unlockAchievement("szc_10");
        if (isEggActive) return;

        try {
            isEggActive = true;

            javafx.stage.Window window = this.getScene().getWindow();
            if (window == null) {
                isEggActive = false;
                return;
            }

            javafx.scene.image.Image img = com.runofashes.utils.FileLoader.loadUiImage("chalkokon.jpg");
            if (img == null) {
                img = com.runofashes.utils.FileLoader.loadUiImage("event_default.png");
            }

            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
            imgView.setPreserveRatio(true);
            imgView.setFitHeight(800);

            javafx.scene.layout.StackPane pane = new javafx.scene.layout.StackPane(imgView);
            pane.setStyle("-fx-background-color: transparent;");

            javafx.stage.Stage eggStage = new javafx.stage.Stage();
            javafx.scene.Scene eggScene = new javafx.scene.Scene(pane, javafx.scene.paint.Color.TRANSPARENT);

            eggStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            eggStage.initOwner(window);
            eggStage.setScene(eggScene);

            eggStage.show();
            eggStage.setX(window.getX() + (window.getWidth() - eggStage.getWidth()) / 2);
            eggStage.setY(window.getY() + (window.getHeight() - eggStage.getHeight()) / 2);

            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            delay.setOnFinished(e -> {
                eggStage.close();
                isEggActive = false;
            });
            delay.play();

        } catch (Exception e) {
            System.out.println("Błąd Easter Ega: " + e.getMessage());
            isEggActive = false;
        }
    }
}