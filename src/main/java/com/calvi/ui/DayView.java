package com.calvi.ui;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.calvi.model.EntryType;
import com.calvi.model.Task;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


public class DayView extends VBox {
    private LocalDate shownDate;
    private List<Task> tasks;
    private Label dayNameLabel = new Label();
    private Label dateLabel = new Label();
    private VBox taskListBox = new VBox(4);
    private TextField taskTitleField;
    private ComboBox<EntryType> taskTypeBox;
    private Runnable onDataChanged;

    public DayView(List<Task> tasks) {
        this.tasks = tasks;

        setPrefWidth(250);
        setAlignment(Pos.TOP_CENTER);
        setSpacing(6);
        setPadding(new Insets(12));
        setStyle("-fx-background-color: white; -fx-border-color: #cccccc;");

        dayNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        dateLabel.setFont(Font.font("Arial", 13));
        dateLabel.setStyle("-fx-text-fill: #888888;");

        taskTitleField = new TextField();
        taskTitleField.setPromptText("Nowe zadanie");

        taskTypeBox = new ComboBox<>();
        taskTypeBox.getItems().addAll(EntryType.values());
        taskTypeBox.setValue(EntryType.TASK);

        Button addTaskButton = new Button("Dodaj zadanie");
        addTaskButton.setOnAction(event -> {
            if (!taskTitleField.getText().isBlank()) {
                Task newTask = new Task(taskTitleField.getText(), shownDate, null, "", null, taskTypeBox.getValue());
                tasks.add(newTask);
                taskTitleField.clear();
                taskTypeBox.setValue(EntryType.TASK);
                refreshTasks();
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            }
        });

        HBox addTaskBox = new HBox(6, taskTitleField, taskTypeBox, addTaskButton);

        getChildren().addAll(dayNameLabel, dateLabel, new Separator(), taskListBox, new Separator(), addTaskBox);
        showDate(LocalDate.now());
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    public void showDate(LocalDate date){
        shownDate = date;

        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("pl"));
        String prettyDayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);

        String monthName = date.getMonth().getDisplayName(TextStyle.FULL, new Locale("pl"));

        dayNameLabel.setText(prettyDayName);
        dateLabel.setText(date.getDayOfMonth() + " " + monthName + " " + date.getYear());

        refreshTasks();
    }

    private void refreshTasks(){
        taskListBox.getChildren().clear();

        List<Task> tasksForDay = new ArrayList<>();
        for (Task task : tasks) { //dla każdego Task w tasks sprawdź, czy należy do wyświetlanego dnia
            if (task.getDate().equals(shownDate)) {
                tasksForDay.add(task);
            }
        }

        if (tasksForDay.isEmpty()) {
            Label noTasksLabel = new Label("Brak zaplanowanych zadań");
            noTasksLabel.setStyle("-fx-text-fill: #aaaaaa;");
            taskListBox.getChildren().add(noTasksLabel);
            return;
        }

        for (Task task : tasksForDay) {
            String prefix = task.getType() == EntryType.EVENT ? "★ " : "• ";
            Label taskLabel = new Label(prefix + task.getTitle());
            taskListBox.getChildren().add(taskLabel);
        }
    }
}
