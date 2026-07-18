package com.calvi.ui;

import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

// prosty suwak on/off - JavaFX nie ma gotowej kontrolki tego typu, więc to
// StackPane z "torem" (Region) i "gałką" (Circle), które przesuwamy w lewo/prawo
public class ToggleSwitch extends StackPane {
    private static final double WIDTH = 34;
    private static final double HEIGHT = 18;

    private boolean selected;
    private final Region track = new Region();
    private final Circle knob = new Circle(7, Color.WHITE);
    private Consumer<Boolean> onToggle;

    public ToggleSwitch(boolean initialSelected) {
        this.selected = initialSelected;

        setPrefSize(WIDTH, HEIGHT);
        setMinSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);
        setCursor(Cursor.HAND);

        track.setPrefSize(WIDTH, HEIGHT);
        StackPane.setAlignment(knob, Pos.CENTER_LEFT);
        knob.setTranslateX(2);

        getChildren().addAll(track, knob);
        applyVisual();

        setOnMouseClicked(event -> setSelected(!selected));
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        applyVisual();
        if (onToggle != null) {
            onToggle.accept(selected);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setOnToggle(Consumer<Boolean> onToggle) {
        this.onToggle = onToggle;
    }

    private void applyVisual() {
        track.setStyle(
                "-fx-background-radius: 9px;" +
                "-fx-background-color: " + (selected ? "#4caf82" : "#cccccc") + ";"
        );
        StackPane.setAlignment(knob, selected ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        knob.setTranslateX(selected ? -2 : 2);
    }
}
