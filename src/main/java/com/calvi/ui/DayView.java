package com.calvi.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class DayView extends StackPane {
    public DayView() {
        Label dayLabel  = new Label("widok dnia");
        dayLabel.setFont(new Font("Arial", 25));
        setPrefWidth(250);
        setStyle("-fx-background-color: yellow;"); 
        getChildren().add(dayLabel);
    }
}
