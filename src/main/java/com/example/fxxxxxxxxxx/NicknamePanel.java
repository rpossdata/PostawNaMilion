package com.example.fxxxxxxxxxx;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class NicknamePanel {
    private VBox panel;
    private TextField nicknameField;
    private Button startButton;

    public NicknamePanel() {
        panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 20;");

        Label title = new Label("Podaj swój nick");
        title.setStyle("-fx-font-size: 24pt; -fx-font-weight: bold; -fx-text-fill: #00eaff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 8, 0, 0, 2);");

        nicknameField = new TextField();
        nicknameField.setPromptText("Nickname");
        nicknameField.setMaxWidth(200);
        nicknameField.setStyle("-fx-font-size: 16pt; -fx-background-color: #2a2a4a; -fx-text-fill: #ffffff; -fx-border-color: #00eaff; -fx-border-radius: 5;");

        startButton = new Button("Rozpocznij grę");
        startButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);");
        startButton.setOnMouseEntered(e -> startButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #bb86fc; -fx-text-fill: #121212; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 0, 2); -fx-cursor: hand;"));
        startButton.setOnMouseExited(e -> startButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);"));
        startButton.setOnAction(e -> {
            if (nicknameField.getText().trim().isEmpty()) {
                nicknameField.setStyle("-fx-font-size: 16pt; -fx-background-color: #2a2a4a; -fx-text-fill: #ffffff; -fx-border-color: #ff5555; -fx-border-radius: 5;");
            } else {
                nicknameField.setStyle("-fx-font-size: 16pt; -fx-background-color: #2a2a4a; -fx-text-fill: #ffffff; -fx-border-color: #00eaff; -fx-border-radius: 5;");
            }
        });

        panel.getChildren().addAll(title, nicknameField, startButton);
    }

    public VBox getPanel() {
        return panel;
    }

    public TextField getNicknameField() {
        return nicknameField;
    }

    public Button getStartButton() {
        return startButton;
    }
}