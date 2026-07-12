package com.calvi.ui;
import java.util.ArrayList;
import java.util.List;
import com.calvi.model.Note;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;


public class NotesView extends VBox {
    private List<Note> notes = new ArrayList<>();
    private VBox notesList;
    private TextField titleField;
    private TextArea contentArea;

    public NotesView() {
        setAlignment(Pos.TOP_LEFT);
        setSpacing(6);
        setPadding(new Insets(12));
        setPrefWidth(250);
        setStyle("-fx-background-color: white; -fx-border-color: #cccccc;");

        titleField = new TextField();
        titleField.setPromptText("Tytuł");

        contentArea = new TextArea();
        contentArea.setPromptText("Treść notatki");
        contentArea.setPrefRowCount(2);

        Button addButton = new Button("Dodaj notatkę");
        Button cancelButton = new Button("Anuluj");

        HBox buttonsBox = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonsBox.getChildren().addAll(addButton, spacer, cancelButton);

        notesList = new VBox();

        getChildren().addAll(titleField, contentArea, buttonsBox, notesList);

    }
}
