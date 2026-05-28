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
        setStyle("-fx-background-color: #111122; -fx-background-radius: 8;");
        setPadding(new Insets(14));
        setSpacing(14);

        Label title = new Label("⚙  Ekwipunek");
        title.setStyle("-fx-text-fill: #f0c040; -fx-font-size: 15px;");
        title.setFont(Font.font("Georgia", 15));

        getChildren().addAll(title, itemList);
        refresh();
    }

    // ── Odświeżanie ───────────────────────────────────────────────────────────

    public void refresh() {
        buildItemList();
        // BUG FIX: buildStatusList() usunięte — patrz komentarz przy klasie
    }

    // ── Budowanie sekcji itemów ───────────────────────────────────────────────

    private void buildItemList() {
        itemList.getChildren().clear();

        Inventory inv = engine.getInventory();
        Map<ItemType, Integer> all = inv.getAllItems();

        if (all.isEmpty()) {
            Label empty = new Label("Brak przedmiotów.");
            empty.setStyle("-fx-text-fill: #555; -fx-font-style: italic; -fx-font-size: 13px;");
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
        Label name = new Label(type.getEmoji() + "  " + type.getLabel());
        name.setStyle("-fx-text-fill: #ddd; -fx-font-size: 13px;");
        name.setWrapText(false);
        name.setTextOverrun(OverrunStyle.ELLIPSIS);

        Label effect = new Label(type.buildEffectDescription());
        effect.setStyle("-fx-text-fill: #7ec8a0; -fx-font-size: 11px;");
        effect.setWrapText(true);

        VBox textBlock = new VBox(2, name, effect);
        textBlock.setMaxWidth(120);
        textBlock.setMinWidth(120);

        Label stack = new Label("×" + count + "/" + type.getMaxStack());
        stack.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        stack.setMinWidth(35);
        stack.setAlignment(Pos.CENTER_RIGHT);

        Button use = new Button("Użyj");
        use.setStyle("""
            -fx-background-color: #1e3a1e; -fx-text-fill: #7ec8a0;
            -fx-font-size: 12px; -fx-padding: 4 10;
            -fx-background-radius: 4; -fx-cursor: hand;
        """);
        use.setOnMouseEntered(e -> use.setStyle(use.getStyle().replace("#1e3a1e", "#2a5a2a")));
        use.setOnMouseExited(e  -> use.setStyle(use.getStyle().replace("#2a5a2a", "#1e3a1e")));
        use.setOnAction(e -> {
            engine.useItem(type);
            onUseCallback.run();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(6, textBlock, spacer, stack, use);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-background-color: #16213e; -fx-background-radius: 6;");
        return row;
    }
}