package com.calvi.ui;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.calvi.model.Note;
import com.calvi.model.NotePriority;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


public class NotesView extends VBox {
    private List<Note> notes;
    private GridPane notesList;
    private VBox formBox;
    private TextField titleField;
    private TextArea contentArea;
    private ComboBox<NotePriority> priorityBox;
    private TextField searchField;
    private Runnable onDataChanged;

    public NotesView(List<Note> notes) {
        this.notes = notes;

        setAlignment(Pos.TOP_LEFT);
        setSpacing(6);
        setPadding(new Insets(12));
        setPrefWidth(250);
        setStyle("-fx-background-color: white; -fx-border-color: #cccccc;");

        Button toggleAddButton = new Button("+ Dodaj notatkę");
        toggleAddButton.setOnAction(event -> {
            formBox.setVisible(true);
            formBox.setManaged(true);
        });

        searchField = new TextField();
        searchField.setPromptText("Szukaj...");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshList());

        HBox topBar = new HBox(6);
        topBar.getChildren().addAll(toggleAddButton, searchField);

        titleField = new TextField();
        titleField.setPromptText("Tytuł");

        contentArea = new TextArea();
        contentArea.setPromptText("Treść notatki");
        contentArea.setPrefRowCount(2);

        priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll(NotePriority.values());
        priorityBox.setPromptText("Priorytet (opcjonalnie)");

        Button addButton = new Button("Dodaj notatkę");
        Button cancelButton = new Button("Anuluj");

        HBox buttonsBox = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonsBox.getChildren().addAll(addButton, spacer, cancelButton);

        formBox = new VBox(6);
        formBox.getChildren().addAll(titleField, contentArea, priorityBox, buttonsBox);
        formBox.setVisible(false);
        formBox.setManaged(false);

        addButton.setOnAction(event -> {
            if (!titleField.getText().isBlank()){
                Note newNote = new Note(titleField.getText(), contentArea.getText());
                newNote.setPriority(priorityBox.getValue());
                notes.add(newNote);
                closeForm();
                refreshList();
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            }
        });

        cancelButton.setOnAction(event -> closeForm());

        notesList = new GridPane();
        addEqualColumns(notesList, 2);
        notesList.setHgap(8);
        notesList.setVgap(8);

        getChildren().addAll(topBar, formBox, notesList);

        refreshList();
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    private void closeForm(){
        titleField.clear();
        contentArea.clear();
        priorityBox.setValue(null);
        formBox.setVisible(false);
        formBox.setManaged(false);
    }

    private void refreshList(){
        notesList.getChildren().clear();

        String query = searchField.getText().trim().toLowerCase();

        List<Note> matchingNotes = new ArrayList<>();
        for (Note note : notes){ //dla każdego Note w notes sprawdź, czy pasuje do wyszukiwania
            String dateText = note.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
            boolean matches = query.isEmpty()
                    || note.getTitle().toLowerCase().contains(query)
                    || note.getContent().toLowerCase().contains(query)
                    || dateText.toLowerCase().contains(query);

            if (matches){
                matchingNotes.add(note);
            }
        }

        matchingNotes.sort((a, b) -> {
            int priorityA = a.getPriority() == null ? -1 : a.getPriority().ordinal();
            int priorityB = b.getPriority() == null ? -1 : b.getPriority().ordinal();
            return priorityB - priorityA; // malejąco: wyższy priorytet trafia wyżej na listę
        });

        for (int i = 0; i < matchingNotes.size(); i++){ //dla każdego Note na pozycji i w matchingNotes zrób...
            Note note = matchingNotes.get(i);
            int col = i % 2;
            int row = i / 2;

            VBox noteBox = new VBox();
            noteBox.setSpacing(4);
            noteBox.setPadding(new Insets(8));
            noteBox.setStyle("-fx-border-color: lightgray; -fx-background-color: " + colorForPriority(note.getPriority()) + ";");

            Label titleLabel = new Label(note.getTitle());
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

            Label contentLabel = new Label(note.getContent());
            contentLabel.setFont(Font.font("Arial", 11));
            contentLabel.setWrapText(true);

            Label dateLabel = new Label(note.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
            dateLabel.setFont(Font.font("Arial", 10));
            dateLabel.setStyle("-fx-text-fill: #888888;");

            noteBox.getChildren().addAll(titleLabel, contentLabel, dateLabel);
            notesList.add(noteBox, col, row);
        }
    }

    private String colorForPriority(NotePriority priority){
        if (priority == null) {
            return "white";
        }

        switch (priority) {
            case HIGH:
                return "#fde2e2";
            case MEDIUM:
                return "#fff3cd";
            case LOW:
                return "#e2f0e2";
            default:
                return "white";
        }
    }

    private void addEqualColumns(GridPane grid, int columnCount){
        for (int i = 0; i < columnCount; i++){
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / columnCount);
            grid.getColumnConstraints().add(column);
        }
    }
}
