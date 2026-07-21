package com.calvi;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import com.calvi.data.AppData;
import com.calvi.data.DataStore;
import com.calvi.model.DailyTask;
import com.calvi.ui.DayView;
import com.calvi.ui.MonthView;
import com.calvi.ui.NotesView;
import com.calvi.ui.TopBar;
import com.calvi.ui.WeekView;


public class Main extends Application{
    public static void main(String[] args) {
        // domyślne locale JVM steruje też wbudowanymi kontrolkami JavaFX (np. wyskakującym
        // kalendarzem DatePickera) - nasze własne new Locale("pl") w kodzie na to nie wpływa,
        // trzeba nadpisać to ustawienie globalnie, zanim cokolwiek z JavaFX się zbuduje
        Locale.setDefault(new Locale("pl", "PL"));
        launch(args);
    }


    @Override
    public void start(Stage stage){
        AppData loadedData;
        try {
            loadedData = DataStore.load();
        } catch (IOException e) {
            loadedData = new AppData();
            e.printStackTrace();
        }
        final AppData appData = loadedData;

        // zadania dzienne mają się odhaczać na nowo każdego dnia - jeśli od ostatniego
        // uruchomienia minął dzień, zerujemy je tutaj, zamiast trzymać osobny wpis na każdy dzień w historii
        LocalDate today = LocalDate.now();
        if (!today.equals(appData.getLastDailyReset())) {
            for (DailyTask dailyTask : appData.getDailyTasks()) {
                dailyTask.setDone(false);
            }
            appData.setLastDailyReset(today);
        }

        BorderPane content = new BorderPane();

        MonthView monthsPane = new MonthView(appData.getTasks(), appData.getDailyTasks());
        DayView dayPane = new DayView(appData.getTasks(), appData.getDailyTasks());
        NotesView notesPane = new NotesView(appData.getNotes());
        WeekView weekPane = new WeekView(appData.getTasks());

        monthsPane.setOnDaySelected(date -> dayPane.showDate(date));
        weekPane.setOnDaySelected(date -> dayPane.showDate(date));

        notesPane.setOnDataChanged(() -> {
            try {
                DataStore.save(appData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        dayPane.setOnDataChanged(() -> {
            try {
                DataStore.save(appData);
            } catch (IOException e) {
                e.printStackTrace();
            }
            monthsPane.refresh();
            weekPane.refresh();
        });

        content.setLeft(notesPane);
        content.setRight(dayPane);
        content.setCenter(monthsPane);
        content.setBottom(weekPane);

        // niewidzialna (na razie) nakładka nad całą resztą aplikacji - blokada ją odsłania
        // i przechwytuje wtedy kliknięcia, więc nic pod spodem ich nie dostaje
        Pane lockOverlay = new Pane();
        lockOverlay.setMouseTransparent(true);
        lockOverlay.setOnMouseClicked(event -> event.consume());

        StackPane contentStack = new StackPane(content, lockOverlay);
        lockOverlay.prefWidthProperty().bind(contentStack.widthProperty());
        lockOverlay.prefHeightProperty().bind(contentStack.heightProperty());

        TopBar topBar = new TopBar(stage);
        topBar.setOnLockChanged(locked -> lockOverlay.setMouseTransparent(!locked));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(contentStack);

        Scene scene = new Scene(root, 1200, 800);

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.setTitle("CalVi");
        stage.show();
    }
}