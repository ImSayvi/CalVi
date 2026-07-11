package com.calvi;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import com.calvi.ui.*;


public class Main extends Application{
    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage stage){
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1200, 800);

        MonthView monthsPane = new MonthView();
        DayView dayPane = new DayView();
        NotesView notesPane = new NotesView();
        WeekView weekPane = new WeekView();

        stage.setScene(scene);

        root.setLeft(notesPane);
        root.setRight(dayPane);
        root.setCenter(monthsPane);
        root.setBottom(weekPane);

        stage.setTitle("CalVi");
        stage.show();
    }
}