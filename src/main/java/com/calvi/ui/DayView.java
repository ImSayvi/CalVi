package com.calvi.ui;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


public class DayView extends VBox {
    private LocalDate shownDate;
    private Label dayNameLabel = new Label();
    private Label dateLabel = new Label();
    private Label noTasksLabel = new Label("Brak zaplanowanych zadań");

    public DayView() {
        setPrefWidth(250);
        setAlignment(Pos.TOP_CENTER);
        setSpacing(6);
        setPadding(new Insets(12));
        setStyle("-fx-background-color: white; -fx-border-color: #cccccc;");

        dayNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        dateLabel.setFont(Font.font("Arial", 13));
        dateLabel.setStyle("-fx-text-fill: #888888;");
        noTasksLabel.setStyle("-fx-text-fill: #aaaaaa;");

        getChildren().addAll(dayNameLabel, dateLabel, new Separator(), noTasksLabel);
        showDate(LocalDate.now());
    }

    public void showDate(LocalDate date){
        shownDate = date;

        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("pl"));
        String prettyDayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);

        String monthName = date.getMonth().getDisplayName(TextStyle.FULL, new Locale("pl"));

        dayNameLabel.setText(prettyDayName);
        dateLabel.setText(date.getDayOfMonth() + " " + monthName + " " + date.getYear());
    }

}
