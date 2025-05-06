package com.example.fxxxxxxxxxx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class PostawNaMilionFX extends Application {
    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        root.setPadding(new Insets(20));

        GameController gameController = new GameController();
        gameController.initializeUI(root);

        Scene scene = new Scene(root, 1000, 900);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Postaw na Milion");


        primaryStage.setMaximized(false);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(900);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);


        primaryStage.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            System.out.println("Szerokość okna: " + newWidth);
        });
        primaryStage.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            System.out.println("Wysokość okna: " + newHeight);
        });

        primaryStage.setOnCloseRequest(e -> Platform.exit());
        primaryStage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> obs, Boolean wasMaximized, Boolean isMaximized) {
                if (isMaximized) {
                    gameController.handleWindowMaximized();
                }
            }
        });

        primaryStage.show();


        System.out.println("Początkowe wymiary okna: " + primaryStage.getWidth() + "x" + primaryStage.getHeight());

        gameController.startGame();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
