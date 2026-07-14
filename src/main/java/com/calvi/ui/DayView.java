package com.calvi.ui;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.calvi.model.EntryType;
import com.calvi.model.Task;
import com.calvi.model.TaskColor;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


public class DayView extends VBox {
    private LocalDate shownDate;
    private List<Task> tasks;
    private Task editingTask;
    private Label dayNameLabel = new Label();
    private Label dateLabel = new Label();
    private VBox taskListBox = new VBox(4);
    private TextField taskTitleField;
    private ComboBox<EntryType> taskTypeBox;
    private ComboBox<TaskColor> taskColorBox;
    private DatePicker taskDatePicker;
    private Button addTaskButton;
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
        taskTitleField.setMaxWidth(Double.MAX_VALUE);

        taskTypeBox = new ComboBox<>();
        taskTypeBox.getItems().addAll(EntryType.values());
        taskTypeBox.setValue(EntryType.TASK);
        HBox.setHgrow(taskTypeBox, Priority.ALWAYS);
        taskTypeBox.setMaxWidth(Double.MAX_VALUE);

        taskColorBox = new ComboBox<>();
        taskColorBox.getItems().addAll(TaskColor.values());
        taskColorBox.setPromptText("Kolor");
        taskColorBox.setCellFactory(listView -> createColorSwatchCell());
        taskColorBox.setButtonCell(createColorSwatchCell());
        HBox.setHgrow(taskColorBox, Priority.ALWAYS);
        taskColorBox.setMaxWidth(Double.MAX_VALUE);

        taskDatePicker = new DatePicker();
        taskDatePicker.setPromptText("Termin");
        HBox.setHgrow(taskDatePicker, Priority.ALWAYS);
        taskDatePicker.setMaxWidth(Double.MAX_VALUE);

        // etykieta w polu daty zmienia się w zależności od typu (deadline dla zadania, koniec zakresu dla wydarzenia)
        taskTypeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            taskDatePicker.setPromptText(newValue == EntryType.EVENT ? "Data zakończenia" : "Termin");
        });

        addTaskButton = new Button("Dodaj");
        addTaskButton.setOnAction(event -> {
            if (!taskTitleField.getText().isBlank()) {
                if (editingTask != null) {
                    editingTask.setTitle(taskTitleField.getText());
                    editingTask.setType(taskTypeBox.getValue());
                    editingTask.setColor(taskColorBox.getValue());
                    applyDatePickerToTask(editingTask);
                } else {
                    Task newTask = new Task(taskTitleField.getText(), shownDate, null, "", null, taskTypeBox.getValue());
                    newTask.setColor(taskColorBox.getValue());
                    applyDatePickerToTask(newTask);
                    tasks.add(newTask);
                }
                closeTaskForm();
                refreshTasks();
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            }
        });

        Button cancelTaskButton = new Button("Anuluj");
        cancelTaskButton.setOnAction(event -> closeTaskForm());

        Region formButtonsSpacer = new Region();
        HBox.setHgrow(formButtonsSpacer, Priority.ALWAYS);
        HBox formButtonsRow = new HBox(12, addTaskButton, formButtonsSpacer, cancelTaskButton);

        HBox typeAndColorRow = new HBox(10, taskTypeBox, taskColorBox);

        VBox addTaskBox = new VBox(8, taskTitleField, typeAndColorRow, taskDatePicker, formButtonsRow);
        addTaskBox.setPadding(new Insets(8));
        addTaskBox.setStyle("-fx-background-color: #f7f7f7; -fx-border-color: #dddddd;");

        getChildren().addAll(dayNameLabel, dateLabel, new Separator(), taskListBox, new Separator(), addTaskBox);
        showDate(LocalDate.now());
    }

    private ListCell<TaskColor> createColorSwatchCell(){
        return new ListCell<TaskColor>() {
            @Override
            protected void updateItem(TaskColor item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Region swatch = new Region();
                    swatch.setPrefSize(16, 16);
                    swatch.setStyle(
                            "-fx-background-color: " + colorForTaskColor(item) + ";" +
                            "-fx-background-radius: 3px;" +
                            "-fx-border-color: #999999;" +
                            "-fx-border-radius: 3px;"
                    );
                    setGraphic(swatch);
                }
            }
        };
    }

    private void applyDatePickerToTask(Task task){
        if (task.getType() == EntryType.EVENT) {
            task.setEndDate(taskDatePicker.getValue());
            task.setDeadline(null);
        } else {
            task.setDeadline(taskDatePicker.getValue());
            task.setEndDate(null);
        }
    }

    private void closeTaskForm(){
        editingTask = null;
        taskTitleField.clear();
        taskTypeBox.setValue(EntryType.TASK);
        taskColorBox.setValue(null);
        taskDatePicker.setValue(null);
        addTaskButton.setText("Dodaj");
    }

    private void openTaskForEdit(Task task){
        editingTask = task;
        taskTitleField.setText(task.getTitle());
        taskTypeBox.setValue(task.getType());
        taskColorBox.setValue(task.getColor());
        taskDatePicker.setValue(task.getType() == EntryType.EVENT ? task.getEndDate() : task.getDeadline());
        addTaskButton.setText("Zapisz");
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
            if (appliesToDate(task, shownDate)) {
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
            HBox taskRow = new HBox(6);
            taskRow.setAlignment(Pos.CENTER_LEFT);
            taskRow.setPadding(new Insets(4));
            taskRow.setStyle("-fx-background-color: " + colorForTaskColor(task.getColor()) + ";");
            taskRow.setOnMouseClicked(event -> openTaskForEdit(task));

            if (task.getType() == EntryType.TASK) {
                CheckBox doneBox = new CheckBox();
                doneBox.setSelected(task.isDone());
                doneBox.setOnAction(event -> {
                    task.setDone(doneBox.isSelected());
                    refreshTasks();
                    if (onDataChanged != null) {
                        onDataChanged.run();
                    }
                });
                taskRow.getChildren().add(doneBox);
            }

            String prefix = task.getType() == EntryType.EVENT ? "★ " : "";
            Label taskLabel = new Label(prefix + task.getTitle());

            Region rowSpacer = new Region();
            HBox.setHgrow(rowSpacer, Priority.ALWAYS);

            Button deleteButton = new Button("✕");
            deleteButton.setTooltip(new Tooltip("Usuń"));
            deleteButton.setStyle(
                    "-fx-background-color: #e74c3c;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 50%;" +
                    "-fx-min-width: 18px; -fx-min-height: 18px;" +
                    "-fx-max-width: 18px; -fx-max-height: 18px;" +
                    "-fx-padding: 0;" +
                    "-fx-font-size: 10px;" +
                    "-fx-cursor: hand;"
            );
            deleteButton.setOnAction(event -> {
                tasks.remove(task);
                refreshTasks();
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            });

            taskRow.getChildren().addAll(taskLabel, rowSpacer, deleteButton);
            taskListBox.getChildren().add(taskRow);
        }
    }

    private boolean appliesToDate(Task task, LocalDate day){
        if (task.getType() == EntryType.EVENT && task.getEndDate() != null) {
            // wydarzenie z zakresem dat "rozciąga się" - widoczne na każdym dniu od date do endDate
            return !day.isBefore(task.getDate()) && !day.isAfter(task.getEndDate());
        }

        if (task.getType() == EntryType.TASK && !task.isDone()) {
            // każde niedokończone zadanie "goni" dzisiejszą datę; deadline (jeśli jest) je zatrzymuje na miejscu,
            // brak deadline'u = goni bez ograniczenia, aż do oznaczenia jako ukończone
            LocalDate today = LocalDate.now();
            LocalDate effectiveDate = today;
            if (effectiveDate.isBefore(task.getDate())) {
                effectiveDate = task.getDate();
            }
            if (task.getDeadline() != null && effectiveDate.isAfter(task.getDeadline())) {
                effectiveDate = task.getDeadline();
            }
            return day.equals(effectiveDate);
        }

        return task.getDate().equals(day);
    }

    private String colorForTaskColor(TaskColor color){
        if (color == null) {
            return "white";
        }

        switch (color) {
            case RED:
                return "#fde2e2";
            case ORANGE:
                return "#ffe4cc";
            case YELLOW:
                return "#fff3cd";
            case GREEN:
                return "#e2f0e2";
            case BLUE:
                return "#dce8fc";
            case PURPLE:
                return "#ecdcfc";
            default:
                return "white";
        }
    }
}
