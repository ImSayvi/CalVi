package com.calvi;

import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import com.calvi.data.AppData;
import com.calvi.data.DataStore;
import com.calvi.ui.DayView;
import com.calvi.ui.MonthView;
import com.calvi.ui.NotesView;
import com.calvi.ui.WeekView;


public class Main extends Application{
    public static void main(String[] args) {
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

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1200, 800);

        MonthView monthsPane = new MonthView();
        DayView dayPane = new DayView(appData.getTasks());
        NotesView notesPane = new NotesView(appData.getNotes());
        WeekView weekPane = new WeekView();

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
        });

        stage.setScene(scene);

        root.setLeft(notesPane);
        root.setRight(dayPane);
        root.setCenter(monthsPane);
        root.setBottom(weekPane);

        stage.setTitle("CalVi");
        stage.show();
    }
}