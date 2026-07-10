package com.calvi;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.layout.Priority;

public class Main extends Application{
    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage stage){
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1200, 800);

        stage.setScene(scene);

        Label monthsLabel  = new Label("Widok miesięcy");
        Label weekLabel = new Label("Widok tygodnia");
        Label notesLabel  = new Label("notatki");
        Label dayLabel  = new Label("widok dnia");
        monthsLabel.setFont(new Font("Arial", 25));
        weekLabel.setFont(new Font("Arial", 25));
        notesLabel.setFont(new Font("Arial", 25));
        dayLabel.setFont(new Font("Arial", 25));
        
        StackPane monthsPane = new StackPane();
        monthsPane.setStyle("-fx-background-color: blue;");
        monthsPane.getChildren().add(monthsLabel);

        StackPane weekPane = new StackPane();
        weekPane.setStyle("-fx-background-color: red;");
        weekPane.getChildren().add(weekLabel);

        StackPane notesPane = new StackPane(); 
        notesPane.setPrefWidth(250);
        notesPane.setStyle("-fx-background-color: pink;"); 
        notesPane.getChildren().add(notesLabel);

        StackPane dayPane = new StackPane();
        dayPane.setPrefWidth(250);
        dayPane.setStyle("-fx-background-color: yellow;"); 
        dayPane.getChildren().add(dayLabel);

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