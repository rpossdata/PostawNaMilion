package com.example.fxxxxxxxxxx;

import javafx.application.Platform;
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
import javafx.scene.layout.Region;
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
        private int level; // Liczba ukończonych poziomów
        private long scoreValue;

        public Score(String nickname, int money, int level) {
            this.nickname = nickname;
            this.money = money;
            this.level = level;


            double[] multipliers = {0.0, 0.19, 0.29, 0.39, 0.49, 0.59, 0.69, 0.79, 0.89, 0.99};


            if (level < 0 || level >= multipliers.length) {
                this.scoreValue = money;
            } else if (level == 0) {
                this.scoreValue = money-1000000;
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
            if (parentDir != null) {
                if (!parentDir.exists()) {
                    System.out.println("Katalog na dane nie istnieje, próba utworzenia: " + parentDir.getAbsolutePath());
                    if (parentDir.mkdirs()) {
                        System.out.println("Utworzono katalog na dane: " + parentDir.getAbsolutePath());
                    } else {
                        String errorMsg = "Nie udało się utworzyć katalogu na dane: " + parentDir.getAbsolutePath() + ". Sprawdź uprawnienia zapisu.";
                        System.err.println(errorMsg);
                        if (gameController != null) gameController.showError("Błąd krytyczny: " + errorMsg);
                    }
                } else if (!parentDir.isDirectory()) {
                    String errorMsg = "Ścieżka dla katalogu danych istnieje, ale nie jest katalogiem: " + parentDir.getAbsolutePath();
                    System.err.println(errorMsg);
                    if (gameController != null) gameController.showError("Błąd krytyczny: " + errorMsg);
                } else {
                    System.out.println("Katalog na dane istnieje: " + parentDir.getAbsolutePath());
                }
            } else {
                String errorMsg = "Nie można określić katalogu nadrzędnego dla pliku wyników. To nieoczekiwany błąd.";
                System.err.println(errorMsg);
                if (gameController != null) gameController.showError("Błąd krytyczny: " + errorMsg);
            }
        } catch (SecurityException e) {
            String errorMsg = "Problem z uprawnieniami podczas sprawdzania/tworzenia katalogu na dane " + SCORE_DATA_FILE.getParentFile().getAbsolutePath() + ": " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            if (gameController != null) gameController.showError("Błąd krytyczny: " + errorMsg);
        } catch (Exception e) {
            String errorMsg = "Nieoczekiwany błąd podczas sprawdzania/tworzenia katalogu na dane " + SCORE_DATA_FILE.getParentFile().getAbsolutePath() + ": " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            if (gameController != null) gameController.showError("Błąd krytyczny: " + errorMsg);
        }
    }

    public void saveScore(String nickname, int money, int level) {
        System.out.println("Zapisywanie wyniku: pseudonim=" + nickname + ", pieniądze=" + money + ", poziom=" + level);
        if (nickname == null || nickname.trim().isEmpty()) {
            String errorMsg = "Nie można zapisać wyniku: pseudonim jest pusty.";
            System.err.println(errorMsg);
            if (gameController != null) gameController.showError(errorMsg);
            return;
        }
        ensureDataDirectoryExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SCORE_DATA_FILE, true))) {
            writer.write(nickname + "," + money + "," + level);
            writer.newLine();
            System.out.println("Wynik zapisany pomyślnie do: " + SCORE_DATA_FILE.getAbsolutePath());
        } catch (IOException e) {
            String errorMsg = "Nie udało się zapisać wyniku do " + SCORE_DATA_FILE.getAbsolutePath() + ": " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            if (gameController != null) gameController.showError(errorMsg);
        }
    }

    public List<Score> loadScores() {
        System.out.println("Ładowanie wyników z: " + SCORE_DATA_FILE.getAbsolutePath());
        List<Score> scores = new ArrayList<>();
        ensureDataDirectoryExists();
        if (!SCORE_DATA_FILE.exists()) {
            System.out.println("Plik wyników nie istnieje: " + SCORE_DATA_FILE.getAbsolutePath() + ". Zostanie utworzony przy pierwszym zapisie wyniku.");
            return scores;
        }
        if (!SCORE_DATA_FILE.canRead()) {
            String errorMsg = "Nie można odczytać pliku wyników (brak uprawnień): " + SCORE_DATA_FILE.getAbsolutePath();
            System.err.println(errorMsg);
            if (gameController != null) gameController.showError(errorMsg);
            return scores;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(SCORE_DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    try {
                        String nicknamePart = parts[0].trim();
                        int moneyPart = Integer.parseInt(parts[1].trim());
                        int levelPart = Integer.parseInt(parts[2].trim());
                        scores.add(new Score(nicknamePart, moneyPart, levelPart));
                    } catch (NumberFormatException e) {
                        System.err.println("Nieprawidłowy format wyniku w pliku " + SCORE_DATA_FILE.getAbsolutePath() + ": '" + line + "'. Błąd: " + e.getMessage());
                    }
                } else {
                    System.err.println("Nieprawidłowa linia wyniku (zła liczba części) w pliku " + SCORE_DATA_FILE.getAbsolutePath() + ": '" + line + "'");
                }
            }
            System.out.println("Załadowano " + scores.size() + " wyników z: " + SCORE_DATA_FILE.getAbsolutePath());
        } catch (IOException e) {
            String errorMsg = "Błąd odczytu wyników z " + SCORE_DATA_FILE.getAbsolutePath() + ": " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            if (gameController != null) gameController.showError(errorMsg);
        }
        return scores;
    }

    public VBox createLeaderboardPanel() {
        System.out.println("Tworzenie panelu rankingu graczy - poprawki wyglądu i skalowania.");
        VBox panel = new VBox(20); // Zmniejszony odstęp
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: transparent; -fx-padding: 20;"); // Zmniejszony padding

        Label title = new Label("Ranking Graczy");
        title.setStyle("-fx-font-size: 30pt; -fx-font-weight: bold; -fx-text-fill: #bb86fc; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 2, 2);");

        TableView<Score> table = new TableView<>();
        String tableBaseStyle = "-fx-background-color: linear-gradient(to bottom, rgba(30, 30, 30, 0.88), rgba(44, 44, 44, 0.88)); " +
                "-fx-border-color: #bb86fc; -fx-border-width: 1; -fx-border-radius: 12; " +
                "-fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 2); " +
                "-fx-padding: 8;";
        table.setStyle(tableBaseStyle);

        table.setPrefHeight(550);
        Label placeholder = new Label("Brak wyników do wyświetlenia.\nZagraj, aby pojawić się w rankingu!");
        placeholder.setStyle("-fx-font-size: 16pt; -fx-text-fill: #cccccc; -fx-text-alignment: center; -fx-padding: 20px;");
        table.setPlaceholder(placeholder);

        table.setRowFactory(tv -> {
            TableRow<Score> row = new TableRow<>();
            String baseRowStyle = "-fx-font-size: 13pt; -fx-padding: 12 10; -fx-min-height: 50; -fx-alignment: CENTER_LEFT; -fx-border-width: 0 0 0.5 0; -fx-border-color: rgba(187, 134, 252, 0.2);";

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
            row.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
                if (row.isEmpty()) return;
                String hoverEffectStyle = "-fx-background-color: #7e3ff2; -fx-text-fill: #ffffff; -fx-cursor: hand; -fx-background-radius: 6; -fx-border-radius: 6;";
                if (isHovered) {
                    row.setStyle(hoverEffectStyle + baseRowStyle.replaceFirst("-fx-border-color:.*?;", "-fx-border-color: transparent;"));
                } else {
                    if (row.getIndex() % 2 == 0) {
                        row.setStyle("-fx-background-color: rgba(50, 50, 60, 0.78); -fx-text-fill: #e8e8e8; " + baseRowStyle);
                    } else {
                        row.setStyle("-fx-background-color: rgba(40, 40, 50, 0.78); -fx-text-fill: #e8e8e8; " + baseRowStyle);
                    }
                }
            });
            return row;
        });

        String columnHeaderStyle = "-fx-alignment: CENTER; -fx-text-fill: #e0cffc; -fx-font-size: 15pt; -fx-font-weight: bold; -fx-background-color: rgba(25, 25, 35, 0.92); -fx-border-color: #bb86fc; -fx-border-width: 0 0 1.5 0; -fx-padding: 10 5;";

        TableColumn<Score, Void> rankCol = new TableColumn<>("Miejsce");
        rankCol.setSortable(false);
        rankCol.setPrefWidth(80); rankCol.setMinWidth(60); rankCol.setMaxWidth(100);
        rankCol.setStyle(columnHeaderStyle);
        rankCol.setCellFactory(col -> new TableCell<Score, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setGraphic(null);
                } else {
                    setText(String.valueOf(getTableRow().getIndex() + 1));
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Score, String> nicknameCol = new TableColumn<>("Pseudonim");
        nicknameCol.setCellValueFactory(new PropertyValueFactory<>("nickname"));
        nicknameCol.setPrefWidth(230); nicknameCol.setMinWidth(180);
        nicknameCol.setStyle(columnHeaderStyle + "-fx-alignment: CENTER_LEFT;");
        nicknameCol.setCellFactory(tc -> new TableCell<Score, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new javafx.geometry.Insets(0, 0, 0, 10));
            }
        });

        TableColumn<Score, Integer> moneyCol = new TableColumn<>("Pieniądze");
        moneyCol.setCellValueFactory(new PropertyValueFactory<>("money"));
        moneyCol.setPrefWidth(150); moneyCol.setMinWidth(120);
        moneyCol.setStyle(columnHeaderStyle + "-fx-alignment: CENTER_RIGHT;");
        moneyCol.setCellFactory(tc -> new TableCell<Score, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,d zł", item));
                setAlignment(Pos.CENTER_RIGHT);
                setPadding(new javafx.geometry.Insets(0, 10, 0, 0));
            }
        });

        TableColumn<Score, Integer> levelCol = new TableColumn<>("Ukończone Poziomy");
        levelCol.setCellValueFactory(new PropertyValueFactory<>("level"));
        levelCol.setPrefWidth(150); levelCol.setMinWidth(130);
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
        scoreCol.setPrefWidth(150); scoreCol.setMinWidth(120);
        scoreCol.setStyle(columnHeaderStyle + "-fx-alignment: CENTER_RIGHT;");
        scoreCol.setCellFactory(tc -> new TableCell<Score, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,d", item));
                setAlignment(Pos.CENTER_RIGHT);
                setPadding(new javafx.geometry.Insets(0, 10, 0, 0));
            }
        });

        table.getColumns().addAll(rankCol, nicknameCol, moneyCol, levelCol, scoreCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        List<Score> scores = loadScores();
        if (scores.isEmpty()) {
            System.out.println("Brak wyników do wyświetlenia w tabeli.");
        } else {
            System.out.println("Sortowanie i dodawanie " + scores.size() + " wyników do tabeli.");
        }
        scores.sort((s1, s2) -> Long.compare(s2.getScoreValue(), s1.getScoreValue()));
        table.getItems().addAll(scores);

        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");

        try {
            javafx.scene.Node verticalScrollBar = scrollPane.lookup(".scroll-bar:vertical");
            if (verticalScrollBar != null) {
                verticalScrollBar.setStyle("-fx-background-color: #2c2c2c; -fx-background-insets: 0; -fx-padding: 2; -fx-pref-width: 12;");
            }
            javafx.scene.Node verticalThumb = scrollPane.lookup(".scroll-bar:vertical .thumb");
            if (verticalThumb != null) {
                verticalThumb.setStyle("-fx-background-color: #bb86fc; -fx-background-insets: 2; -fx-background-radius: 5;");
            }
        } catch (Exception e) {
            System.err.println("Nie udało się ostylować paska przewijania przez lookup: " + e.getMessage());
        }

        Button closeButton = new Button("Zamknij");
        closeButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);");
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #bb86fc; -fx-text-fill: #121212; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 0, 2); -fx-cursor: hand;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);"));
        if (gameController != null) {
            closeButton.setOnAction(e -> gameController.closeLeaderboard());
        } else {
            closeButton.setOnAction(e -> System.err.println("GameController jest null w Leaderboard, nie można zamknąć."));
        }

        Button replayButton = new Button("Zagraj ponownie");
        replayButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);");
        replayButton.setOnMouseEntered(e -> replayButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #bb86fc; -fx-text-fill: #121212; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 0, 2); -fx-cursor: hand;"));
        replayButton.setOnMouseExited(e -> replayButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);"));
        if (gameController != null) {
            replayButton.setOnAction(e -> gameController.showNicknamePanel());
        } else {
            replayButton.setOnAction(e -> System.err.println("GameController jest null w Leaderboard, nie można rozpocząć nowej gry."));
        }

        Button exitButton = new Button("Wyjdź z gry");
        exitButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);");
        exitButton.setOnMouseEntered(e -> exitButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #bb86fc; -fx-text-fill: #121212; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 0, 2); -fx-cursor: hand;"));
        exitButton.setOnMouseExited(e -> exitButton.setStyle("-fx-font-size: 18pt; -fx-padding: 12 25; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);"));
        exitButton.setOnAction(e -> Platform.exit());

        HBox buttonBox = new HBox(15, closeButton, replayButton, exitButton);
        buttonBox.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(title, scrollPane, buttonBox);
        panel.setMaxWidth(900); // Zmniejszona szerokość panelu

        return panel;
    }
}
