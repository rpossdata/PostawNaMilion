package com.example.fxxxxxxxxxx;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class NicknamePanel {
    private VBox panel;
    private TextField nicknameField;
    private Button startButton;

    public NicknamePanel() {
        panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 20;");

        Label title = new Label("Podaj swój nick");
        title.setStyle("-fx-font-size: 24pt; -fx-font-weight: bold; -fx-text-fill: #00eaff;");

        nicknameField = new TextField();
        nicknameField.setPromptText("Nickname");
        nicknameField.setMaxWidth(200);
        nicknameField.setStyle("-fx-font-size: 16pt; -fx-background-color: #2a2a4a; -fx-text-fill: #ffffff; -fx-border-color: #00eaff; -fx-border-radius: 5;");

        startButton = new Button("Rozpocznij grę");
        startButton.setStyle("-fx-font-size: 16pt; -fx-padding: 10; -fx-background-color: #00eaff; -fx-text-fill: #1a1a2e; -fx-background-radius: 8; -fx-border-radius: 8;");
        startButton.setOnAction(e -> {
            if (nicknameField.getText().trim().isEmpty()) {
                nicknameField.setStyle("-fx-border-color: #ff5555; -fx-background-color: #2a2a4a; -fx-text-fill: #ffffff;");
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