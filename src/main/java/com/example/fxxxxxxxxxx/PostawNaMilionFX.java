package com.example.fxxxxxxxxxx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.File;

public class PostawNaMilionFX extends Application {
    @Override
    public void start(Stage primaryStage) {
        System.out.println("Working directory: " + new File(".").getAbsolutePath());
        StackPane root = new StackPane();
        root.setPadding(new Insets(20));

        GameController gameController = new GameController();
        gameController.initializeUI(root);

        Scene scene = new Scene(root, 1280, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Postaw na Milion");

        primaryStage.setMaximized(false);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        primaryStage.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            System.out.println("Szerokość okna: " + newWidth);
        });
        primaryStage.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            System.out.println("Wysokość okna: " + newHeight);
        });

        primaryStage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> obs, Boolean wasMaximized, Boolean isMaximized) {
                if (isMaximized) {
                    gameController.handleWindowMaximized();
                }
            }
        });

        primaryStage.setOnCloseRequest(e -> Platform.exit());

        primaryStage.show();

        System.out.println("Początkowe wymiary okna: " + primaryStage.getWidth() + "x" + primaryStage.getHeight());
    }

    public static void main(String[] args) {
        launch(args);
    }
}