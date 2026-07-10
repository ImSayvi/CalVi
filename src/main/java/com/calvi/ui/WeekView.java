package com.calvi.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class WeekView extends StackPane {
    public WeekView(){
        Label weekLabel = new Label("Widok tygodnia");
        weekLabel.setFont(new Font("Arial", 25));
        setStyle("-fx-background-color: red;");
        getChildren().add(weekLabel);
    }
}
