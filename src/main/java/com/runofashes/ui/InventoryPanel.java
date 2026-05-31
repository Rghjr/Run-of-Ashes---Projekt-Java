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

    // BUG FIX: usunięto pole statusList i metodę buildStatusList() — były martwym kodem.
    // statusList był wypełniany w każdym refresh() ale nigdy nie był dodany do getChildren(),
    // więc użytkownik nigdy go nie widział. Statusy są wyświetlane przez activeStatusesBox
    // w Main.java. Wasted computation na każdy refresh ekwipunku.

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

        Label name = new Label(type.getEmoji() + "  " + type.getLabel());
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
}