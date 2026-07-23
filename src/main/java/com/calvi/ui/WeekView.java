package com.calvi.ui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import com.calvi.model.EntryType;
import com.calvi.model.Task;
import com.calvi.model.TaskColor;
import com.calvi.model.WeatherLocation;
import com.calvi.weather.DailyWeather;
import com.calvi.weather.WeatherService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class WeekView extends BorderPane {
    private static final int MAX_FULL_CHIPS = 3;

    private LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    private Consumer<LocalDate> onDaySelected;
    private List<Task> tasks;
    private WeatherLocation weatherLocation;

    // Open-Meteo daje prognozę tylko na najbliższe dni (nie na dowolną przeszłość/przyszłość),
    // więc dla dni spoza zakresu po prostu nie będzie tu wpisu - patrz buildDaysRow()
    private Map<LocalDate, DailyWeather> forecastByDate = new HashMap<>();

    public WeekView(List<Task> tasks, WeatherLocation weatherLocation){
        this.tasks = tasks;
        this.weatherLocation = weatherLocation;
        // wyżej niż na początku (280) - pogoda i pełne, zawijane tytuły zadań zajmują teraz
        // więcej miejsca w każdej komórce dnia, więc przy starej wysokości mniej się mieściło
        setPrefHeight(340);
        refresh();
        refreshWeather();
    }

    public void refresh(){
        setTop(buildNavBar());
        setCenter(buildDaysRow());
    }

    public void setOnDaySelected(Consumer<LocalDate> onDaySelected) {
        this.onDaySelected = onDaySelected;
    }

    // wywoływane też z zewnątrz (Main/TopBar) po zmianie miasta w ustawieniach - pobiera
    // prognogę w tle (sieć nie może blokować wątku FX) i po powrocie odświeża widok na wątku FX
    public void refreshWeather(){
        double latitude = weatherLocation.getLatitude();
        double longitude = weatherLocation.getLongitude();

        new Thread(() -> {
            try {
                List<DailyWeather> forecast = WeatherService.fetchForecast(latitude, longitude);
                Map<LocalDate, DailyWeather> byDate = new HashMap<>();
                for (DailyWeather day : forecast) {
                    byDate.put(day.date(), day);
                }

                Platform.runLater(() -> {
                    forecastByDate = byDate;
                    refresh();
                });
            } catch (Exception e) {
                e.printStackTrace(); // brak neta/błąd API - po prostu zostajemy bez pogody, nie wywalamy apki
            }
        }).start();
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

            DailyWeather weather = forecastByDate.get(day);
            if (weather != null) {
                long tempMax = Math.round(weather.tempMax());
                long tempMin = Math.round(weather.tempMin());

                // "Segoe UI Emoji" (czcionka Windows) zamiast Arial dla samej ikonki - Arial nie ma
                // kompletu glifów emoji, więc któryś kod pogody potrafił pokazać się jako pusty kwadrat
                Label iconLabel = new Label(WeatherService.iconForCode(weather.weatherCode()));
                iconLabel.setFont(Font.font("Segoe UI Emoji", 16));
                Tooltip.install(iconLabel, new Tooltip(WeatherService.descriptionForCode(weather.weatherCode())));

                // strzałki zamiast gołego "22/11" - bez nich nie było widać, który to max, a który min
                Label tempLabel = new Label("↑" + tempMax + "° ↓" + tempMin + "°");
                tempLabel.setFont(Font.font("Arial", 11));

                HBox weatherRow = new HBox(4, iconLabel, tempLabel);
                weatherRow.setAlignment(Pos.CENTER);
                dayBox.getChildren().add(weatherRow);
            }

            List<Task> dayTasks = new ArrayList<>();
            for (Task task : tasks) { //dla każdego Task sprawdź, czy dotyczy tego konkretnego dnia
                if (task.appliesToDate(day)) {
                    dayTasks.add(task);
                }
            }

            // do 3 zadań - pełne chipy jak dotychczas; więcej - reszta ląduje jako kropki (patrz niżej),
            // bo to właśnie dni z >3 zadaniami rozdymywały komórkę i zabierały miejsce reszcie widoku
            int fullChipCount = Math.min(dayTasks.size(), MAX_FULL_CHIPS);

            for (int t = 0; t < fullChipCount; t++) {
                Task task = dayTasks.get(t);
                String prefix = task.getType() == EntryType.EVENT ? "★ " : "";

                // brak deadline'u u wydarzeń (EVENT) - isDeadlineClose() ma sens tylko dla TASK
                boolean isDeadlineClose = task.getType() == EntryType.TASK
                        && task.getDeadline() != null
                        && task.isDeadlineClose();

                // TextFlow zawija zawarty Text do szerokości, jaką dostanie w layoucie - podobnie jak
                // Label.setWrapText, ale Text (w przeciwieństwie do Label) ma pewne wsparcie na przekreślenie
                Text titleText = new Text(prefix + task.getTitle());
                titleText.setFont(Font.font("Arial", 10));
                if (task.isDone()) {
                    titleText.setStrikethrough(true);
                    titleText.setFill(Color.web("#999999"));
                } else if (isDeadlineClose) {
                    titleText.setFill(Color.web("#e74c3c"));
                    titleText.setStyle("-fx-font-weight: bold;");
                }

                TextFlow chipTitle = new TextFlow(titleText);
                chipTitle.setMaxWidth(Double.MAX_VALUE);

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

            if (dayTasks.size() > MAX_FULL_CHIPS) {
                HBox dotsRow = new HBox(3);
                dotsRow.setAlignment(Pos.CENTER);

                for (int t = MAX_FULL_CHIPS; t < dayTasks.size(); t++) {
                    Task task = dayTasks.get(t);
                    String prefix = task.getType() == EntryType.EVENT ? "★ " : "";
                    boolean isDeadlineClose = task.getType() == EntryType.TASK
                            && task.getDeadline() != null
                            && task.isDeadlineClose();

                    Circle dot = new Circle(4, Color.web(colorForTaskColor(task.getColor())));
                    dot.setStroke(isDeadlineClose ? Color.web("#e74c3c") : Color.web("#999999"));
                    dot.setStrokeWidth(isDeadlineClose ? 1.5 : 0.5);
                    if (task.isDone()) {
                        dot.setOpacity(0.4);
                    }
                    Tooltip.install(dot, new Tooltip(prefix + task.getTitle()));

                    dotsRow.getChildren().add(dot);
                }

                dayBox.getChildren().add(dotsRow);
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
