package com.calvi.ui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import com.calvi.model.EntryType;
import com.calvi.model.Task;
import com.calvi.model.TaskColor;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


public class WeekView extends BorderPane {
    private LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    private Consumer<LocalDate> onDaySelected;
    private List<Task> tasks;

    public WeekView(List<Task> tasks){
        this.tasks = tasks;
        setPrefHeight(280);
        refresh();
    }

    public void refresh(){
        setTop(buildNavBar());
        setCenter(buildDaysRow());
    }

    public void setOnDaySelected(Consumer<LocalDate> onDaySelected) {
        this.onDaySelected = onDaySelected;
    }

    private BorderPane buildNavBar(){
        BorderPane navBar = new BorderPane();

        Button prevButton = new Button("<");
        Button nextButton = new Button(">");
        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM");
        Button actualWeek = new Button(weekStart.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        navBar.setLeft(prevButton);
        navBar.setRight(nextButton);
        navBar.setCenter(actualWeek);

        BorderPane.setAlignment(actualWeek, Pos.CENTER);

        prevButton.setOnAction(event-> {
            weekStart = weekStart.minusWeeks(1);
            refresh();
        });

        nextButton.setOnAction(event -> {
            weekStart = weekStart.plusWeeks(1);
            refresh();
        });

        actualWeek.setOnAction(event->{
            weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            refresh();
        });

        return navBar;
    }

    private HBox buildDaysRow(){
        HBox daysRow = new HBox();
        LocalDate today = LocalDate.now();

        for (int i = 0; i <7; i++){
            LocalDate day = weekStart.plusDays(i);

            String dayOfWeekName = day.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("pl"));
            String prettyDayOfWeekName = dayOfWeekName.substring(0, 1).toUpperCase() + dayOfWeekName.substring(1);

            VBox dayBox = new VBox(4);
            dayBox.setAlignment(Pos.TOP_CENTER);
            dayBox.setPadding(new Insets(6));
            dayBox.setMaxWidth(Double.MAX_VALUE);
            dayBox.setStyle(day.equals(today)
                    ? "-fx-border-color: lightgray; -fx-background-color: #f5eeef;"
                    : "-fx-border-color: lightgray;");

            LocalDate cellDate = weekStart.plusDays(i);
            dayBox.setOnMouseClicked(event->{
                    if(onDaySelected != null){
                        onDaySelected.accept(cellDate);
                    }
                }
            );        

            Label weekLabel = new Label(prettyDayOfWeekName);
            weekLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            Label dateLabel = new Label(day.format(DateTimeFormatter.ofPattern("dd.MM")));

            dayBox.getChildren().addAll(weekLabel, dateLabel);

            for (Task task : tasks) { //dla każdego Task sprawdź, czy dotyczy tego konkretnego dnia
                if (task.appliesToDate(day)) {
                    String prefix = task.getType() == EntryType.EVENT ? "★ " : "";

                    // brak deadline'u u wydarzeń (EVENT) - isDeadlineClose() ma sens tylko dla TASK
                    boolean isDeadlineClose = task.getType() == EntryType.TASK
                            && task.getDeadline() != null
                            && task.isDeadlineClose();

                    // Label + setWrapText zamiast Text + wrappingWidth - Label sam zawija tekst do
                    // szerokości, jaką dostanie w layoucie, bez ręcznego liczenia/bindowania szerokości
                    Label chipTitle = new Label(prefix + task.getTitle());
                    chipTitle.setFont(Font.font("Arial", 10));
                    chipTitle.setWrapText(true);
                    chipTitle.setMaxWidth(Double.MAX_VALUE);
                    if (task.isDone()) {
                        chipTitle.setOpacity(0.5);
                    } else if (isDeadlineClose) {
                        chipTitle.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }

                    VBox chipContent = new VBox(2, chipTitle);

                    if (task.getType() == EntryType.TASK && task.getDeadline() != null) {
                        String deadlineStr = task.getDeadline().format(DateTimeFormatter.ofPattern("dd.MM"));

                        Label deadlineLabel = new Label(isDeadlineClose ? "❗ termin: " + deadlineStr : "termin: " + deadlineStr);
                        deadlineLabel.setFont(Font.font("Arial", 9));
                        deadlineLabel.setWrapText(true);
                        deadlineLabel.setMaxWidth(Double.MAX_VALUE);
                        deadlineLabel.setStyle(isDeadlineClose
                                ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
                                : "-fx-text-fill: #888888;");
                        chipContent.getChildren().add(deadlineLabel);
                    }

                    StackPane chip = new StackPane(chipContent);
                    chip.setMaxWidth(Double.MAX_VALUE);
                    chip.setStyle(
                            "-fx-background-color: " + colorForTaskColor(task.getColor()) + ";" +
                            "-fx-background-radius: 3px;" +
                            "-fx-padding: 1 3 1 3;"
                    );
                    dayBox.getChildren().add(chip);
                }
            }

            daysRow.getChildren().add(dayBox);
            HBox.setHgrow(dayBox, Priority.ALWAYS);
        }

        return daysRow;
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
