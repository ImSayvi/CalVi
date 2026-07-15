package com.calvi.ui;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


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
    private DatePicker eventStartDatePicker;
    private CheckBox eventHasTimeCheckBox;
    private Spinner<Integer> eventStartHourSpinner;
    private Spinner<Integer> eventStartMinuteSpinner;
    private Spinner<Integer> eventEndHourSpinner;
    private Spinner<Integer> eventEndMinuteSpinner;
    private VBox eventStartRow;
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

        eventStartDatePicker = new DatePicker();
        eventStartDatePicker.setPromptText("Data rozpoczęcia");
        HBox.setHgrow(eventStartDatePicker, Priority.ALWAYS);
        eventStartDatePicker.setMaxWidth(Double.MAX_VALUE);

        eventHasTimeCheckBox = new CheckBox("Podaj godzinę");

        eventStartHourSpinner = new Spinner<>(0, 23, 12);
        eventStartHourSpinner.setEditable(true);
        eventStartHourSpinner.setPrefWidth(70);

        eventStartMinuteSpinner = new Spinner<>(0, 59, 0, 5);
        eventStartMinuteSpinner.setEditable(true);
        eventStartMinuteSpinner.setPrefWidth(70);

        eventEndHourSpinner = new Spinner<>(0, 23, 13);
        eventEndHourSpinner.setEditable(true);
        eventEndHourSpinner.setPrefWidth(70);

        eventEndMinuteSpinner = new Spinner<>(0, 59, 0, 5);
        eventEndMinuteSpinner.setEditable(true);
        eventEndMinuteSpinner.setPrefWidth(70);

        // zmiana godziny rozpoczęcia sama podpowiada godzinę zakończenia (start + 1h)
        eventStartHourSpinner.valueProperty().addListener((observable, oldValue, newValue) -> updateEventEndTimeDefault());
        eventStartMinuteSpinner.valueProperty().addListener((observable, oldValue, newValue) -> updateEventEndTimeDefault());

        // godzina jest opcjonalna - dopóki checkbox niezaznaczony, spinnery są nieaktywne i nieużywane przy zapisie
        eventHasTimeCheckBox.setOnAction(event -> {
            boolean hasTime = eventHasTimeCheckBox.isSelected();
            eventStartHourSpinner.setDisable(!hasTime);
            eventStartMinuteSpinner.setDisable(!hasTime);
            eventEndHourSpinner.setDisable(!hasTime);
            eventEndMinuteSpinner.setDisable(!hasTime);
        });
        eventStartHourSpinner.setDisable(true);
        eventStartMinuteSpinner.setDisable(true);
        eventEndHourSpinner.setDisable(true);
        eventEndMinuteSpinner.setDisable(true);

        HBox eventStartTimeRow = new HBox(6, new Label("Od:"), eventStartHourSpinner, eventStartMinuteSpinner);
        eventStartTimeRow.setAlignment(Pos.CENTER_LEFT);

        HBox eventEndTimeRow = new HBox(6, new Label("Do:"), eventEndHourSpinner, eventEndMinuteSpinner);
        eventEndTimeRow.setAlignment(Pos.CENTER_LEFT);

        eventStartRow = new VBox(4, eventStartDatePicker, eventHasTimeCheckBox, eventStartTimeRow, eventEndTimeRow);
        eventStartRow.setVisible(false);
        eventStartRow.setManaged(false);

        // etykieta w polu daty zmienia się w zależności od typu (deadline dla zadania, koniec zakresu dla wydarzenia),
        // a pole daty/godziny rozpoczęcia pokazuje się tylko dla wydarzeń
        taskTypeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            boolean isEvent = newValue == EntryType.EVENT;
            taskDatePicker.setPromptText(isEvent ? "Data zakończenia" : "Termin");
            eventStartRow.setVisible(isEvent);
            eventStartRow.setManaged(isEvent);
        });

        addTaskButton = new Button("Dodaj");
        addTaskButton.setOnAction(event -> {
            if (!taskTitleField.getText().isBlank()) {
                boolean isEvent = taskTypeBox.getValue() == EntryType.EVENT;

                if (editingTask != null) {
                    editingTask.setTitle(taskTitleField.getText());
                    editingTask.setType(taskTypeBox.getValue());
                    editingTask.setColor(taskColorBox.getValue());
                    if (isEvent) {
                        // tylko wydarzenie pozwala zmienić datę/godzinę rozpoczęcia -
                        // dla zadania NIE ruszamy date/time, bo mogłoby to zepsuć "skaczący" termin
                        LocalDate startDate = eventStartDatePicker.getValue() != null
                                ? eventStartDatePicker.getValue() : editingTask.getDate();
                        editingTask.setDate(startDate);
                        editingTask.setTime(getSelectedStartTime());
                        editingTask.setEndTime(getSelectedEndTime());
                    }
                    applyDatePickerToTask(editingTask);
                } else {
                    LocalDate startDate = shownDate;
                    LocalTime startTime = null;
                    LocalTime endTimeValue = null;
                    if (isEvent) {
                        startDate = eventStartDatePicker.getValue() != null ? eventStartDatePicker.getValue() : shownDate;
                        startTime = getSelectedStartTime();
                        endTimeValue = getSelectedEndTime();
                    }
                    Task newTask = new Task(taskTitleField.getText(), startDate, startTime, "", null, taskTypeBox.getValue());
                    newTask.setEndTime(endTimeValue);
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

        VBox addTaskBox = new VBox(8, taskTitleField, typeAndColorRow, eventStartRow, taskDatePicker, formButtonsRow);
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

    private LocalTime getSelectedStartTime(){
        return eventHasTimeCheckBox.isSelected()
                ? LocalTime.of(eventStartHourSpinner.getValue(), eventStartMinuteSpinner.getValue())
                : null;
    }

    private LocalTime getSelectedEndTime(){
        return eventHasTimeCheckBox.isSelected()
                ? LocalTime.of(eventEndHourSpinner.getValue(), eventEndMinuteSpinner.getValue())
                : null;
    }

    private void updateEventEndTimeDefault(){
        LocalTime start = LocalTime.of(eventStartHourSpinner.getValue(), eventStartMinuteSpinner.getValue());
        LocalTime end = start.plusHours(1);
        eventEndHourSpinner.getValueFactory().setValue(end.getHour());
        eventEndMinuteSpinner.getValueFactory().setValue(end.getMinute());
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
        eventStartDatePicker.setValue(null);
        eventHasTimeCheckBox.setSelected(false);
        eventStartHourSpinner.setDisable(true);
        eventStartMinuteSpinner.setDisable(true);
        eventEndHourSpinner.setDisable(true);
        eventEndMinuteSpinner.setDisable(true);
        eventStartHourSpinner.getValueFactory().setValue(12);
        eventStartMinuteSpinner.getValueFactory().setValue(0);
        eventEndHourSpinner.getValueFactory().setValue(13);
        eventEndMinuteSpinner.getValueFactory().setValue(0);
        addTaskButton.setText("Dodaj");
    }

    private void openTaskForEdit(Task task){
        editingTask = task;
        taskTitleField.setText(task.getTitle());
        taskTypeBox.setValue(task.getType());
        taskColorBox.setValue(task.getColor());
        taskDatePicker.setValue(task.getType() == EntryType.EVENT ? task.getEndDate() : task.getDeadline());
        if (task.getType() == EntryType.EVENT) {
            eventStartDatePicker.setValue(task.getDate());

            boolean hasTime = task.getTime() != null;
            eventHasTimeCheckBox.setSelected(hasTime);
            eventStartHourSpinner.setDisable(!hasTime);
            eventStartMinuteSpinner.setDisable(!hasTime);
            eventEndHourSpinner.setDisable(!hasTime);
            eventEndMinuteSpinner.setDisable(!hasTime);

            LocalTime time = hasTime ? task.getTime() : LocalTime.of(12, 0);
            eventStartHourSpinner.getValueFactory().setValue(time.getHour());
            eventStartMinuteSpinner.getValueFactory().setValue(time.getMinute());

            LocalTime endTimeValue = task.getEndTime() != null ? task.getEndTime() : time.plusHours(1);
            eventEndHourSpinner.getValueFactory().setValue(endTimeValue.getHour());
            eventEndMinuteSpinner.getValueFactory().setValue(endTimeValue.getMinute());
        }
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
            if (task.appliesToDate(shownDate)) {
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
            String timeText = "";
            if (task.getTime() != null) {
                DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
                timeText = task.getTime().format(timeFormat);
                if (task.getEndTime() != null) {
                    timeText += "-" + task.getEndTime().format(timeFormat);
                }
                timeText += " ";
            }
            Text taskLabel = new Text(prefix + timeText + task.getTitle());
            if (task.isDone()) {
                taskLabel.setStrikethrough(true);
                taskLabel.setFill(Color.web("#999999"));
            }

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
