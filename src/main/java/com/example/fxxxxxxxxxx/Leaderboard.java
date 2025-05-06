package com.example.fxxxxxxxxxx;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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
        private long score; // Zmieniono na long, aby uniknąć przepełnienia przy mnożeniu

        public Score(String nickname, int money, int level) {
            this.nickname = nickname;
            this.money = money;
            this.level = level;
            this.score = (long) money * level; // Rzutowanie na long, aby wynik był long
        }

        public String getNickname() { return nickname; }
        public int getMoney() { return money; }
        public int getLevel() { return level; }
        public long getScore() { return score; } // Zwraca long
    }

    private static final String SCORE_FILE = "resources/scores.txt"; // Upewnij się, że folder "resources" istnieje w głównym folderze projektu
    // lub w miejscu, z którego uruchamiana jest aplikacja.
    // Dla projektów Maven/Gradle, często jest to src/main/resources/
    private final GameController gameController;

    public Leaderboard(GameController gameController) {
        this.gameController = gameController;
        System.out.println("Leaderboard initialized, ensuring directory exists");
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        System.out.println("Ensuring directory exists for: " + SCORE_FILE);
        try {
            File file = new File(SCORE_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                System.out.println("Parent directory does not exist, creating: " + parentDir.getAbsolutePath());
                if (parentDir.mkdirs()) {
                    System.out.println("Created directory: " + parentDir.getAbsolutePath());
                } else {
                    System.err.println("Failed to create directory: " + parentDir.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            String errorMsg = "Failed to ensure directory for " + SCORE_FILE + ": " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            if (gameController != null) gameController.showError(errorMsg); // Sprawdzenie czy gameController nie jest null
        }
    }

    public void saveScore(String nickname, int money, int level) {
        System.out.println("saveScore called with: nickname=" + nickname + ", money=" + money + ", level=" + level);
        if (nickname == null || nickname.trim().isEmpty()) {
            String errorMsg = "Cannot save score: nickname is null or empty";
            System.err.println(errorMsg);
            if (gameController != null) gameController.showError(errorMsg);
            return;
        }
        // Można pozwolić na ujemne pieniądze jeśli to część logiki gry (np. dług)
        // if (money < 0 || level < 0) {
        //     String errorMsg = "Cannot save score: invalid money (" + money + ") or level (" + level + ")";
        //     System.err.println(errorMsg);
        //     if (gameController != null) gameController.showError(errorMsg);
        //     return;
        // }

        File file = new File(SCORE_FILE);
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                System.out.println("Parent directory does not exist, creating: " + parentDir.getAbsolutePath());
                if(!parentDir.mkdirs()){
                    System.err.println("Could not create directory: " + parentDir.getAbsolutePath());
                    // Można rzucić wyjątek lub wyświetlić błąd użytkownikowi
                }
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) { // true - dopisywanie do pliku
                writer.write(nickname + "," + money + "," + level);
                writer.newLine();
                System.out.println("Score saved successfully to: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            String errorMsg = "Failed to save score to " + file.getAbsolutePath() + ": " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            if (gameController != null) gameController.showError(errorMsg);
        }
    }

    public List<Score> loadScores() {
        System.out.println("Loading scores from: " + SCORE_FILE);
        List<Score> scores = new ArrayList<>();
        File file = new File(SCORE_FILE);
        if (!file.exists()) {
            System.out.println("Scores file does not exist at: " + file.getAbsolutePath() + ". A new file will be created on save.");
            return scores; // Zwróć pustą listę, plik zostanie utworzony przy pierwszym zapisie
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    try {
                        String nickname = parts[0].trim();
                        int money = Integer.parseInt(parts[1].trim());
                        int level = Integer.parseInt(parts[2].trim());
                        // Usunięto filtr "Test,0,0" aby pozwolić na różne wpisy testowe,
                        // chyba że jest to konkretne wymaganie.
                        // if ("Test".equalsIgnoreCase(nickname) && money == 0 && level == 0) {
                        //     continue;
                        // }
                        scores.add(new Score(nickname, money, level));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid score format in " + file.getAbsolutePath() + ": '" + line + "'. Error: " + e.getMessage());
                    }
                } else {
                    System.err.println("Invalid score line (incorrect number of parts) in " + file.getAbsolutePath() + ": '" + line + "'");
                }
            }
            System.out.println("Loaded " + scores.size() + " scores from: " + file.getAbsolutePath());
        } catch (IOException e) {
            String errorMsg = "Error reading scores from " + file.getAbsolutePath() + ": " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            if (gameController != null) gameController.showError(errorMsg);
        }
        return scores;
    }

    public VBox createLeaderboardPanel() {
        System.out.println("Creating remodeled leaderboard panel with larger table, smaller font, and transparent background to match gameplay");
        VBox panel = new VBox(25); // Zwiększony odstęp
        panel.setAlignment(Pos.CENTER);
        // Ustawienie przezroczystego tła, aby tło z cząsteczkami było widoczne
        panel.setStyle("-fx-background-color: transparent; -fx-padding: 40;");


        Label title = new Label("Ranking Graczy");
        title.setStyle("-fx-font-size: 32pt; -fx-font-weight: bold; -fx-text-fill: #bb86fc; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 2, 2);");

        TableView<Score> table = new TableView<>();
        table.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(30, 30, 30, 0.85), rgba(44, 44, 44, 0.85)); " + // Lekko przezroczyste tło tabeli
                        "-fx-border-color: #bb86fc; -fx-border-width: 1.5; -fx-border-radius: 10; " +
                        "-fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0, 0, 3); " +
                        "-fx-padding: 5;" // Zmniejszony padding tabeli
        );
        table.setPrefWidth(1000); // Szerokość tabeli
        table.setPrefHeight(600); // Dodana preferowana wysokość dla tabeli, aby lepiej zarządzać rozmiarem w ScrollPane

        table.setRowFactory(tv -> {
            TableRow<Score> row = new TableRow<>();
            // Dynamiczne ustawianie stylu dla naprzemiennych wierszy
            row.indexProperty().addListener((obs, oldIndex, newIndex) -> {
                String baseStyle = "-fx-font-size: 12pt; -fx-padding: 10; -fx-min-height: 50; -fx-alignment: CENTER_LEFT; -fx-border-color: transparent #bb86fc transparent #bb86fc; -fx-border-width: 0 0.5 0 0.5;";
                if (newIndex.intValue() % 2 == 0) {
                    row.setStyle("-fx-background-color: rgba(44, 44, 44, 0.75); -fx-text-fill: #e0e0e0; " + baseStyle);
                } else {
                    row.setStyle("-fx-background-color: rgba(36, 36, 36, 0.75); -fx-text-fill: #e0e0e0; " + baseStyle);
                }
            });
            row.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
                String baseStyleHover = "-fx-font-size: 12pt; -fx-padding: 10; -fx-min-height: 50; -fx-alignment: CENTER_LEFT; -fx-border-color: transparent #bb86fc transparent #bb86fc; -fx-border-width: 0 0.5 0 0.5; -fx-background-radius: 8; -fx-cursor: hand;";
                if (isHovered && !row.isEmpty()) {
                    row.setStyle("-fx-background-color: #6200ee; -fx-text-fill: #ffffff; " + baseStyleHover);
                } else if(!row.isEmpty()){ // Powrót do stylu bazowego po zakończeniu hover
                    String baseStyle = "-fx-font-size: 12pt; -fx-padding: 10; -fx-min-height: 50; -fx-alignment: CENTER_LEFT; -fx-border-color: transparent #bb86fc transparent #bb86fc; -fx-border-width: 0 0.5 0 0.5;";
                    if (row.getIndex() % 2 == 0) {
                        row.setStyle("-fx-background-color: rgba(44, 44, 44, 0.75); -fx-text-fill: #e0e0e0; " + baseStyle);
                    } else {
                        row.setStyle("-fx-background-color: rgba(36, 36, 36, 0.75); -fx-text-fill: #e0e0e0; " + baseStyle);
                    }
                }
            });
            return row;
        });

        String columnHeaderStyle = "-fx-alignment: CENTER; -fx-text-fill: #bb86fc; -fx-font-size: 16pt; -fx-font-weight: bold; -fx-background-color: rgba(30, 30, 30, 0.9); -fx-border-color: #bb86fc; -fx-border-width: 0 0 1 0;"; // Dolna krawędź dla nagłówków

        TableColumn<Score, Void> rankCol = new TableColumn<>("Miejsce");
        rankCol.setSortable(false); // Ranking jest określany przez sortowanie listy, nie przez kolumnę
        rankCol.setCellFactory(col -> {
            TableCell<Score, Void> cell = new TableCell<>();
            cell.setAlignment(Pos.CENTER_LEFT); // Wyrównanie tekstu w komórce
            cell.itemProperty().addListener((obs, oldItem, newItem) -> { // Listener do aktualizacji tekstu
                if (cell.getTableRow() != null && cell.getTableRow().getItem() != null) {
                    cell.setText(String.valueOf(cell.getTableRow().getIndex() + 1));
                } else {
                    cell.setText(null);
                }
            });
            return cell;
        });
        rankCol.setStyle(columnHeaderStyle);
        rankCol.setMinWidth(100); rankCol.setMaxWidth(120); // Ustalenie szerokości

        TableColumn<Score, String> nicknameCol = new TableColumn<>("Pseudonim");
        nicknameCol.setCellValueFactory(new PropertyValueFactory<>("nickname"));
        nicknameCol.setStyle(columnHeaderStyle + "-fx-alignment: CENTER_LEFT;"); // Lewe wyrównanie dla pseudonimu
        nicknameCol.setMinWidth(250);
        nicknameCol.setCellFactory(tc -> new TableCell<Score, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setAlignment(Pos.CENTER_LEFT);
            }
        });


        TableColumn<Score, Integer> moneyCol = new TableColumn<>("Pieniądze");
        moneyCol.setCellValueFactory(new PropertyValueFactory<>("money"));
        moneyCol.setStyle(columnHeaderStyle + "-fx-alignment: CENTER_RIGHT;"); // Prawe wyrównanie dla liczb
        moneyCol.setMinWidth(180);
        moneyCol.setCellFactory(tc -> new TableCell<Score, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,d zł", item));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        TableColumn<Score, Integer> levelCol = new TableColumn<>("Poziom");
        levelCol.setCellValueFactory(new PropertyValueFactory<>("level"));
        levelCol.setStyle(columnHeaderStyle);
        levelCol.setMinWidth(100);  levelCol.setMaxWidth(120);
        levelCol.setCellFactory(tc -> new TableCell<Score, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Score, Long> scoreCol = new TableColumn<>("Wynik");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        scoreCol.setStyle(columnHeaderStyle + "-fx-alignment: CENTER_RIGHT;");
        scoreCol.setMinWidth(180);
        scoreCol.setCellFactory(tc -> new TableCell<Score, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,d", item));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        table.getColumns().addAll(rankCol, nicknameCol, moneyCol, levelCol, scoreCol);
        // Usunięto CONSTRAINED_RESIZE_POLICY aby pozwolić na indywidualne szerokości kolumn i scrollbar poziomy jeśli potrzeba
        // table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        List<Score> scores = loadScores();
        scores.sort((s1, s2) -> Long.compare(s2.getScore(), s1.getScore())); // Sortowanie malejąco wg wyniku
        table.getItems().addAll(scores);

        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToWidth(true); // Dopasuj szerokość ScrollPane do tabeli
        scrollPane.setFitToHeight(true); // Dodano dopasowanie wysokości
        scrollPane.setMaxHeight(700); // Ustawienie maksymalnej wysokości, aby panel nie był za duży
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: #bb86fc; -fx-border-width: 0.5; -fx-border-radius: 10; -fx-background-radius: 10;");
        // Ustawienia dla pasków przewijania (opcjonalne)
        scrollPane.lookup(".scroll-bar:vertical").setStyle("-fx-background-color: #2c2c2c; -fx-background-insets: 0; -fx-padding: 2;");
        scrollPane.lookup(".scroll-bar:vertical .thumb").setStyle("-fx-background-color: #bb86fc; -fx-background-insets: 2; -fx-background-radius: 2;");


        Button closeButton = new Button("Zamknij");
        closeButton.setStyle("-fx-font-size: 20pt; -fx-padding: 15 30; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 12; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 3);");
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-font-size: 20pt; -fx-padding: 15 30; -fx-background-color: #bb86fc; -fx-text-fill: #121212; -fx-background-radius: 12; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 12, 0, 0, 3); -fx-cursor: hand;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-font-size: 20pt; -fx-padding: 15 30; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 12; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 3);"));
        if (gameController != null) { // Upewnij się, że gameController nie jest null przed ustawieniem akcji
            closeButton.setOnAction(e -> gameController.closeLeaderboard());
        } else {
            closeButton.setOnAction(e -> System.err.println("GameController is null in Leaderboard, cannot close."));
        }


        panel.getChildren().addAll(title, scrollPane, closeButton);
        return panel;
    }
}