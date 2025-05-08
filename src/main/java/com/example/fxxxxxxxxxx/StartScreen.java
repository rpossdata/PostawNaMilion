package com.example.fxxxxxxxxxx;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class StartScreen {
    private VBox panel;
    private Button leaderboardButton;
    private Button startGameButton;
    private GameController gameController;

    public StartScreen(GameController gameController) {
        this.gameController = gameController;
        initializePanel();
    }

    private void initializePanel() {
        panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 20;");

        // Tytuł gry
        Label title = new Label("Postaw na milion");
        title.setStyle("-fx-font-size: 48pt; -fx-font-weight: bold; -fx-text-fill: #00eaff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 10, 0, 0, 3);");

        // Zasady gry
        Text rules = new Text(
                "Zasady gry:\n\n" +
                        "1. Odpowiadaj na pytania, stawiając pieniądze na wybrane odpowiedzi.\n" +
                        "2. Minimalna stawka na pytanie to 50 000 zł.\n" +
                        "3. Jeśli postawisz na złą odpowiedź, tracisz wszystkie pieniądze.\n" +
                        "4. Możesz się poddać, zachowując niepostawione pieniądze.\n" +
                        "5. Użyj kół ratunkowych (50:50, Pytanie do publiczności, Telefon do przyjaciela), aby zwiększyć swoje szanse!\n" +
                        "6. Przejdź wszystkie 10 poziomów, aby wygrać!"
        );
        rules.setStyle("-fx-font-size: 16pt; -fx-fill: #ffffff;");
        rules.setTextAlignment(TextAlignment.CENTER);
        rules.setWrappingWidth(600);

        // Przyciski
        leaderboardButton = new Button("Tabela Wyników");
        leaderboardButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);");
        leaderboardButton.setOnMouseEntered(e -> leaderboardButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #bb86fc; -fx-text-fill: #121212; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 0, 2); -fx-cursor: hand;"));
        leaderboardButton.setOnMouseExited(e -> leaderboardButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);"));
        leaderboardButton.setOnAction(e -> showLeaderboard());

        startGameButton = new Button("Rozpocznij Grę");
        startGameButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);");
        startGameButton.setOnMouseEntered(e -> startGameButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #bb86fc; -fx-text-fill: #121212; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 0, 2); -fx-cursor: hand;"));
        startGameButton.setOnMouseExited(e -> startGameButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);"));
        startGameButton.setOnAction(e -> gameController.showNicknamePanel());

        panel.getChildren().addAll(title, rules, leaderboardButton, startGameButton);
    }

    private void showLeaderboard() {
        StackPane root = (StackPane) panel.getParent();
        if (root != null && gameController.getLeaderboard() != null) {
            root.getChildren().clear();
            VBox leaderboardPanel = gameController.getLeaderboard().createLeaderboardPanel();
            root.getChildren().add(leaderboardPanel);
        } else {
            System.err.println("Błąd: Nie można wyświetlić tabeli wyników - root lub leaderboard jest null.");
        }
    }

    public VBox getPanel() {
        return panel;
    }
}