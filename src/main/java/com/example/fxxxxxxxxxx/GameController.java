package com.example.fxxxxxxxxxx;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameController {
    private Label questionLabel, timerLabel, moneyLabel, levelLabel;
    private Label[] answerLabels = new Label[4];
    private Pane[] answerDropPanes = new Pane[4];
    private Label[] answerStakeLabels = new Label[4];
    private Button[] allInButtons = new Button[4];
    private Button lifeline5050, lifelineAudience, lifelinePhone, confirmButton, surrenderButton;
    private ProgressBar timerProgress, levelProgress;
    private Pane banknoteStackPane;
    private Timeline timer;
    private Canvas particleCanvas;
    private List<Particle> particles = new ArrayList<>();
    private int timeLeft = 120;
    private int currentQuestionIndex = 0;
    private int money = 1000000;
    private boolean lifeline5050Used = false;
    private boolean lifelineAudienceUsed = false;
    private boolean lifelinePhoneUsed = false;
    private Question currentQuestion;
    private QuestionLoader questionLoader;
    private BanknoteManager banknoteManager;
    private UIFactory uiFactory;
    private NicknamePanel nicknamePanel;
    private Leaderboard leaderboard;
    private String nickname;
    private boolean isGameActive = true;
    private StackPane root;
    private static final int BANKNOTE_VALUE = 50000;
    private static final int MINIMUM_BET = 50000;

    public GameController() {
        questionLoader = new QuestionLoader();
        banknoteManager = new BanknoteManager();
        leaderboard = new Leaderboard(this);
        uiFactory = new UIFactory(banknoteManager, this);
        nicknamePanel = new NicknamePanel();
    }

    public void initializeUI(StackPane root) {
        System.out.println("Initializing UI");
        this.root = root;
        showNicknamePanel();
    }

    private void showNicknamePanel() {
        System.out.println("Showing nickname panel");
        root.getChildren().clear();
        VBox nicknamePane = nicknamePanel.getPanel();
        nicknamePanel.getStartButton().setOnAction(e -> {
            String inputNickname = nicknamePanel.getNicknameField().getText().trim();
            if (!inputNickname.isEmpty()) {
                nickname = inputNickname;
                System.out.println("Nickname set: " + nickname);
                startGame();
            } else {
                System.out.println("Nickname empty, showing error");
                nicknamePanel.getNicknameField().setStyle("-fx-border-color: #ff5555; -fx-background-color: #2a2a4a; -fx-text-fill: #ffffff;");
            }
        });
        root.getChildren().add(nicknamePane);
    }

    private void startGame() {
        System.out.println("Starting game");
        root.getChildren().clear();

        particleCanvas = new Canvas();
        particleCanvas.widthProperty().bind(root.widthProperty());
        particleCanvas.heightProperty().bind(root.heightProperty());
        particles.clear();
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle(particleCanvas.getWidth(), particleCanvas.getHeight()));
        }
        AnimationTimer particleTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (particleCanvas.getGraphicsContext2D() == null) return;
                GraphicsContext gc = particleCanvas.getGraphicsContext2D();
                gc.setFill(Color.web("#1a1a2e"));
                gc.fillRect(0, 0, particleCanvas.getWidth(), particleCanvas.getHeight());
                for (Particle p : new ArrayList<>(particles)) {
                    p.update(particleCanvas.getWidth(), particleCanvas.getHeight());
                    p.draw(gc);
                }
            }
        };
        particleTimer.start();

        VBox gameContent = uiFactory.createGameContent(root);
        questionLabel = uiFactory.getQuestionLabel();
        timerLabel = uiFactory.getTimerLabel();
        moneyLabel = uiFactory.getMoneyLabel();
        levelLabel = uiFactory.getLevelLabel();
        answerLabels = uiFactory.getAnswerLabels();
        answerDropPanes = uiFactory.getAnswerDropPanes();
        answerStakeLabels = uiFactory.getAnswerStakeLabels();
        allInButtons = uiFactory.getAllInButtons();
        lifeline5050 = uiFactory.getLifeline5050();
        lifelineAudience = uiFactory.getLifelineAudience();
        lifelinePhone = uiFactory.getLifelinePhone();
        confirmButton = uiFactory.getConfirmButton();
        surrenderButton = uiFactory.getSurrenderButton();
        timerProgress = uiFactory.getTimerProgress();
        levelProgress = uiFactory.getLevelProgress();
        banknoteStackPane = uiFactory.getBanknoteStackPane();

        root.getChildren().addAll(particleCanvas, gameContent);

        particleCanvas.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            for (Particle p : particles) {
                p.adjustForNewWidth(newWidth.doubleValue());
            }
        });
        particleCanvas.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            for (Particle p : particles) {
                p.adjustForNewHeight(newHeight.doubleValue());
            }
        });

        questionLoader.loadQuestions();
        if (timer != null) {
            timer.stop();
        }
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timer.setCycleCount(Timeline.INDEFINITE);

        resetGameState();
        loadNextQuestion();
        timer.play();
    }

    public void handleWindowMaximized() {
        particles.clear();
        for (int i = 0; i < 50; i++) {
            if (particleCanvas != null) {
                particles.add(new Particle(particleCanvas.getWidth(), particleCanvas.getHeight(), true));
            }
        }
    }

    private void updateTimer() {
        if (!isGameActive) return;

        timeLeft--;
        timerLabel.setText(timeLeft + "s");
        double timeForQuestion = (currentQuestionIndex >= 5 ? 90.0 : 120.0);
        timerProgress.setProgress(timeLeft / timeForQuestion);

        if (timeLeft <= 30 && timeLeft > 0 && timeLeft % 2 == 0) {
            FadeTransition ft = new FadeTransition(Duration.millis(200), timerLabel);
            ft.setFromValue(1.0);
            ft.setToValue(0.3);
            ft.setAutoReverse(true);
            ft.setCycleCount(2);
            ft.play();
        } else if (timeLeft <= 0) {
            endGame("Czas minął! Straciłeś wszystko!");
        }
    }

    private void loadNextQuestion() {
        System.out.println("Ładowanie następnego pytania, indeks: " + currentQuestionIndex);
        int currentLevel = currentQuestionIndex + 1;
        levelLabel.setText("Poziom: " + currentLevel);
        levelProgress.setProgress(currentLevel / 10.0);

        questionLabel.setStyle("-fx-font-size: 22pt; -fx-font-weight: bold; -fx-text-fill: #00eaff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 8, 0, 2, 2);");

        if (!questionLoader.hasQuestions(currentLevel)) {
            triggerConfetti();
            endGame("Gratulacje! Wygrałeś " + formatCurrencyDisplay(money) + " zł przechodząc wszystkie poziomy!");
            return;
        }

        currentQuestion = questionLoader.getRandomQuestion(currentLevel);
        if (currentQuestion == null) {
            endGame("Błąd: Nie udało się załadować pytania dla poziomu " + currentLevel + ". Koniec gry.");
            return;
        }
        questionLabel.setText(currentQuestion.getText());

        TranslateTransition tt = new TranslateTransition(Duration.millis(800), questionLabel);
        tt.setFromY(-50);
        tt.setToY(0);
        questionLabel.setTranslateY(-50);
        tt.play();

        List<Integer> indices = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(indices);
        int newCorrectIndex = indices.indexOf(currentQuestion.getCorrectAnswerIndex());

        for (int i = 0; i < 4; i++) {
            int originalIndex = indices.get(i);
            answerLabels[i].setText(currentQuestion.getAnswers()[originalIndex]);
            answerLabels[i].setStyle("-fx-font-size: 16pt; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-background-color: #2a2a4a; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 2, 2);");
            answerLabels[i].setVisible(true);
            answerLabels[i].setOpacity(1.0);
            answerLabels[i].setEffect(null);
            answerDropPanes[i].setVisible(true);
            answerDropPanes[i].getChildren().clear();
            answerStakeLabels[i].setText("0 zł");

            ScaleTransition st = new ScaleTransition(Duration.millis(500), answerLabels[i]);
            st.setFromX(0.1); st.setFromY(0.1);
            st.setToX(1.0); st.setToY(1.0);
            answerLabels[i].setScaleX(0.1); answerLabels[i].setScaleY(0.1);
            st.play();
        }

        currentQuestion.setCorrectAnswerIndex(newCorrectIndex);

        if (banknoteManager != null && banknoteStackPane != null) {
            banknoteManager.initializeBanknotes(money, banknoteStackPane);
        }

        timeLeft = currentQuestionIndex >= 5 ? 90 : 120;
        timerLabel.setText(timeLeft + "s");
        timerProgress.setProgress(1.0);
        if (banknoteManager != null) {
            banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);
        }

        setupAllInButtons();
    }

    private void setupAllInButtons() {
        for (int i = 0; i < allInButtons.length; i++) {
            final int index = i;
            allInButtons[i].setOnAction(e -> {
                if (!isGameActive || banknoteManager == null || banknoteStackPane == null || answerDropPanes[index] == null) return;

                System.out.println("All In button pressed for answer index: " + index);
                for (Pane dropPane : answerDropPanes) {
                    if (dropPane != null && dropPane != answerDropPanes[index]) {
                        dropPane.getChildren().clear();
                    }
                }

                banknoteStackPane.getChildren().clear();
                int numBanknotes = money / BANKNOTE_VALUE;
                for (int j = 0; j < numBanknotes && j < banknoteManager.getBanknotes().size(); j++) {
                    Label banknote = banknoteManager.getBanknotes().get(j);
                    banknoteManager.placeBanknoteInPane(banknote, answerDropPanes[index]);
                    banknoteManager.getBanknoteLocations().put(banknote, answerDropPanes[index]);

                    ScaleTransition st = new ScaleTransition(Duration.millis(200), banknote);
                    st.setFromX(1.0); st.setFromY(1.0);
                    st.setToX(1.2); st.setToY(1.2);
                    st.setAutoReverse(true);
                    st.setCycleCount(2);
                    st.play();
                }

                banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);
            });
        }
    }

    public void confirmAnswer() {
        if (!isGameActive || currentQuestion == null) return;

        int totalStake = 0;
        int correctStake = 0;

        for (int i = 0; i < 4; i++) {
            int stakeOnAnswer = 0;
            if (answerDropPanes[i] != null && banknoteManager != null && banknoteManager.getBanknoteLocations() != null) {
                stakeOnAnswer = (int) answerDropPanes[i].getChildren().stream()
                        .filter(node -> node instanceof Label && banknoteManager.getBanknoteLocations().containsKey(node))
                        .count() * BANKNOTE_VALUE;
            }
            totalStake += stakeOnAnswer;
            if (i == currentQuestion.getCorrectAnswerIndex()) {
                correctStake = stakeOnAnswer;
            }
        }

        if (totalStake < MINIMUM_BET) {
            showError("Musisz postawić co najmniej " + formatCurrencyDisplay(MINIMUM_BET) + " zł!");
            return;
        }

        if (currentQuestion.getCorrectAnswerIndex() >= 0 && currentQuestion.getCorrectAnswerIndex() < answerLabels.length) {
            answerLabels[currentQuestion.getCorrectAnswerIndex()].setStyle("-fx-font-size: 16pt; -fx-font-weight: bold; -fx-text-fill: #1a1a2e; -fx-background-color: #00ff88; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,255,0,0.7), 15, 0, 0, 0);");
            ScaleTransition st = new ScaleTransition(Duration.millis(600), answerLabels[currentQuestion.getCorrectAnswerIndex()]);
            st.setFromX(1.0); st.setFromY(1.0);
            st.setToX(1.15); st.setToY(1.15);
            st.setAutoReverse(true);
            st.setCycleCount(3);
            st.play();
        }

        double multiplier = currentQuestionIndex >= 5 ? 2.1 : 2.0;
        int winnings = (int) (correctStake * multiplier);
        int penalty = (correctStake == 0 && money > 0) ? money / 2 : 0;
        money = winnings - penalty;

        Timeline delayTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            if (money <= 0) {
                endGame("Niestety, straciłeś wszystko. Koniec gry.");
            } else {
                currentQuestionIndex++;
                if (currentQuestionIndex >= 10) {
                    triggerConfetti();
                    endGame("Gratulacje! Wygrałeś " + formatCurrencyDisplay(money) + " zł!");
                } else {
                    loadNextQuestion();
                }
            }
        }));
        delayTimeline.play();
    }

    public void surrender() {
        if (!isGameActive) return;
        endGame("Poddałeś się! Wygrywasz " + formatCurrencyDisplay(money) + " zł!");
    }

    public void use5050() {
        if (lifeline5050Used || !isGameActive || currentQuestion == null) return;
        lifeline5050Used = true;
        lifeline5050.setDisable(true);
        lifeline5050.setStyle(uiFactory.getButtonDisabledStyle());

        int correctIndex = currentQuestion.getCorrectAnswerIndex();
        List<Integer> indicesToRemove = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (i != correctIndex) {
                indicesToRemove.add(i);
            }
        }
        Collections.shuffle(indicesToRemove);

        int removedCount = 0;
        for (int i = 0; i < indicesToRemove.size() && removedCount < 2; i++) {
            int indexToHide = indicesToRemove.get(i);
            if (answerLabels[indexToHide].isVisible()) {
                final int finalIndex = indexToHide;
                FadeTransition ft = new FadeTransition(Duration.millis(500), answerLabels[finalIndex]);
                ft.setToValue(0);
                ft.setOnFinished(e -> {
                    answerLabels[finalIndex].setVisible(false);
                    if (answerDropPanes[finalIndex] != null) answerDropPanes[finalIndex].setVisible(false);
                });
                ft.play();
                removedCount++;
            }
        }
    }

    public void useAudience() {
        if (lifelineAudienceUsed || !isGameActive || currentQuestion == null) return;
        lifelineAudienceUsed = true;
        lifelineAudience.setDisable(true);
        lifelineAudience.setStyle(uiFactory.getButtonDisabledStyle());

        String[] answers = currentQuestion.getAnswers();
        int correctIdx = currentQuestion.getCorrectAnswerIndex();
        Random random = new Random();
        int[] percentages = new int[4];
        int sum = 0;

        percentages[correctIdx] = 40 + random.nextInt(31);
        sum += percentages[correctIdx];

        List<Integer> otherIndices = new ArrayList<>();
        for (int i = 0; i < 4; i++) if (i != correctIdx) otherIndices.add(i);

        for (int i = 0; i < otherIndices.size(); i++) {
            int idx = otherIndices.get(i);
            if (i == otherIndices.size() - 1) {
                percentages[idx] = 100 - sum;
            } else {
                percentages[idx] = random.nextInt(Math.min(20, 100 - sum - (otherIndices.size() - 1 - i) * 5));
                if (percentages[idx] < 0) percentages[idx] = 0;
            }
            sum += percentages[idx];
        }
        if (sum > 100) {
            int diff = sum - 100;
            percentages[correctIdx] -= diff;
            if (percentages[correctIdx] < 0) percentages[correctIdx] = 0;
        } else if (sum < 100 && otherIndices.size() > 0) {
            percentages[otherIndices.get(random.nextInt(otherIndices.size()))] += (100 - sum);
        }

        StringBuilder result = new StringBuilder("Głosy publiczności:\n");
        for (int i = 0; i < 4; i++) {
            result.append(answers[i]).append(": ").append(percentages[i]).append("%\n");
        }
        showInfo(result.toString());
    }

    public void usePhone() {
        if (lifelinePhoneUsed || !isGameActive || currentQuestion == null) return;
        lifelinePhoneUsed = true;
        lifelinePhone.setDisable(true);
        lifelinePhone.setStyle(uiFactory.getButtonDisabledStyle());

        int correct = currentQuestion.getCorrectAnswerIndex();
        String suggestion = currentQuestion.getAnswers()[correct];
        Random rand = new Random();
        if (rand.nextInt(100) < 25) {
            List<Integer> wrongAnswersIndices = new ArrayList<>();
            for (int i = 0; i < 4; i++) if (i != correct) wrongAnswersIndices.add(i);
            Collections.shuffle(wrongAnswersIndices);
            if (!wrongAnswersIndices.isEmpty()) {
                suggestion = currentQuestion.getAnswers()[wrongAnswersIndices.get(0)];
            }
        }
        showInfo("Dziekan (telefon do przyjaciela):\nMyślę, że poprawna odpowiedź to: " + suggestion);
    }

    private void endGame(String message) {
        System.out.println("endGame wywołane z wiadomością: " + message + ", nickname=" + nickname + ", money=" + money + ", osiągnięty poziom=" + (currentQuestionIndex + 1));
        isGameActive = false;
        if (timer != null) {
            timer.stop();
        }
        disableUI();

        if (nickname != null && !nickname.trim().isEmpty()) {
            leaderboard.saveScore(nickname, money, currentQuestionIndex + 1);
        } else {
            System.err.println("Pseudonim jest pusty lub null, nie można zapisać wyniku.");
        }

        Alert finalAlert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        finalAlert.setTitle("Koniec Gry");
        finalAlert.setHeaderText(null);
        finalAlert.getDialogPane().setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #00eaff; -fx-border-width: 2; -fx-border-radius: 10; -fx-padding: 15;");
        finalAlert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14pt;");
        ButtonBar buttonBar = (ButtonBar) finalAlert.getDialogPane().lookup(".button-bar");
        buttonBar.getButtons().forEach(btn -> btn.setStyle("-fx-background-color: #00eaff; -fx-text-fill: #1a1a2e; -fx-background-radius: 8; -fx-font-size: 14pt;"));
        finalAlert.showAndWait();

        showLeaderboard();
    }

    private void disableUI() {
        if (confirmButton != null) confirmButton.setDisable(true);
        if (surrenderButton != null) surrenderButton.setDisable(true);
        if (lifeline5050 != null) lifeline5050.setDisable(true);
        if (lifelineAudience != null) lifelineAudience.setDisable(true);
        if (lifelinePhone != null) lifelinePhone.setDisable(true);

        if (allInButtons != null) {
            for (Button allInButton : allInButtons) {
                if (allInButton != null) allInButton.setDisable(true);
            }
        }
        if (answerDropPanes != null) {
            for (Pane dropPane : answerDropPanes) {
                if (dropPane != null) {
                    dropPane.setOnDragOver(null);
                    dropPane.setOnDragDropped(null);
                }
            }
        }
        if (banknoteStackPane != null) {
            banknoteStackPane.setOnDragOver(null);
            banknoteStackPane.setOnDragDropped(null);
        }
    }

    private void showLeaderboard() {
        System.out.println("Pokazywanie tabeli wyników");
        root.getChildren().clear();
        if (particleCanvas != null) {
            root.getChildren().add(particleCanvas);
        }

        VBox leaderboardPane = leaderboard.createLeaderboardPanel();
        leaderboardPane.getChildren().stream()
                .filter(node -> node instanceof Button && "Zamknij".equals(((Button) node).getText()))
                .map(node -> (Button) node)
                .findFirst()
                .ifPresent(closeButton -> {
                    closeButton.setOnAction(e -> {
                        closeLeaderboard();
                    });
                });

        root.getChildren().add(leaderboardPane);
    }

    private void resetGameState() {
        System.out.println("Resetowanie stanu gry");
        timeLeft = 120;
        currentQuestionIndex = 0;
        money = 1000000;
        lifeline5050Used = false;
        lifelineAudienceUsed = false;
        lifelinePhoneUsed = false;
        isGameActive = true;

        if (confirmButton != null) confirmButton.setDisable(false);
        if (surrenderButton != null) surrenderButton.setDisable(false);

        if (lifeline5050 != null && uiFactory != null) {
            lifeline5050.setDisable(false);
            lifeline5050.setStyle(uiFactory.getButtonBaseStyle());
        }
        if (lifelineAudience != null && uiFactory != null) {
            lifelineAudience.setDisable(false);
            lifelineAudience.setStyle(uiFactory.getButtonBaseStyle());
        }
        if (lifelinePhone != null && uiFactory != null) {
            lifelinePhone.setDisable(false);
            lifelinePhone.setStyle(uiFactory.getButtonBaseStyle());
        }

        if (allInButtons != null) {
            for (Button allInButton : allInButtons) {
                if (allInButton != null) allInButton.setDisable(false);
            }
        }

        if (uiFactory != null && banknoteManager != null && answerDropPanes != null) {
            for (int i = 0; i < answerDropPanes.length; i++) {
                if (answerDropPanes[i] != null) {
                    final Pane targetDropPane = answerDropPanes[i];
                    targetDropPane.setOnDragOver(event -> {
                        if (event.getGestureSource() != targetDropPane && event.getDragboard().hasString()) {
                            event.acceptTransferModes(TransferMode.MOVE);
                        }
                        event.consume();
                    });

                    int finalI = i;
                    targetDropPane.setOnDragDropped(event -> {
                        Dragboard db = event.getDragboard();
                        boolean success = false;
                        if (db.hasString()) {
                            try {
                                int banknoteIndex = Integer.parseInt(db.getString());
                                if (banknoteManager.getBanknotes() != null && banknoteIndex >= 0 && banknoteIndex < banknoteManager.getBanknotes().size()) {
                                    Label banknoteToMove = banknoteManager.getBanknotes().get(banknoteIndex);
                                    Pane currentParent = banknoteManager.getBanknoteLocations().get(banknoteToMove);

                                    if (currentParent != targetDropPane) {
                                        if (currentParent != null) {
                                            currentParent.getChildren().remove(banknoteToMove);
                                        }
                                        banknoteManager.placeBanknoteInPane(banknoteToMove, targetDropPane);
                                        banknoteManager.getBanknoteLocations().put(banknoteToMove, targetDropPane);
                                        banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);

                                        ScaleTransition st = new ScaleTransition(Duration.millis(200), banknoteToMove);
                                        st.setFromX(1.0); st.setFromY(1.0);
                                        st.setToX(1.2); st.setToY(1.2);
                                        st.setAutoReverse(true);
                                        st.setCycleCount(2);
                                        st.play();

                                        success = true;
                                        System.out.println("Banknote " + banknoteIndex + " moved to answer pane " + finalI);
                                    }
                                } else {
                                    System.err.println("Invalid banknote index from dragboard: " + db.getString());
                                }
                            } catch (NumberFormatException ex) {
                                System.err.println("Error parsing banknote ID from dragboard: " + db.getString());
                            }
                        }
                        event.setDropCompleted(success);
                        event.consume();
                    });
                }
            }
        }

        if (banknoteStackPane != null && banknoteManager != null) {
            banknoteStackPane.setOnDragOver(event -> {
                if (event.getGestureSource() != banknoteStackPane && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            banknoteStackPane.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    try {
                        int banknoteIndex = Integer.parseInt(db.getString());
                        if (banknoteManager.getBanknotes() != null && banknoteIndex >= 0 && banknoteIndex < banknoteManager.getBanknotes().size()) {
                            Label banknoteToMove = banknoteManager.getBanknotes().get(banknoteIndex);
                            Pane currentParent = banknoteManager.getBanknoteLocations().get(banknoteToMove);

                            if (currentParent != banknoteStackPane) {
                                if (currentParent != null) {
                                    currentParent.getChildren().remove(banknoteToMove);
                                }
                                banknoteManager.placeBanknoteInPane(banknoteToMove, banknoteStackPane);
                                banknoteManager.getBanknoteLocations().put(banknoteToMove, banknoteStackPane);
                                banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);

                                ScaleTransition st = new ScaleTransition(Duration.millis(200), banknoteToMove);
                                st.setFromX(1.0); st.setFromY(1.0);
                                st.setToX(1.2); st.setToY(1.2);
                                st.setAutoReverse(true);
                                st.setCycleCount(2);
                                st.play();

                                success = true;
                                System.out.println("Banknote " + banknoteIndex + " moved to banknote stack");
                            }
                        } else {
                            System.err.println("Invalid banknote index from dragboard: " + db.getString());
                        }
                    } catch (NumberFormatException ex) {
                        System.err.println("Error parsing banknote ID from dragboard: " + db.getString());
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });
        }

        if (banknoteManager != null && banknoteStackPane != null) {
            banknoteManager.initializeBanknotes(money, banknoteStackPane);
            banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);
        }
    }

    private void triggerConfetti() {
        if (particleCanvas == null) return;
        for (int i = 0; i < 100; i++) {
            particles.add(new Particle(particleCanvas.getWidth(), particleCanvas.getHeight(), true));
        }
        Timeline confettiTimer = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            particles.removeIf(p -> p.isConfetti());
        }));
        confettiTimer.setCycleCount(1);
        confettiTimer.play();
    }

    private String formatCurrencyDisplay(int amount) {
        return String.format("%,d", amount).replace(",", " ");
    }

    public void showError(String message) {
        System.out.println("Pokazywanie dialogu błędu: " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.getDialogPane().setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: #ffffff; -fx-border-color: #ff5555; -fx-border-width: 2; -fx-border-radius: 10; -fx-padding: 15;");
        alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14pt;");
        ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().lookup(".button-bar");
        buttonBar.getButtons().forEach(btn -> btn.setStyle("-fx-background-color: #ff5555; -fx-text-fill: #1a1a2e; -fx-background-radius: 8; -fx-font-size: 14pt;"));
        alert.showAndWait();
    }

    private void showInfo(String message) {
        System.out.println("Pokazywanie dialogu informacji: " + message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Informacja");
        alert.setHeaderText(null);
        alert.getDialogPane().setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: #ffffff; -fx-border-color: #00eaff; -fx-border-width: 2; -fx-border-radius: 10; -fx-padding: 15;");
        alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14pt;");
        ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().lookup(".button-bar");
        buttonBar.getButtons().forEach(btn -> btn.setStyle("-fx-background-color: #00eaff; -fx-text-fill: #1a1a2e; -fx-background-radius: 8; -fx-font-size: 14pt;"));
        alert.showAndWait();
    }

    public void closeLeaderboard() {
        System.out.println("Closing leaderboard");
        root.getChildren().clear();
        if (particleCanvas != null) {
            root.getChildren().add(particleCanvas);
        }
        showNicknamePanel();
    }
}