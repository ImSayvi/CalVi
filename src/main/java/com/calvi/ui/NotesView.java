package com.calvi.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class NotesView extends StackPane {

    public NotesView() {
        Label notesLabel  = new Label("notatki");
        notesLabel.setFont(new Font("Arial", 25));
        setPrefWidth(250);
        setStyle("-fx-background-color: pink;"); 
        getChildren().add(notesLabel);
    }
}
