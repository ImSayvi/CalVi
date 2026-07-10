package com.calvi.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class MonthView extends StackPane {
    public MonthView(){
        Label monthsLabel  = new Label("Widok miesięcy");
        monthsLabel.setFont(new Font("Arial", 25));
        setStyle("-fx-background-color: blue;");
        getChildren().add(monthsLabel);
    }
}
