package com.calvi.ui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


public class WeekView extends BorderPane {
    private LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    public WeekView(){
        setPrefHeight(280);
        refresh();
    }

    private void refresh(){
        setTop(buildNavBar());
        setCenter(buildDaysRow());
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

            VBox dayBox = new VBox();
            dayBox.setAlignment(Pos.TOP_CENTER);
            dayBox.setPadding(new Insets(6));
            dayBox.setMaxWidth(Double.MAX_VALUE);
            dayBox.setStyle(day.equals(today)
                    ? "-fx-border-color: lightgray; -fx-background-color: #f5eeef;"
                    : "-fx-border-color: lightgray;");

            Label weekLabel = new Label(prettyDayOfWeekName);
            weekLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            Label dateLabel = new Label(day.format(DateTimeFormatter.ofPattern("dd.MM")));

            dayBox.getChildren().addAll(weekLabel, dateLabel);
            daysRow.getChildren().add(dayBox);
            HBox.setHgrow(dayBox, Priority.ALWAYS);
        }

        return daysRow;
    }

}
