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
        private long score;

        public Score(String nickname, int money, int level) {
            this.nickname = nickname;
            this.money = money;
            this.level = level;
            this.score = (long) money * level;
        }

        public String getNickname() { return nickname; }
        public int getMoney() { return money; }
        public int getLevel() { return level; }
        public long getScore() { return score; }
    }

    private static final String SCORE_FILE = "resources/scores.txt";
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
                parentDir.mkdirs();
                System.out.println("Created directory: " + parentDir.getAbsolutePath());
            }
        } catch (Exception e) {
            String errorMsg = "Failed to ensure directory for " + SCORE_FILE + ": " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            gameController.showError(errorMsg);
        }
    }

    public void saveScore(String nickname, int money, int level) {
        System.out.println("saveScore called with: nickname=" + nickname + ", money=" + money + ", level=" + level);
        if (nickname == null || nickname.trim().isEmpty()) {
            String errorMsg = "Cannot save score: nickname is null or empty";
            System.err.println(errorMsg);
            gameController.showError(errorMsg);
            return;
        }
        if (money < 0 || level < 0) {
            String errorMsg = "Cannot save score: invalid money (" + money + ") or level (" + level + ")";
            System.err.println(errorMsg);
            gameController.showError(errorMsg);
            return;
        }

        File file = new File(SCORE_FILE);
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                System.out.println("Parent directory does not exist, creating: " + parentDir.getAbsolutePath());
                parentDir.mkdirs();
                System.out.println("Created directory: " + parentDir.getAbsolutePath());
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(nickname + "," + money + "," + level);
                writer.newLine();
                System.out.println("Score saved successfully to: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            String errorMsg = "Failed to save score to " + file.getAbsolutePath() + ": " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            gameController.showError(errorMsg);
        }
    }

    public List<Score> loadScores() {
        System.out.println("Loading scores from: " + SCORE_FILE);
        List<Score> scores = new ArrayList<>();
        File file = new File(SCORE_FILE);
        if (!file.exists()) {
            System.out.println("Scores file does not exist at: " + file.getAbsolutePath());
            return scores;
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
                        // Skip "Test,0,0" entries
                        if ("Test".equalsIgnoreCase(nickname) && money == 0 && level == 0) {
                            continue;
                        }
                        scores.add(new Score(nickname, money, level));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid score format in " + file.getAbsolutePath() + ": " + line);
                    }
                } else {
                    System.err.println("Invalid score line in " + file.getAbsolutePath() + ": " + line);
                }
            }
            System.out.println("Loaded " + scores.size() + " scores from: " + file.getAbsolutePath());
        } catch (IOException e) {
            String errorMsg = "Error reading scores from " + file.getAbsolutePath() + ": " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            gameController.showError(errorMsg);
        }
        return scores;
    }

    public VBox createLeaderboardPanel() {
        System.out.println("Creating remodeled leaderboard panel with larger table, smaller font, and transparent background to match gameplay");
        VBox panel = new VBox(25);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: transparent; -fx-padding: 40;");

        // Title with modernized styling
        Label title = new Label("Ranking Graczy");
        title.setStyle("-fx-font-size: 32pt; -fx-font-weight: bold; -fx-text-fill: #bb86fc; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 2, 2);");

        // Table setup
        TableView<Score> table = new TableView<>();
        table.setStyle("-fx-background-color: linear-gradient(to bottom, #1e1e1e, #2c2c2c); -fx-border-color: #bb86fc; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0, 0, 3); -fx-padding: 15;");
        table.setPrefWidth(1000); // Width at 1000

        // Row factory with alternating colors, smooth hover effect, and smaller font
        table.setRowFactory(tv -> {
            TableRow<Score> row = new TableRow<>();
            row.setStyle("-fx-background-color: " + (row.getIndex() % 2 == 0 ? "#2c2c2c" : "#242424") + "; -fx-text-fill: #ffffff; -fx-font-size: 12pt; -fx-padding: 15; -fx-min-height: 70; -fx-alignment: CENTER; -fx-border-color: #bb86fc; -fx-border-width: 0.5;");
            row.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
                if (isHovered && !row.isEmpty()) {
                    row.setStyle("-fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-font-size: 12pt; -fx-padding: 15; -fx-min-height: 70; -fx-alignment: CENTER; -fx-border-color: #bb86fc; -fx-border-width: 0.5; -fx-background-radius: 8; -fx-transition: background-color 0.3s ease;");
                } else {
                    row.setStyle("-fx-background-color: " + (row.getIndex() % 2 == 0 ? "#2c2c2c" : "#242424") + "; -fx-text-fill: #ffffff; -fx-font-size: 12pt; -fx-padding: 15; -fx-min-height: 70; -fx-alignment: CENTER; -fx-border-color: #bb86fc; -fx-border-width: 0.5;");
                }
            });
            return row;
        });

        // Rank column
        TableColumn<Score, Void> rankCol = new TableColumn<>("Miejsce");
        rankCol.setCellFactory(col -> new TableCell<Score, Void>() {
            @Override
            public void updateIndex(int index) {
                super.updateIndex(index);
                if (index >= 0 && index < table.getItems().size()) {
                    setText(String.valueOf(index + 1));
                } else {
                    setText(null);
                }
            }
        });
        rankCol.setStyle("-fx-alignment: CENTER; -fx-text-fill: #bb86fc; -fx-font-size: 20pt; -fx-font-weight: bold; -fx-background-color: #1e1e1e; -fx-border-color: #bb86fc;");
        rankCol.setMinWidth(120);

        // Nickname column
        TableColumn<Score, String> nicknameCol = new TableColumn<>("Pseudonim");
        nicknameCol.setCellValueFactory(new PropertyValueFactory<>("nickname"));
        nicknameCol.setStyle("-fx-alignment: CENTER; -fx-text-fill: #bb86fc; -fx-font-size: 20pt; -fx-font-weight: bold; -fx-background-color: #1e1e1e; -fx-border-color: #bb86fc; -fx-wrap-text: true;");
        nicknameCol.setMinWidth(300);
        nicknameCol.setCellFactory(tc -> new TableCell<Score, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.length() > 20 ? item.substring(0, 20) + "..." : item);
                }
            }
        });

        // Money column
        TableColumn<Score, Integer> moneyCol = new TableColumn<>("Pieniądze");
        moneyCol.setCellValueFactory(new PropertyValueFactory<>("money"));
        moneyCol.setStyle("-fx-alignment: CENTER; -fx-text-fill: #bb86fc; -fx-font-size: 20pt; -fx-font-weight: bold; -fx-background-color: #1e1e1e; -fx-border-color: #bb86fc; -fx-wrap-text: true;");
        moneyCol.setMinWidth(240);
        moneyCol.setCellFactory(tc -> new TableCell<Score, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d", item));
                }
            }
        });

        // Level column
        TableColumn<Score, Integer> levelCol = new TableColumn<>("Poziom");
        levelCol.setCellValueFactory(new PropertyValueFactory<>("level"));
        levelCol.setStyle("-fx-alignment: CENTER; -fx-text-fill: #bb86fc; -fx-font-size: 20pt; -fx-font-weight: bold; -fx-background-color: #1e1e1e; -fx-border-color: #bb86fc; -fx-wrap-text: true;");
        levelCol.setMinWidth(120);
        levelCol.setCellFactory(tc -> new TableCell<Score, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        // Score column
        TableColumn<Score, Long> scoreCol = new TableColumn<>("Wynik");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        scoreCol.setStyle("-fx-alignment: CENTER; -fx-text-fill: #bb86fc; -fx-font-size: 20pt; -fx-font-weight: bold; -fx-background-color: #1e1e1e; -fx-border-color: #bb86fc; -fx-wrap-text: true;");
        scoreCol.setMinWidth(240);
        scoreCol.setCellFactory(tc -> new TableCell<Score, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d", item));
                }
            }
        });

        table.getColumns().addAll(rankCol, nicknameCol, moneyCol, levelCol, scoreCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load and sort scores
        List<Score> scores = loadScores();
        scores.sort((s1, s2) -> Long.compare(s2.getScore(), s1.getScore()));
        table.getItems().addAll(scores);

        // ScrollPane with increased height
        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(2600); // Height at 2600
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: #bb86fc; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Close button with modernized styling
        Button closeButton = new Button("Zamknij");
        closeButton.setStyle("-fx-font-size: 20pt; -fx-padding: 15; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 12; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 3);");
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-font-size: 20pt; -fx-padding: 15; -fx-background-color: #bb86fc; -fx-text-fill: #121212; -fx-background-radius: 12; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 12, 0, 0, 3);"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-font-size: 20pt; -fx-padding: 15; -fx-background-color: #6200ee; -fx-text-fill: #ffffff; -fx-background-radius: 12; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 3);"));
        closeButton.setOnAction(e -> gameController.closeLeaderboard());

        panel.getChildren().addAll(title, scrollPane, closeButton);
        return panel;
    }
}