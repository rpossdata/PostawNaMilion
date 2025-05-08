package com.example.fxxxxxxxxxx;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Leaderboard {
    public static class Score {
        private String nickname;
        private int money;
        private int level;
        private long scoreValue;

        public Score(String nickname, int money, int level) {
            this.nickname = nickname != null ? nickname.trim() : "Unknown";
            this.money = money;
            this.level = level;

            double[] multipliers = {0.0, 0.19, 0.29, 0.39, 0.49, 0.59, 0.69, 0.79, 0.89, 0.99, 1.0};
            if (level < 0 || level >= multipliers.length) {
                this.scoreValue = money;
            } else if (level == 0) {
                this.scoreValue = money - 1000000;
            } else {
                this.scoreValue = (long) (money * multipliers[level]);
            }
        }

        public String getNickname() { return nickname; }
        public int getMoney() { return money; }
        public int getLevel() { return level; }
        public long getScoreValue() { return scoreValue; }
    }

    private static final File SCORE_DATA_FILE = new File("game_data/scores.txt");
    private final GameController gameController;

    public Leaderboard(GameController gameController) {
        this.gameController = gameController;
        System.out.println("Inicjalizacja Leaderboard.");
        ensureDataDirectoryExists();
    }

    private void ensureDataDirectoryExists() {
        System.out.println("Sprawdzanie/tworzenie katalogu na wyniki: " + SCORE_DATA_FILE.getParentFile().getAbsolutePath());
        try {
            File parentDir = SCORE_DATA_FILE.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    System.out.println("Utworzono katalog na dane: " + parentDir.getAbsolutePath());
                } else {
                    String errorMsg = "Nie udało się utworzyć katalogu na dane: " + parentDir.getAbsolutePath();
                    System.err.println(errorMsg);
                    if (gameController != null) gameController.showError(errorMsg);
                }
            }
        } catch (SecurityException e) {
            String errorMsg = "Brak uprawnień do utworzenia katalogu: " + SCORE_DATA_FILE.getParentFile().getAbsolutePath();
            System.err.println(errorMsg);
            if (gameController != null) gameController.showError(errorMsg);
        }
    }

    public void saveScore(String nickname, int money, int level) {
        System.out.println("Zapisywanie wyniku: pseudonim=" + nickname + ", pieniądze=" + money + ", poziom=" + level);
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = "Unknown";
        }
        ensureDataDirectoryExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SCORE_DATA_FILE, true))) {
            writer.write(nickname + "," + money + "," + level);
            writer.newLine();
            System.out.println("Wynik zapisany do: " + SCORE_DATA_FILE.getAbsolutePath());
        } catch (IOException e) {
            String errorMsg = "Błąd zapisu wyniku: " + e.getMessage();
            System.err.println(errorMsg);
            if (gameController != null) gameController.showError(errorMsg);
        }
    }

    public List<Score> loadScores() {
        System.out.println("Ładowanie wyników z: " + SCORE_DATA_FILE.getAbsolutePath());
        List<Score> scores = new ArrayList<>();
        ensureDataDirectoryExists();
        if (!SCORE_DATA_FILE.exists()) {
            System.out.println("Plik wyników nie istnieje, zwracam pustą listę.");
            return scores;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(SCORE_DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 3) {
                    try {
                        String nicknamePart = parts[0].trim();
                        int moneyPart = Integer.parseInt(parts[1].trim());
                        int levelPart = Integer.parseInt(parts[2].trim());
                        if (!nicknamePart.isEmpty()) {
                            scores.add(new Score(nicknamePart, moneyPart, levelPart));
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Nieprawidłowy format danych w linii: " + line);
                    }
                } else {
                    System.err.println("Nieprawidłowa linia w pliku wyników: " + line);
                }
            }
        } catch (IOException e) {
            String errorMsg = "Błąd odczytu wyników: " + e.getMessage();
            System.err.println(errorMsg);
            if (gameController != null) gameController.showError(errorMsg);
        }
        System.out.println("Załadowano " + scores.size() + " wyników.");
        return scores;
    }

    public VBox createLeaderboardPanel() {
        System.out.println("Tworzenie panelu rankingu.");
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 20;");

        // Dynamiczne skalowanie panelu do rozmiaru okna
        if (gameController != null && gameController.getRoot() != null) {
            panel.prefWidthProperty().bind(gameController.getRoot().widthProperty());
            panel.prefHeightProperty().bind(gameController.getRoot().heightProperty());
            panel.setPadding(new Insets(20, 20, 20, 20));
        } else {
            panel.setPrefWidth(1280);
            panel.setPrefHeight(720);
            System.err.println("GameController lub root jest null, używam domyślnych rozmiarów.");
        }

        Label title = new Label("Ranking Graczy");
        title.setStyle("-fx-font-size: 36pt; -fx-font-weight: bold; -fx-text-fill: #00eaff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 8, 0, 0, 2);");

        TableView<Score> table = new TableView<>();
        table.setStyle("-fx-background-color: #2a2a4a; -fx-border-color: #00eaff; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10;");

        // Dynamiczne skalowanie tabeli
        table.prefWidthProperty().bind(panel.widthProperty().multiply(0.95));
        table.prefHeightProperty().bind(panel.heightProperty().multiply(0.75));

        Label placeholder = new Label("Brak wyników do wyświetlenia.\nZagraj, aby pojawić się w rankingu!");
        placeholder.setStyle("-fx-font-size: 16pt; -fx-text-fill: #ffffff; -fx-text-alignment: center; -fx-padding: 20;");
        table.setPlaceholder(placeholder);

        table.setRowFactory(tv -> {
            TableRow<Score> row = new TableRow<>();
            String baseRowStyle = "-fx-font-size: 14pt; -fx-padding: 15 10; -fx-min-height: 60; -fx-alignment: CENTER_LEFT; -fx-border-width: 0 0 0.5 0; -fx-border-color: rgba(0, 234, 255, 0.2);";

            row.indexProperty().addListener((obs, oldIndex, newIndex) -> {
                if (row.isEmpty()) {
                    row.setStyle("-fx-background-color: transparent;");
                    return;
                }
                if (newIndex.intValue() % 2 == 0) {
                    row.setStyle("-fx-background-color: rgba(50, 50, 60, 0.78); -fx-text-fill: #e8e8e8; " + baseRowStyle);
                } else {
                    row.setStyle("-fx-background-color: rgba(40, 40, 50, 0.78); -fx-text-fill: #e8e8e8; " + baseRowStyle);
                }
            });
            return row;
        });

        String columnHeaderStyle = "-fx-alignment: CENTER; -fx-text-fill: #e0cffc; -fx-font-size: 16pt; -fx-font-weight: bold; -fx-background-color: rgba(25, 25, 35, 0.92); -fx-border-color: #00eaff; -fx-border-width: 0 0 1.5 0; -fx-padding: 12 5;";

        TableColumn<Score, Void> rankCol = new TableColumn<>("Miejsce");
        rankCol.setSortable(false);
        rankCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1)); // 10% szerokości tabeli
        rankCol.setMinWidth(100);
        rankCol.setMaxWidth(150);
        rankCol.setStyle(columnHeaderStyle);
        rankCol.setCellFactory(col -> new TableCell<Score, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.valueOf(getTableRow().getIndex() + 1));
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Score, String> nicknameCol = new TableColumn<>("Pseudonim");
        nicknameCol.setCellValueFactory(new PropertyValueFactory<>("nickname"));
        nicknameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.35)); // 35% szerokości tabeli
        nicknameCol.setMinWidth(300);
        nicknameCol.setStyle(columnHeaderStyle + "-fx-alignment: CENTER_LEFT;");
        nicknameCol.setCellFactory(tc -> new TableCell<Score, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 0, 0, 10));
            }
        });

        TableColumn<Score, Integer> moneyCol = new TableColumn<>("Pieniądze");
        moneyCol.setCellValueFactory(new PropertyValueFactory<>("money"));
        moneyCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2)); // 20% szerokości tabeli
        moneyCol.setMinWidth(200);
        moneyCol.setStyle(columnHeaderStyle + "-fx-alignment: CENTER_RIGHT;");
        moneyCol.setCellFactory(tc -> new TableCell<Score, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,d zł", item));
                setAlignment(Pos.CENTER_RIGHT);
                setPadding(new Insets(0, 10, 0, 0));
            }
        });

        TableColumn<Score, Integer> levelCol = new TableColumn<>("Ukończone Poziomy");
        levelCol.setCellValueFactory(new PropertyValueFactory<>("level"));
        levelCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2)); // 20% szerokości tabeli
        levelCol.setMinWidth(200);
        levelCol.setStyle(columnHeaderStyle);
        levelCol.setCellFactory(tc -> new TableCell<Score, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Score, Long> scoreCol = new TableColumn<>("Wynik");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("scoreValue"));
        scoreCol.prefWidthProperty().bind(table.widthProperty().multiply(0.15)); // 15% szerokości tabeli
        scoreCol.setMinWidth(150);
        scoreCol.setStyle(columnHeaderStyle + "-fx-alignment: CENTER_RIGHT;");
        scoreCol.setCellFactory(tc -> new TableCell<Score, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,d", item));
                setAlignment(Pos.CENTER_RIGHT);
                setPadding(new Insets(0, 10, 0, 0));
            }
        });

        table.getColumns().addAll(rankCol, nicknameCol, moneyCol, levelCol, scoreCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        List<Score> scores = loadScores();
        if (scores.isEmpty()) {
            System.out.println("Brak wyników do wyświetlenia w tabeli.");
        } else {
            System.out.println("Sortowanie i dodawanie " + scores.size() + " wyników do tabeli.");
            scores.sort((s1, s2) -> Long.compare(s2.getScoreValue(), s1.getScoreValue()));
            table.getItems().addAll(scores);
        }

        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        scrollPane.prefWidthProperty().bind(table.prefWidthProperty());
        scrollPane.prefHeightProperty().bind(table.prefHeightProperty());

        try {
            javafx.scene.Node verticalScrollBar = scrollPane.lookup(".scroll-bar:vertical");
            if (verticalScrollBar != null) {
                verticalScrollBar.setStyle("-fx-background-color: #2c2c2c; -fx-background-insets: 0; -fx-padding: 2; -fx-pref-width: 12;");
            }
            javafx.scene.Node verticalThumb = scrollPane.lookup(".scroll-bar:vertical .thumb");
            if (verticalThumb != null) {
                verticalThumb.setStyle("-fx-background-color: #00eaff; -fx-background-insets: 2; -fx-background-radius: 5;");
            }
        } catch (Exception e) {
            System.err.println("Nie udało się ostylować paska przewijania: " + e.getMessage());
        }

        Button closeButton = new Button("Zamknij");
        closeButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);");
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #bb86fc; -fx-text-fill: #121212; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 0, 2); -fx-cursor: hand;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);"));
        closeButton.setOnAction(e -> {
            if (gameController != null) {
                gameController.closeLeaderboard();
            } else {
                System.err.println("GameController jest null, nie można zamknąć rankingu.");
            }
        });

        Button replayButton = new Button("Zagraj ponownie");
        replayButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);");
        replayButton.setOnMouseEntered(e -> replayButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #bb86fc; -fx-text-fill: #121212; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 0, 2); -fx-cursor: hand;"));
        replayButton.setOnMouseExited(e -> replayButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);"));
        replayButton.setOnAction(e -> {
            if (gameController != null) {
                gameController.showNicknamePanel();
            } else {
                System.err.println("GameController jest null, nie można rozpocząć nowej gry.");
            }
        });

        Button exitButton = new Button("Wyjdź z gry");
        exitButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);");
        exitButton.setOnMouseEntered(e -> exitButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #bb86fc; -fx-text-fill: #121212; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 0, 2); -fx-cursor: hand;"));
        exitButton.setOnMouseExited(e -> exitButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);"));
        exitButton.setOnAction(e -> Platform.exit());

        HBox buttonBox = new HBox(15, closeButton, replayButton, exitButton);
        buttonBox.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(title, scrollPane, buttonBox);

        return panel;
    }
}