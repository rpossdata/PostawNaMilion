package com.example.fxxxxxxxxxx;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Glow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.RotateTransition;
import javafx.util.Duration;
import java.util.ArrayList; // Keep this if used, though not directly in the provided snippet for banknote iteration

public class UIFactory {
    private Label questionLabel, timerLabel, moneyLabel, levelLabel;
    private Label[] answerLabels = new Label[4];
    private Pane[] answerDropPanes = new Pane[4];
    private Label[] answerStakeLabels = new Label[4];
    private Button[] allInButtons = new Button[4];
    private Button lifeline5050, lifelineAudience, lifelinePhone, confirmButton, surrenderButton;
    private ProgressBar timerProgress, levelProgress;
    private Pane banknoteStackPane;
    private BanknoteManager banknoteManager;
    private GameController gameController;

    public UIFactory(BanknoteManager banknoteManager, GameController gameController) {
        this.banknoteManager = banknoteManager;
        this.gameController = gameController;
    }

    public VBox createGameContent(StackPane root) {
        VBox gameContent = new VBox(20); // Spacing between child nodes
        gameContent.maxWidthProperty().bind(root.widthProperty().multiply(0.9)); // Max width is 90% of root
        gameContent.setAlignment(Pos.CENTER); // Center content vertically and horizontally

        // Create different sections of the UI
        HBox topPanel = createTopPanel(gameContent);
        levelProgress = createLevelProgress(gameContent);
        GridPane answerPanel = createAnswerPanel(gameContent);
        HBox bottomPanel = createBottomPanel(gameContent);

        // Add all sections to the main game content VBox
        gameContent.getChildren().addAll(topPanel, levelProgress, answerPanel, bottomPanel);
        return gameContent;
    }

    private HBox createTopPanel(VBox gameContent) {
        HBox topPanel = new HBox(20); // Spacing between elements in HBox
        topPanel.setAlignment(Pos.CENTER); // Center elements in HBox

        // Question Label
        questionLabel = new Label("Pytanie"); // Default text
        questionLabel.setStyle("-fx-font-size: 22pt; -fx-font-weight: bold; -fx-text-fill: #00eaff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 8, 0, 2, 2);");
        questionLabel.setWrapText(true); // Allow text to wrap
        questionLabel.maxWidthProperty().bind(gameContent.maxWidthProperty().multiply(0.5)); // Max width relative to parent

        // Timer Label
        timerLabel = new Label("120s"); // Default text
        timerLabel.setStyle("-fx-font-size: 18pt; -fx-font-weight: bold; -fx-text-fill: #ff5555; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 1, 1);");

        // Timer Progress Bar
        timerProgress = new ProgressBar(1.0); // Default progress
        timerProgress.prefWidthProperty().bind(gameContent.maxWidthProperty().multiply(0.2)); // Preferred width
        timerProgress.setStyle("-fx-pref-height: 25; -fx-accent: #00ff88; -fx-background-color: #333; -fx-background-radius: 5;");
        // Listener to change color based on progress
        timerProgress.progressProperty().addListener((obs, oldVal, newVal) -> {
            double progress = newVal.doubleValue();
            if (progress <= 0.1) { // Critical time
                timerProgress.setStyle("-fx-pref-height: 25; -fx-accent: #ff0000; -fx-background-color: #333; -fx-background-radius: 5;");
            } else if (progress <= 0.25) { // Warning time
                timerProgress.setStyle("-fx-pref-height: 25; -fx-accent: #ff5555; -fx-background-color: #333; -fx-background-radius: 5;");
            } else { // Normal time
                timerProgress.setStyle("-fx-pref-height: 25; -fx-accent: #00ff88; -fx-background-color: #333; -fx-background-radius: 5;");
            }
        });

        // Level Label
        levelLabel = new Label("Poziom: 1"); // Default text
        levelLabel.setStyle("-fx-font-size: 16pt; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        topPanel.getChildren().addAll(questionLabel, timerLabel, timerProgress, levelLabel);
        return topPanel;
    }

    private ProgressBar createLevelProgress(VBox gameContent) {
        ProgressBar progress = new ProgressBar(0.0); // Initial progress
        progress.prefWidthProperty().bind(gameContent.maxWidthProperty()); // Width binds to parent
        progress.setStyle("-fx-pref-height: 20; -fx-accent: #ffaa00; -fx-background-color: #333; -fx-background-radius: 5;");
        return progress;
    }

    private GridPane createAnswerPanel(VBox gameContent) {
        GridPane answerPanel = new GridPane();
        answerPanel.setHgap(15); // Horizontal gap
        answerPanel.setVgap(20); // Vertical gap
        answerPanel.setAlignment(Pos.CENTER); // Center grid content

        for (int i = 0; i < 4; i++) {
            // Answer Labels
            answerLabels[i] = new Label("Odpowiedź " + (i + 1));
            answerLabels[i].setStyle("-fx-font-size: 16pt; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-background-color: #2a2a4a; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 2, 2);");
            answerLabels[i].setWrapText(true);
            answerLabels[i].maxWidthProperty().bind(gameContent.maxWidthProperty().multiply(0.35));

            // Answer Drop Panes (for banknotes)
            answerDropPanes[i] = new Pane();
            answerDropPanes[i].setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #00eaff; -fx-border-radius: 8; -fx-background-radius: 8;");
            answerDropPanes[i].prefWidthProperty().bind(gameContent.maxWidthProperty().multiply(0.2));
            answerDropPanes[i].setMinHeight(50); // Minimum height for dropping

            // Stake Labels (money bet on this answer)
            answerStakeLabels[i] = new Label("0 zł");
            answerStakeLabels[i].setStyle("-fx-font-size: 14pt; -fx-text-fill: #ffffff; -fx-padding: 5;");

            // "All In" Buttons
            allInButtons[i] = createAllInButton(i);
            allInButtons[i].setTooltip(new Tooltip("Postaw wszystko na tę odpowiedź")); // Tooltip for clarity

            final int finalI = i; // Effectively final variable for lambda expressions
            // Drag Over event for drop panes
            answerDropPanes[i].setOnDragOver(event -> {
                if (event.getGestureSource() != answerDropPanes[finalI] && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE); // Accept only if data is present and source is different
                }
                event.consume();
            });
            // Drag Dropped event for drop panes
            answerDropPanes[i].setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) { // Check if dragboard has the expected string (banknote index)
                    try {
                        int banknoteGlobalIndex = Integer.parseInt(db.getString()); // Get banknote's global index
                        if (banknoteManager != null && banknoteManager.getBanknotes() != null &&
                                banknoteGlobalIndex >= 0 && banknoteGlobalIndex < banknoteManager.getBanknotes().size()) {

                            Label banknote = banknoteManager.getBanknotes().get(banknoteGlobalIndex);
                            Pane sourcePane = banknoteManager.getBanknoteLocations().get(banknote);

                            if (sourcePane != null && sourcePane != answerDropPanes[finalI]) {
                                sourcePane.getChildren().remove(banknote); // Remove from old parent
                                answerDropPanes[finalI].getChildren().add(banknote); // Add to new parent (this drop pane)
                                // Position banknote within the drop pane (optional, could be managed by layout)
                                banknote.setLayoutX(10); // Example positioning
                                banknote.setLayoutY(answerDropPanes[finalI].getChildren().size() * 5 - 5); // Stagger
                                banknoteManager.getBanknoteLocations().put(banknote, answerDropPanes[finalI]); // Update location map
                                banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes); // Update UI
                                success = true;
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing banknote index from dragboard: " + db.getString());
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });

            VBox answerBox = new VBox(5); // VBox to hold drop pane and stake label
            answerBox.setAlignment(Pos.CENTER);
            answerBox.getChildren().addAll(answerDropPanes[i], answerStakeLabels[i]);

            answerPanel.add(answerLabels[i], 0, i); // Column 0, Row i
            answerPanel.add(answerBox, 1, i);      // Column 1, Row i
            answerPanel.add(allInButtons[i], 2, i); // Column 2, Row i
        }
        return answerPanel;
    }

    private HBox createBottomPanel(VBox gameContent) {
        HBox bottomPanel = new HBox(20); // Spacing
        bottomPanel.setAlignment(Pos.CENTER);

        // Money Label (remaining money)
        moneyLabel = new Label("Pozostało: 1 000 000 zł");
        moneyLabel.setStyle("-fx-font-size: 18pt; -fx-font-weight: bold; -fx-text-fill: #00ff88; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 1, 1);");

        // Banknote Stack Pane (where player's banknotes are initially)
        banknoteStackPane = new Pane();
        banknoteStackPane.setStyle("-fx-background-color: #333; -fx-border-color: #ffaa00; -fx-border-radius: 8; -fx-background-radius: 8;");
        banknoteStackPane.prefWidthProperty().bind(gameContent.maxWidthProperty().multiply(0.2));
        banknoteStackPane.setMinHeight(50); // Min height for dropping
        // Drag Over event for banknote stack
        banknoteStackPane.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        // Drag Dropped event for banknote stack
        banknoteStackPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                try {
                    int banknoteGlobalIndex = Integer.parseInt(db.getString());
                    if (banknoteManager != null && banknoteManager.getBanknotes() != null &&
                            banknoteGlobalIndex >= 0 && banknoteGlobalIndex < banknoteManager.getBanknotes().size()) {

                        Label banknote = banknoteManager.getBanknotes().get(banknoteGlobalIndex);
                        Pane sourcePane = banknoteManager.getBanknoteLocations().get(banknote);

                        if (sourcePane != null && sourcePane != banknoteStackPane) { // Ensure it's coming from an answer pane
                            sourcePane.getChildren().remove(banknote);
                            banknoteStackPane.getChildren().add(banknote);
                            // Position banknote within the stack pane (e.g., cascade)
                            banknote.setLayoutX(5 + (banknoteStackPane.getChildren().size() % 10) * 2); // Example positioning
                            banknote.setLayoutY(5 + (banknoteStackPane.getChildren().size() / 10) * 2); // Example positioning
                            banknoteManager.getBanknoteLocations().put(banknote, banknoteStackPane);
                            banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);
                            success = true;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing banknote index from dragboard: " + db.getString());
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        // Lifeline, Confirm, and Surrender Buttons
        // Corrected method calls to GameController
        lifeline5050 = createStyledButton("\u2753", e -> gameController.useLifeline5050()); // ❓ (Question mark icon)
        lifeline5050.setTooltip(new Tooltip("Koło ratunkowe 50:50"));

        lifelineAudience = createStyledButton("\uD83D\uDE4B", e -> gameController.useLifelineAudience()); // 🙋 (Person raising hand)
        lifelineAudience.setTooltip(new Tooltip("Zapytaj publiczność"));

        lifelinePhone = createStyledButton("\uD83D\uDCDE", e -> gameController.useLifelinePhone()); // 📞 (Telephone receiver)
        lifelinePhone.setTooltip(new Tooltip("Telefon do przyjaciela"));

        confirmButton = createStyledButton("\u2705", e -> gameController.confirmAnswer()); // ✅ (Check mark button)
        confirmButton.setTooltip(new Tooltip("Potwierdź odpowiedź"));

        surrenderButton = createStyledButton("\uD83D\uDEA9", e -> gameController.handleSurrender()); // 🏳️ (Waving white flag)
        surrenderButton.setTooltip(new Tooltip("Poddaj się"));

        bottomPanel.getChildren().addAll(moneyLabel, banknoteStackPane, lifeline5050, lifelineAudience, lifelinePhone, confirmButton, surrenderButton);
        return bottomPanel;
    }

    private Button createStyledButton(String text, EventHandler<ActionEvent> handler) {
        Button button = new Button(text);
        String baseStyle = getButtonBaseStyle();
        String hoverStyle = getButtonHoverStyle();
        String disabledStyle = getButtonDisabledStyle();

        button.setStyle(baseStyle);
        // Removed the setOnAction here as it's set again below, which is redundant.
        // The second setOnAction includes logic for disabled style, which is better.

        // Rotation animations for hover effect
        RotateTransition rtEnter = new RotateTransition(Duration.millis(200), button);
        rtEnter.setToAngle(10); // Rotate slightly on enter

        RotateTransition rtExit = new RotateTransition(Duration.millis(200), button);
        rtExit.setToAngle(0); // Rotate back on exit

        button.setOnMouseEntered(e -> {
            if (!button.isDisabled()) { // Apply hover effects only if not disabled
                rtEnter.playFromStart();
                button.setEffect(new Glow(0.7));
                button.setStyle(hoverStyle);
            }
        });
        button.setOnMouseExited(e -> {
            if (!button.isDisabled()) { // Reset effects only if not disabled
                rtExit.playFromStart();
                button.setEffect(new Glow(0.5)); // Or null if no base glow
                button.setStyle(baseStyle);
            }
        });

        // Set the action and handle disabled style update
        button.setOnAction(e -> {
            handler.handle(e); // Execute the passed action
            // After action, if button becomes disabled (e.g. lifeline used), apply disabled style
            if (button.isDisable()) {
                button.setStyle(disabledStyle);
                button.setEffect(null); // Remove glow effect when disabled
            }
        });

        // Listener for disable property to update style immediately when disabled externally
        button.disabledProperty().addListener((obs, واsDisabled, isNowDisabled) -> {
            if (isNowDisabled) {
                button.setStyle(disabledStyle);
                button.setEffect(null);
            } else {
                button.setStyle(baseStyle);
                // Optionally re-apply base effect if any
            }
        });

        return button;
    }

    private Button createAllInButton(int answerIndex) {
        Button button = new Button("All In");
        // Specific styles for "All In" button
        String baseStyle = "-fx-font-size: 14pt; -fx-padding: 10; -fx-background-color: linear-gradient(to bottom, #55ff55, #22cc22); -fx-text-fill: #ffffff; -fx-border-color: #ccffcc; -fx-border-width: 2; -fx-background-radius: 15; -fx-border-radius: 15; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.8), 8, 0.7, 0, 0);";
        String hoverStyle = "-fx-font-size: 14pt; -fx-padding: 10; -fx-background-color: linear-gradient(to bottom, #77ff77, #44dd44); -fx-text-fill: #ffffff; -fx-border-color: #ccffcc; -fx-border-width: 2; -fx-background-radius: 15; -fx-border-radius: 15; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,255,255,1.0), 12, 0.9, 0, 0);";

        button.setStyle(baseStyle);

        // Action for "All In" button
        button.setOnAction(e -> {
            if (banknoteManager == null || gameController == null || answerDropPanes == null || banknoteStackPane == null) return;

            // This logic should ideally be in GameController or BanknoteManager to keep UIFactory cleaner.
            // For now, directly calling BanknoteManager methods.

            // 1. Move all banknotes from OTHER answer panes back to the main stack.
            for (int i = 0; i < answerDropPanes.length; i++) {
                if (i != answerIndex && answerDropPanes[i] != null) {
                    banknoteManager.moveBanknotesFromPaneToStack(answerDropPanes[i], banknoteStackPane);
                }
            }

            // 2. Move all banknotes from the main stack to the TARGET answer pane.
            if (answerDropPanes[answerIndex] != null) {
                banknoteManager.moveAllBanknotesToPane(answerDropPanes[answerIndex], banknoteStackPane);
            }

            // 3. Update UI elements.
            banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);
        });

        // Hover animations
        RotateTransition rtEnter = new RotateTransition(Duration.millis(200), button);
        rtEnter.setToAngle(10);
        RotateTransition rtExit = new RotateTransition(Duration.millis(200), button);
        rtExit.setToAngle(0);

        button.setOnMouseEntered(e -> {
            rtEnter.playFromStart();
            button.setEffect(new Glow(0.7));
            button.setStyle(hoverStyle);
        });
        button.setOnMouseExited(e -> {
            rtExit.playFromStart();
            button.setEffect(new Glow(0.5)); // Or null
            button.setStyle(baseStyle);
        });

        return button;
    }

    // Style definitions for standard buttons
    public String getButtonBaseStyle() {
        return "-fx-font-size: 28pt; -fx-padding: 15; -fx-background-color: linear-gradient(to bottom, #33ccff, #0033cc); -fx-text-fill: #ffffff; -fx-border-color: #e6f3ff; -fx-border-width: 2; -fx-background-radius: 20; -fx-border-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.8), 8, 0.7, 0, 0);";
    }

    public String getButtonHoverStyle() {
        return "-fx-font-size: 28pt; -fx-padding: 15; -fx-background-color: linear-gradient(to bottom, #66d9ff, #0066cc); -fx-text-fill: #ffffff; -fx-border-color: #e6f3ff; -fx-border-width: 2; -fx-background-radius: 20; -fx-border-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,255,255,1.0), 12, 0.9, 0, 0);";
    }

    public String getButtonDisabledStyle() {
        return "-fx-font-size: 28pt; -fx-padding: 15; -fx-background-color: #555; -fx-text-fill: #aaa; -fx-border-color: #666; -fx-border-width: 2; -fx-background-radius: 20; -fx-border-radius: 20; -fx-cursor: default; -fx-effect: none;"; // Changed cursor to default for disabled
    }

    // Getters for UI elements, allowing GameController to access them
    public Label getQuestionLabel() { return questionLabel; }
    public Label getTimerLabel() { return timerLabel; }
    public Label getMoneyLabel() { return moneyLabel; }
    public Label getLevelLabel() { return levelLabel; }
    public Label[] getAnswerLabels() { return answerLabels; }
    public Pane[] getAnswerDropPanes() { return answerDropPanes; }
    public Label[] getAnswerStakeLabels() { return answerStakeLabels; }
    public Button[] getAllInButtons() { return allInButtons; }
    public Button getLifeline5050() { return lifeline5050; }
    public Button getLifelineAudience() { return lifelineAudience; }
    public Button getLifelinePhone() { return lifelinePhone; }
    public Button getConfirmButton() { return confirmButton; }
    public Button getSurrenderButton() { return surrenderButton; }
    public ProgressBar getTimerProgress() { return timerProgress; }
    public ProgressBar getLevelProgress() { return levelProgress; }
    public Pane getBanknoteStackPane() { return banknoteStackPane; }
}
