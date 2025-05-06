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
import java.util.ArrayList;

public class UIFactory {
    private Label questionLabel, timerLabel, moneyLabel, levelLabel;
    private Label[] answerLabels = new Label[4];
    private Pane[] answerDropPanes = new Pane[4];
    private Label[] answerStakeLabels = new Label[4];
    private Button[] allInButtons = new Button[4];
    private Button lifeline5050, lifelineAudience, lifelinePhone, confirmButton;
    private ProgressBar timerProgress, levelProgress;
    private Pane banknoteStackPane;
    private BanknoteManager banknoteManager;
    private GameController gameController; // Nowe pole do przechowywania instancji GameController

    public UIFactory(BanknoteManager banknoteManager, GameController gameController) {
        this.banknoteManager = banknoteManager;
        this.gameController = gameController;
    }

    public VBox createGameContent(StackPane root) {
        VBox gameContent = new VBox(20);
        gameContent.maxWidthProperty().bind(root.widthProperty().multiply(0.9));
        gameContent.setAlignment(Pos.CENTER);

        HBox topPanel = createTopPanel(gameContent);
        levelProgress = createLevelProgress(gameContent);
        GridPane answerPanel = createAnswerPanel(gameContent);
        HBox bottomPanel = createBottomPanel(gameContent);

        gameContent.getChildren().addAll(topPanel, levelProgress, answerPanel, bottomPanel);
        return gameContent;
    }

    private HBox createTopPanel(VBox gameContent) {
        HBox topPanel = new HBox(20);
        topPanel.setAlignment(Pos.CENTER);
        questionLabel = new Label("Pytanie");
        questionLabel.setStyle("-fx-font-size: 22pt; -fx-font-weight: bold; -fx-text-fill: #00eaff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 8, 0, 2, 2);");
        questionLabel.setWrapText(true);
        questionLabel.maxWidthProperty().bind(gameContent.maxWidthProperty().multiply(0.5));

        timerLabel = new Label("120s");
        timerLabel.setStyle("-fx-font-size: 18pt; -fx-font-weight: bold; -fx-text-fill: #ff5555; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 1, 1);");

        timerProgress = new ProgressBar(1.0);
        timerProgress.prefWidthProperty().bind(gameContent.maxWidthProperty().multiply(0.2));
        timerProgress.setStyle("-fx-pref-height: 25; -fx-accent: #00ff88; -fx-background-color: #333; -fx-background-radius: 5;");

        levelLabel = new Label("Poziom: 1");
        levelLabel.setStyle("-fx-font-size: 16pt; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        topPanel.getChildren().addAll(questionLabel, timerLabel, timerProgress, levelLabel);
        return topPanel;
    }

    private ProgressBar createLevelProgress(VBox gameContent) {
        ProgressBar progress = new ProgressBar(0.0);
        progress.prefWidthProperty().bind(gameContent.maxWidthProperty());
        progress.setStyle("-fx-pref-height: 20; -fx-accent: #ffaa00; -fx-background-color: #333; -fx-background-radius: 5;");
        return progress;
    }

    private GridPane createAnswerPanel(VBox gameContent) {
        GridPane answerPanel = new GridPane();
        answerPanel.setHgap(15);
        answerPanel.setVgap(20);
        answerPanel.setAlignment(Pos.CENTER);
        for (int i = 0; i < 4; i++) {
            answerLabels[i] = new Label("Odpowiedź " + (i + 1));
            answerLabels[i].setStyle("-fx-font-size: 16pt; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-background-color: #2a2a4a; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 2, 2);");
            answerLabels[i].setWrapText(true);
            answerLabels[i].maxWidthProperty().bind(gameContent.maxWidthProperty().multiply(0.35));

            answerDropPanes[i] = new Pane();
            answerDropPanes[i].setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #00eaff; -fx-border-radius: 8; -fx-background-radius: 8;");
            answerDropPanes[i].prefWidthProperty().bind(gameContent.maxWidthProperty().multiply(0.2));
            answerDropPanes[i].setMinHeight(50);

            answerStakeLabels[i] = new Label("0 zł");
            answerStakeLabels[i].setStyle("-fx-font-size: 14pt; -fx-text-fill: #ffffff; -fx-padding: 5;");

            allInButtons[i] = createAllInButton(i);
            allInButtons[i].setTooltip(new Tooltip("Postaw wszystko na tę odpowiedź"));

            int finalI = i;
            answerDropPanes[i].setOnDragOver(event -> {
                if (event.getGestureSource() != answerDropPanes[finalI] && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });
            answerDropPanes[i].setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    int index = Integer.parseInt(db.getString());
                    if (index >= 0 && index < banknoteManager.getBanknotes().size()) {
                        Label banknote = banknoteManager.getBanknotes().get(index);
                        Pane sourcePane = banknoteManager.getBanknoteLocations().get(banknote);
                        if (sourcePane != answerDropPanes[finalI]) {
                            sourcePane.getChildren().remove(banknote);
                            answerDropPanes[finalI].getChildren().add(banknote);
                            banknote.setLayoutX(event.getX() - banknote.getWidth() / 2);
                            banknote.setLayoutY(event.getY() - banknote.getHeight() / 2);
                            banknoteManager.getBanknoteLocations().put(banknote, answerDropPanes[finalI]);
                            banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);
                            success = true;
                        }
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });

            VBox answerBox = new VBox(5);
            answerBox.getChildren().addAll(answerDropPanes[i], answerStakeLabels[i]);
            answerPanel.add(answerLabels[i], 0, i);
            answerPanel.add(answerBox, 1, i);
            answerPanel.add(allInButtons[i], 2, i);
        }
        return answerPanel;
    }

    private HBox createBottomPanel(VBox gameContent) {
        HBox bottomPanel = new HBox(20);
        bottomPanel.setAlignment(Pos.CENTER);
        moneyLabel = new Label("Pozostało: 1 000 000 zł");
        moneyLabel.setStyle("-fx-font-size: 18pt; -fx-font-weight: bold; -fx-text-fill: #00ff88; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 1, 1);");

        banknoteStackPane = new Pane();
        banknoteStackPane.setStyle("-fx-background-color: #333; -fx-border-color: #ffaa00; -fx-border-radius: 8; -fx-background-radius: 8;");
        banknoteStackPane.prefWidthProperty().bind(gameContent.maxWidthProperty().multiply(0.2));
        banknoteStackPane.setMinHeight(50);
        banknoteStackPane.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        banknoteStackPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int index = Integer.parseInt(db.getString());
                if (index >= 0 && index < banknoteManager.getBanknotes().size()) {
                    Label banknote = banknoteManager.getBanknotes().get(index);
                    Pane sourcePane = banknoteManager.getBanknoteLocations().get(banknote);
                    if (sourcePane != banknoteStackPane) {
                        sourcePane.getChildren().remove(banknote);
                        banknoteStackPane.getChildren().add(banknote);
                        banknote.setLayoutX(event.getX() - banknote.getWidth() / 2);
                        banknote.setLayoutY(event.getY() - banknote.getHeight() / 2);
                        banknoteManager.getBanknoteLocations().put(banknote, banknoteStackPane);
                        banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);
                        success = true;
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        lifeline5050 = createStyledButton("\u2753", e -> gameController.use5050());
        lifeline5050.setTooltip(new Tooltip("Koło ratunkowe 50:50"));
        lifelineAudience = createStyledButton("\uD83D\uDE4B", e -> gameController.useAudience());
        lifelineAudience.setTooltip(new Tooltip("Zapytaj publiczność"));
        lifelinePhone = createStyledButton("\uD83D\uDCDE", e -> gameController.usePhone());
        lifelinePhone.setTooltip(new Tooltip("Telefon do przyjaciela"));
        confirmButton = createStyledButton("\u2705", e -> gameController.confirmAnswer());
        confirmButton.setTooltip(new Tooltip("Potwierdź odpowiedź"));

        bottomPanel.getChildren().addAll(moneyLabel, banknoteStackPane, lifeline5050, lifelineAudience, lifelinePhone, confirmButton);
        return bottomPanel;
    }

    private Button createStyledButton(String text, EventHandler<ActionEvent> handler) {
        Button button = new Button(text);
        String baseStyle = getButtonBaseStyle();
        String hoverStyle = getButtonHoverStyle();
        String disabledStyle = getButtonDisabledStyle();

        button.setStyle(baseStyle);
        button.setOnAction(handler);

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
            button.setEffect(new Glow(0.5));
            button.setStyle(baseStyle);
        });

        button.setOnAction(e -> {
            handler.handle(e);
            if (button.isDisable()) {
                button.setStyle(disabledStyle);
            }
        });

        return button;
    }

    private Button createAllInButton(int answerIndex) {
        Button button = new Button("All In");
        String baseStyle = "-fx-font-size: 14pt; -fx-padding: 10; -fx-background-color: linear-gradient(to bottom, #55ff55, #22cc22); -fx-text-fill: #ffffff; -fx-border-color: #ccffcc; -fx-border-width: 2; -fx-background-radius: 15; -fx-border-radius: 15; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.8), 8, 0.7, 0, 0);";
        String hoverStyle = "-fx-font-size: 14pt; -fx-padding: 10; -fx-background-color: linear-gradient(to bottom, #77ff77, #44dd44); -fx-text-fill: #ffffff; -fx-border-color: #ccffcc; -fx-border-width: 2; -fx-background-radius: 15; -fx-border-radius: 15; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,255,255,1.0), 12, 0.9, 0, 0);";

        button.setStyle(baseStyle);

        button.setOnAction(e -> {
            for (int i = 0; i < 4; i++) {
                if (i != answerIndex) {
                    for (Node banknote : new ArrayList<>(answerDropPanes[i].getChildren())) {
                        if (banknoteManager.getBanknoteLocations().containsKey(banknote)) {
                            answerDropPanes[i].getChildren().remove(banknote);
                            banknoteStackPane.getChildren().add(banknote);
                            banknote.setLayoutX(10 + (banknoteStackPane.getChildren().size() % 5) * 5);
                            banknote.setLayoutY(5 + (banknoteStackPane.getChildren().size() / 5) * 5);
                            banknoteManager.getBanknoteLocations().put((Label) banknote, banknoteStackPane);
                        }
                    }
                }
            }

            int banknoteCount = banknoteManager.getBanknotes().size();
            answerDropPanes[answerIndex].getChildren().clear();
            for (int j = 0; j < banknoteCount; j++) {
                Label banknote = banknoteManager.getBanknotes().get(j);
                banknoteStackPane.getChildren().remove(banknote);
                answerDropPanes[answerIndex].getChildren().add(banknote);
                banknote.setLayoutX(10 + (j % 5) * 5);
                banknote.setLayoutY(5 + (j / 5) * 5);
                banknoteManager.getBanknoteLocations().put(banknote, answerDropPanes[answerIndex]);
            }

            banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);
        });

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
            button.setEffect(new Glow(0.5));
            button.setStyle(baseStyle);
        });

        return button;
    }

    public String getButtonBaseStyle() {
        return "-fx-font-size: 28pt; -fx-padding: 15; -fx-background-color: linear-gradient(to bottom, #33ccff, #0033cc); -fx-text-fill: #ffffff; -fx-border-color: #e6f3ff; -fx-border-width: 2; -fx-background-radius: 20; -fx-border-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.8), 8, 0.7, 0, 0);";
    }

    public String getButtonHoverStyle() {
        return "-fx-font-size: 28pt; -fx-padding: 15; -fx-background-color: linear-gradient(to bottom, #66d9ff, #0066cc); -fx-text-fill: #ffffff; -fx-border-color: #e6f3ff; -fx-border-width: 2; -fx-background-radius: 20; -fx-border-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,255,255,1.0), 12, 0.9, 0, 0);";
    }

    public String getButtonDisabledStyle() {
        return "-fx-font-size: 28pt; -fx-padding: 15; -fx-background-color: #555; -fx-text-fill: #aaa; -fx-border-color: #666; -fx-border-width: 2; -fx-background-radius: 20; -fx-border-radius: 20; -fx-cursor: hand; -fx-effect: none;";
    }

    public Label getQuestionLabel() {
        return questionLabel;
    }

    public Label getTimerLabel() {
        return timerLabel;
    }

    public Label getMoneyLabel() {
        return moneyLabel;
    }

    public Label getLevelLabel() {
        return levelLabel;
    }

    public Label[] getAnswerLabels() {
        return answerLabels;
    }

    public Pane[] getAnswerDropPanes() {
        return answerDropPanes;
    }

    public Label[] getAnswerStakeLabels() {
        return answerStakeLabels;
    }

    public Button[] getAllInButtons() {
        return allInButtons;
    }

    public Button getLifeline5050() {
        return lifeline5050;
    }

    public Button getLifelineAudience() {
        return lifelineAudience;
    }

    public Button getLifelinePhone() {
        return lifelinePhone;
    }

    public Button getConfirmButton() {
        return confirmButton;
    }

    public ProgressBar getTimerProgress() {
        return timerProgress;
    }

    public ProgressBar getLevelProgress() {
        return levelProgress;
    }

    public Pane getBanknoteStackPane() {
        return banknoteStackPane;
    }
}
