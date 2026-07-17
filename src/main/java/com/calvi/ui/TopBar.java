package com.calvi.ui;

import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class TopBar extends HBox {
    private double dragOffsetX;
    private double dragOffsetY;
    private boolean locked = false;
    private Consumer<Boolean> onLockChanged;

    public TopBar(Stage stage) {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(6);
        setStyle("-fx-background-color: #2c2c2c; -fx-padding: 8 10 8 12;");

        Label title = new Label("CalVi");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // pushnie resztę przycisków na prawą stronę paska
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button settingsButton = new Button("⚙"); // na razie bez akcji - ustawienia wyglądu dojdą później
        Button closeButton = new Button("✕");
        closeButton.setOnAction(event -> stage.close());

        Button lockButton = new Button("🔓");
        lockButton.setOnAction(event -> {
            locked = !locked;
            lockButton.setText(locked ? "🔒" : "🔓");
            // w zablokowanym stanie ma działać wyłącznie sama kłódka - reszta paska (w tym drag,
            // liczony niżej ręcznie na mysz) ma być nieaktywna
            settingsButton.setDisable(locked);
            closeButton.setDisable(locked);
            // całe okno robi się półprzezroczyste - widać przez nie tło, ale treść wciąż da się przeczytać
            stage.setOpacity(locked ? 0.75 : 1.0);
            if (onLockChanged != null) {
                onLockChanged.accept(locked);
            }
        });

        getChildren().addAll(title, spacer, lockButton, settingsButton, closeButton);

        // okno bez systemowej ramki nie da się już przeciągać za domyślny pasek tytułu,
        // więc sami liczymy przesunięcie myszy i przesuwamy nim Stage
        setOnMousePressed(event -> {
            if (locked) {
                return;
            }
            dragOffsetX = event.getSceneX();
            dragOffsetY = event.getSceneY();
        });
        setOnMouseDragged(event -> {
            if (locked) {
                return;
            }
            stage.setX(event.getScreenX() - dragOffsetX);
            stage.setY(event.getScreenY() - dragOffsetY);
        });
    }

    public void setOnLockChanged(Consumer<Boolean> onLockChanged) {
        this.onLockChanged = onLockChanged;
    }
}
