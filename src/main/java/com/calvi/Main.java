package com.calvi;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public class Main extends Application{
    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage stage){
        BorderPane root = new StackPane();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        Label label = new Label("test javaFX");
        label.setFont(new Font("Arial", 25));
        
        root.getChildren().add(label);


        stage.setTitle("test java FX");
        stage.show();
    }
}