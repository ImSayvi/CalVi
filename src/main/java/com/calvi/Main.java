package com.calvi;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;

import com.calvi.model.Note;
import com.calvi.model.Task;
import com.calvi.ui.*;


public class Main extends Application{
    public static void main(String[] args) {
        Task test = new Task("Test title", null, null, "test desc", null, null);
        Note testNote = new Note("testowa notataka", "test 123");

        System.out.println(test);
        System.out.println(testNote);
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
        

        VBox center = new VBox();
        center.getChildren().addAll(monthsPane, weekPane);
        VBox.setVgrow(monthsPane, Priority.ALWAYS);
        VBox.setVgrow(weekPane, Priority.ALWAYS);

        root.setLeft(notesPane);
        root.setRight(dayPane);
        root.setCenter(center);

        stage.setTitle("CalVi");
        stage.show();
    }
}