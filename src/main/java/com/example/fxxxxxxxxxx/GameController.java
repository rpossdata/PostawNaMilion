package com.example.fxxxxxxxxxx;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Map;

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
    private AnimationTimer particleTimer;
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
    private VBox gameContent;
    private static final int BANKNOTE_VALUE = 50000;
    private static final int MINIMUM_BET = 50000;
    private static final int TOTAL_LEVELS = 10;

    public GameController() {
        questionLoader = new QuestionLoader();
        banknoteManager = new BanknoteManager();
        leaderboard = new Leaderboard(this);
        uiFactory = new UIFactory(banknoteManager, this);
        nicknamePanel = new NicknamePanel();
    }

    public void initializeUI(StackPane root) {
        System.out.println("Inicjalizacja UI");
        this.root = root;
        showStartScreen();
    }

    private void showStartScreen() {
        System.out.println("Wyświetlanie ekranu startowego");
        root.getChildren().clear();
        StartScreen startScreen = new StartScreen(this);
        root.getChildren().add(startScreen.getPanel());
    }

    public void showNicknamePanel() {
        System.out.println("Wyświetlanie panelu pseudonimu");
        isGameActive = false;
        if (timer != null) {
            timer.stop();
        }
        if (particleTimer != null) {
            particleTimer.stop();
        }
        if (particleCanvas != null) {
            particleCanvas = null;
        }
        root.getChildren().clear();
        VBox nicknamePane = nicknamePanel.getPanel();
        nicknamePanel.getStartButton().setOnAction(e -> {
            String inputNickname = nicknamePanel.getNicknameField().getText().trim();
            if (!inputNickname.isEmpty()) {
                nickname = inputNickname;
                System.out.println("Pseudonim ustawiony: " + nickname);
                startGame();
            } else {
                System.out.println("Pseudonim jest pusty, wyświetlanie błędu walidacji");
                nicknamePanel.getNicknameField().setStyle("-fx-border-color: #ff5555; -fx-background-color: #2a2a4a; -fx-text-fill: #ffffff;");
            }
        });
        root.getChildren().add(nicknamePane);
    }

    public void startGame() {
        System.out.println("Rozpoczynanie gry...");
        root.getChildren().clear();
        isGameActive = true;

        particleCanvas = new Canvas();
        particleCanvas.widthProperty().bind(root.widthProperty());
        particleCanvas.heightProperty().bind(root.heightProperty());
        particles.clear();
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle(particleCanvas.getWidth(), particleCanvas.getHeight()));
        }

        if (particleTimer != null) {
            particleTimer.stop();
        }
        particleTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (particleCanvas == null || particleCanvas.getGraphicsContext2D() == null) return;
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

        gameContent = uiFactory.createGameContent(root);
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

        if (confirmButton != null) confirmButton.setOnAction(e -> confirmAnswer());
        if (surrenderButton != null) surrenderButton.setOnAction(e -> handleSurrender());
        if (lifeline5050 != null) lifeline5050.setOnAction(e -> useLifeline5050());
        if (lifelineAudience != null) lifelineAudience.setOnAction(e -> useLifelineAudience());
        if (lifelinePhone != null) lifelinePhone.setOnAction(e -> useLifelinePhone());

        root.getChildren().addAll(particleCanvas, gameContent);

        particleCanvas.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            if (particles != null) {
                for (Particle p : particles) p.adjustForNewWidth(newWidth.doubleValue());
            }
        });
        particleCanvas.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            if (particles != null) {
                for (Particle p : particles) p.adjustForNewHeight(newHeight.doubleValue());
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
        System.out.println("Gra rozpoczęta.");
    }

    private void resetGameState() {
        System.out.println("Resetowanie stanu gry.");
        money = 1000000;
        currentQuestionIndex = 0;
        lifeline5050Used = false;
        lifelineAudienceUsed = false;
        lifelinePhoneUsed = false;
        isGameActive = true;

        timeLeft = (currentQuestionIndex >= 5 ? 90 : 120);
        if (timerLabel != null) timerLabel.setText(timeLeft + "s");
        if (timerProgress != null) timerProgress.setProgress(1.0);
        if (moneyLabel != null) moneyLabel.setText(formatCurrencyDisplay(money));
        if (levelLabel != null) levelLabel.setText("Poziom: " + (currentQuestionIndex + 1));
        if (levelProgress != null) levelProgress.setProgress((currentQuestionIndex + 1.0) / TOTAL_LEVELS);

        if (lifeline5050 != null) { lifeline5050.setDisable(lifeline5050Used); lifeline5050.setOpacity(lifeline5050Used ? 0.5 : 1.0); }
        if (lifelineAudience != null) { lifelineAudience.setDisable(lifelineAudienceUsed); lifelineAudience.setOpacity(lifelineAudienceUsed ? 0.5 : 1.0); }
        if (lifelinePhone != null) { lifelinePhone.setDisable(lifelinePhoneUsed); lifelinePhone.setOpacity(lifelinePhoneUsed ? 0.5 : 1.0); }
        if (confirmButton != null) confirmButton.setDisable(false);

        if (banknoteStackPane != null) banknoteStackPane.getChildren().clear();
        if (answerDropPanes != null) {
            for (Pane pane : answerDropPanes) if (pane != null) pane.getChildren().clear();
        }
        if (answerStakeLabels != null) {
            for (Label stakeLabel : answerStakeLabels) if (stakeLabel != null) stakeLabel.setText("0 zł");
        }
        System.out.println("Stan gry zresetowany. Pieniądze: " + money + ", Ukończone poziomy: " + currentQuestionIndex);
    }

    public void handleWindowMaximized() {
        if (particleCanvas == null || particles == null) return;
        particles.clear();
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle(particleCanvas.getWidth(), particleCanvas.getHeight(), true));
        }
    }

    private void updateTimer() {
        if (!isGameActive) return;

        timeLeft--;
        if (timerLabel != null) timerLabel.setText(timeLeft + "s");
        double timeForQuestion = (currentQuestionIndex >= 5 ? 90.0 : 120.0);
        if (timerProgress != null) timerProgress.setProgress(timeLeft / timeForQuestion);

        if (timeLeft <= 30 && timeLeft > 0 && timeLeft % 2 == 0) {
            if (timerLabel != null) {
                FadeTransition ft = new FadeTransition(Duration.millis(200), timerLabel);
                ft.setFromValue(1.0); ft.setToValue(0.3);
                ft.setAutoReverse(true); ft.setCycleCount(2);
                ft.play();
            }
        } else if (timeLeft <= 0) {
            endGame("Czas minął! Straciłeś wszystkie pieniądze.");
        }
    }

    private void loadNextQuestion() {
        if (!isGameActive) return;

        if (currentQuestionIndex >= TOTAL_LEVELS) {
            triggerConfetti();
            endGame("Gratulacje! Wygrałeś " + formatCurrencyDisplay(money) + " zł przechodząc wszystkie poziomy!");
            return;
        }

        int levelToLoad = currentQuestionIndex + 1;
        System.out.println("Ładowanie pytania dla poziomu: " + levelToLoad + " (ukończono: " + currentQuestionIndex + ")");

        if (levelLabel != null) levelLabel.setText("Poziom: " + levelToLoad);
        if (levelProgress != null) levelProgress.setProgress(levelToLoad / (double)TOTAL_LEVELS);
        if (questionLabel != null) questionLabel.setStyle("-fx-font-size: 22pt; -fx-font-weight: bold; -fx-text-fill: #00eaff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 8, 0, 2, 2);");

        currentQuestion = questionLoader.getRandomQuestion(levelToLoad);
        if (currentQuestion == null) {
            endGame("Błąd: Nie udało się załadować pytania dla poziomu " + levelToLoad + ". Koniec gry.");
            return;
        }
        if (currentQuestion.getAnswers() == null || currentQuestion.getCorrectAnswerIndex() < 0 || currentQuestion.getCorrectAnswerIndex() >= currentQuestion.getAnswers().length) {
            endGame("Błąd: Pytanie dla poziomu " + levelToLoad + " ma nieprawidłowe dane odpowiedzi. Koniec gry.");
            return;
        }

        if (questionLabel != null) questionLabel.setText(currentQuestion.getText());

        if (questionLabel != null) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(800), questionLabel);
            tt.setFromY(-50); tt.setToY(0);
            questionLabel.setTranslateY(-50);
            tt.play();
        }

        List<Integer> indices = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(indices);
        int shuffledCorrectAnswerIndex = -1;
        String[] originalAnswers = currentQuestion.getAnswers();
        String correctAnswerText = originalAnswers[currentQuestion.getCorrectAnswerIndex()];

        for (int i = 0; i < 4; i++) {
            if (answerLabels[i] == null) continue;
            int originalIndex = indices.get(i);
            if (originalIndex < 0 || originalIndex >= originalAnswers.length) {
                System.err.println("Błąd: originalIndex poza zakresem podczas tasowania odpowiedzi.");
                answerLabels[i].setText("Błąd Odp.");
                continue;
            }
            answerLabels[i].setText(originalAnswers[originalIndex]);
            if (originalAnswers[originalIndex].equals(correctAnswerText)) {
                shuffledCorrectAnswerIndex = i;
            }

            answerLabels[i].setStyle("-fx-font-size: 16pt; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-background-color: #2a2a4a; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 2, 2);");
            answerLabels[i].setVisible(true);
            answerLabels[i].setOpacity(1.0);
            answerLabels[i].setEffect(null);
            if (answerDropPanes[i] != null) {
                answerDropPanes[i].setVisible(true);
                answerDropPanes[i].getChildren().clear();
            }
            if (answerStakeLabels[i] != null) {
                answerStakeLabels[i].setText("0 zł");
            }

            ScaleTransition st = new ScaleTransition(Duration.millis(500), answerLabels[i]);
            st.setFromX(0.1); st.setFromY(0.1);
            st.setToX(1.0); st.setToY(1.0);
            answerLabels[i].setScaleX(0.1); answerLabels[i].setScaleY(0.1);
            st.play();
        }
        currentQuestion.setCorrectAnswerIndex(shuffledCorrectAnswerIndex);

        if (banknoteManager != null && banknoteStackPane != null) {
            banknoteManager.initializeBanknotes(money, banknoteStackPane);
        }

        timeLeft = currentQuestionIndex >= 5 ? 90 : 120;
        if (timerLabel != null) timerLabel.setText(timeLeft + "s");
        if (timerProgress != null) timerProgress.setProgress(1.0);
        if (banknoteManager != null) {
            banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);
        }
        if (confirmButton != null) confirmButton.setDisable(false);

        setupAllInButtons();
        System.out.println("Pytanie załadowane. Poprawna odpowiedź (po potasowaniu) ma indeks: " + currentQuestion.getCorrectAnswerIndex());
    }

    private void setupAllInButtons() {
        if (allInButtons == null || banknoteManager == null) return;
        for (int i = 0; i < allInButtons.length; i++) {
            final int index = i;
            if (allInButtons[i] != null) {
                allInButtons[i].setOnAction(e -> {
                    if (!isGameActive || banknoteStackPane == null || answerDropPanes == null || index >= answerDropPanes.length || answerDropPanes[index] == null) return;
                    System.out.println("Przycisk 'All In' wciśnięty dla odpowiedzi o indeksie: " + index);

                    for (int j = 0; j < answerDropPanes.length; j++) {
                        if (j != index && answerDropPanes[j] != null) {
                            banknoteManager.moveBanknotesFromPaneToStack(answerDropPanes[j], banknoteStackPane);
                        }
                    }
                    banknoteManager.moveAllBanknotesToPane(answerDropPanes[index], banknoteStackPane);
                    banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);
                });
            }
        }
    }

    public void confirmAnswer() {
        if (!isGameActive || currentQuestion == null || (confirmButton != null && confirmButton.isDisabled())) return;
        if (banknoteManager == null || banknoteManager.getBanknoteLocations() == null) {
            showError("Błąd wewnętrzny: Problem z systemem banknotów.");
            if (confirmButton != null) confirmButton.setDisable(false);
            return;
        }

        if (confirmButton != null) confirmButton.setDisable(true);

        int totalStake = 0;
        int correctStake = 0;

        if (answerDropPanes != null) {
            for (int i = 0; i < 4; i++) {
                if (answerDropPanes[i] == null) continue;
                int stakeInPane = 0;
                for(Map.Entry<Label, Pane> entry : banknoteManager.getBanknoteLocations().entrySet()){
                    if(entry.getValue() == answerDropPanes[i]){
                        stakeInPane += BANKNOTE_VALUE;
                    }
                }
                totalStake += stakeInPane;
                if (currentQuestion.getCorrectAnswerIndex() >= 0 && currentQuestion.getCorrectAnswerIndex() < 4 && i == currentQuestion.getCorrectAnswerIndex()) {
                    correctStake = stakeInPane;
                }
            }
        }
        System.out.println("Potwierdzono odpowiedź. Całkowita stawka: " + totalStake + ", Stawka na poprawną: " + correctStake);

        if (totalStake < MINIMUM_BET && money > 0) {
            showError("Musisz postawić co najmniej " + formatCurrencyDisplay(MINIMUM_BET) + " zł na odpowiedzi.");
            if (confirmButton != null) confirmButton.setDisable(false);
            return;
        }

        final String greenStyle = "-fx-font-size: 18pt; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-background-color: #4CAF50; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(76,175,80,0.7), 10, 0, 0, 3);";
        final String redStyle = "-fx-font-size: 18pt; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-background-color: #F44336; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(244,67,54,0.7), 10, 0, 0, 3);";
        int correctAnswerVisualIndex = currentQuestion.getCorrectAnswerIndex();

        if (correctStake > 0) {
            System.out.println("Poprawna odpowiedź!");
            money = correctStake;
            if (moneyLabel != null) moneyLabel.setText(formatCurrencyDisplay(money));
            if (banknoteManager != null) banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);

            if (answerLabels != null && correctAnswerVisualIndex >= 0 && correctAnswerVisualIndex < answerLabels.length && answerLabels[correctAnswerVisualIndex] != null) {
                answerLabels[correctAnswerVisualIndex].setStyle(greenStyle);
            }

            Timeline delay = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
                currentQuestionIndex++;
                loadNextQuestion();
            }));
            delay.play();
        } else {
            System.out.println("Błędna odpowiedź lub brak stawki na poprawną.");
            money = 0;
            if (moneyLabel != null) moneyLabel.setText(formatCurrencyDisplay(money));
            if (banknoteManager != null) {
                if (banknoteStackPane != null) banknoteManager.initializeBanknotes(0, banknoteStackPane);
                banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);
            }

            if (answerLabels != null && correctAnswerVisualIndex >=0 && correctAnswerVisualIndex < answerLabels.length && answerLabels[correctAnswerVisualIndex] != null) {
                answerLabels[correctAnswerVisualIndex].setStyle(greenStyle);
            }
            if (answerDropPanes != null && banknoteManager != null && banknoteManager.getBanknoteLocations() != null) {
                for (int i = 0; i < 4; i++) {
                    if (answerDropPanes[i] == null) continue;
                    boolean stakeOnThisWrongAnswer = false;
                    for(Map.Entry<Label, Pane> entry : banknoteManager.getBanknoteLocations().entrySet()){
                        if(entry.getValue() == answerDropPanes[i]){
                            stakeOnThisWrongAnswer = true;
                            break;
                        }
                    }
                    if (stakeOnThisWrongAnswer && i != correctAnswerVisualIndex) {
                        if (answerLabels != null && i < answerLabels.length && answerLabels[i] != null) answerLabels[i].setStyle(redStyle);
                    }
                }
            }
            Timeline delay = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
                endGame("Zła odpowiedź! Straciłeś wszystkie pieniądze.");
            }));
            delay.play();
        }
    }

    void handleSurrender() {
        if (!isGameActive) return;
        System.out.println("Gracz się poddał.");
        int moneyKept = 0;
        if (banknoteManager != null && banknoteManager.getBanknoteLocations() != null && banknoteStackPane != null) {
            for (Map.Entry<Label, Pane> entry : banknoteManager.getBanknoteLocations().entrySet()) {
                if (entry.getValue() == banknoteStackPane) {
                    moneyKept += BANKNOTE_VALUE;
                }
            }
        }
        money = moneyKept;
        endGame("Poddałeś się. Twój wynik to " + formatCurrencyDisplay(money) + " zł.");
    }

    private void endGame(String message) {
        boolean alreadyOnLeaderboardScreen = false;
        if (root != null && !root.getChildren().isEmpty()) {
            Node firstChild = root.getChildren().get(root.getChildren().size() -1);
            if (firstChild instanceof VBox) {
                VBox currentRootVBox = (VBox) firstChild;
                if (!currentRootVBox.getChildren().isEmpty() && currentRootVBox.getChildren().get(0) instanceof Label) {
                    if (((Label) currentRootVBox.getChildren().get(0)).getText().equals("Ranking Graczy")) {
                        alreadyOnLeaderboardScreen = true;
                    }
                }
            }
        }

        if (alreadyOnLeaderboardScreen && !message.toLowerCase().contains("gratulacje")) {
            System.out.println("endGame wywołane ponownie, gdy ranking (nie-wygrana) jest już wyświetlany. Pomijanie.");
            return;
        }
        if (!isGameActive && message.toLowerCase().contains("gratulacje") && alreadyOnLeaderboardScreen){
            System.out.println("endGame (wygrana) wywołane ponownie, gdy ranking jest już wyświetlany. Pomijanie.");
            return;
        }

        isGameActive = false;
        if (timer != null) {
            timer.stop();
        }

        System.out.println("Gra zakończona: " + message);
        System.out.println("Zapisywanie wyniku: Pseudonim: " + nickname + ", Pieniądze: " + money + ", Ukończone poziomy: " + currentQuestionIndex);

        if (leaderboard != null && nickname != null && !nickname.trim().isEmpty()) {
            leaderboard.saveScore(nickname, money, currentQuestionIndex);
        } else {
            System.err.println("Nie można zapisać wyniku: tablica wyników lub pseudonim jest null/pusty. Pseudonim: " + nickname);
        }

        root.getChildren().clear();

        if (particleCanvas != null) {
            root.getChildren().add(particleCanvas);
        }

        if (leaderboard != null) {
            VBox leaderboardPane = leaderboard.createLeaderboardPanel();
            if (leaderboardPane != null) {
                root.getChildren().add(leaderboardPane);
                System.out.println("Panel rankingu dodany do sceny.");
            } else {
                System.err.println("KRYTYCZNY BŁĄD: Panel rankingu był null. Nie można wyświetlić rankingu.");
                Label errorLabel = new Label("Nie można załadować tablicy wyników.\nSprawdź konsolę po więcej informacji.");
                errorLabel.setStyle("-fx-font-size: 18pt; -fx-text-fill: red; -fx-text-alignment: center;");
                StackPane.setAlignment(errorLabel, Pos.CENTER);
                root.getChildren().add(errorLabel);
            }
        } else {
            Label gameOverLabel = new Label("Koniec Gry!\n" + message + "\nTwój wynik: " + formatCurrencyDisplay(money) + " zł");
            gameOverLabel.setStyle("-fx-font-size: 24pt; -fx-text-fill: white; -fx-alignment: center; -fx-text-alignment: center;");
            StackPane.setAlignment(gameOverLabel, Pos.CENTER);
            root.getChildren().add(gameOverLabel);
        }
    }

    public void closeLeaderboard() {
        System.out.println("Zamykanie tabeli wyników, pokazywanie ekranu startowego.");
        showStartScreen();
    }

    public void showError(String message) {
        if (root == null || root.getScene() == null || root.getScene().getWindow() == null || !root.getScene().getWindow().isShowing()) {
            System.err.println("BŁĄD (UI niedostępne dla Alert): " + message);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(root.getScene().getWindow());
        alert.showAndWait();
    }

    private String formatCurrencyDisplay(int amount) {
        return String.format("%,d zł", amount);
    }

    private void triggerConfetti() {
        System.out.println("Gratulacje! *efekt konfetti*");
        if (particles != null && particleCanvas != null) {
            for (int i = 0; i < 100; i++) {
                Particle p = new Particle(particleCanvas.getWidth(), particleCanvas.getHeight());
                particles.add(p);
            }
        }
    }

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
    public Leaderboard getLeaderboard() { return leaderboard; }
    public StackPane getRoot() { return root; }

    public void useLifeline5050() {
        if(lifeline5050Used || !isGameActive || currentQuestion == null || answerLabels == null) return;
        if (currentQuestion.getCorrectAnswerIndex() < 0 || currentQuestion.getCorrectAnswerIndex() >= 4) {
            System.err.println("Błąd w 50/50: Nieprawidłowy indeks poprawnej odpowiedzi.");
            return;
        }
        System.out.println("Użyto koła 50/50");
        List<Integer> wrongAnswerIndices = new ArrayList<>();
        for(int i=0; i<4; i++) {
            if (answerLabels[i] == null || !answerLabels[i].isVisible()) continue;
            if(i != currentQuestion.getCorrectAnswerIndex()) {
                wrongAnswerIndices.add(i);
            }
        }
        Collections.shuffle(wrongAnswerIndices);
        int hiddenCount = 0;
        for(int i=0; i < wrongAnswerIndices.size() && hiddenCount < 2; i++) {
            int indexToHide = wrongAnswerIndices.get(i);
            if (indexToHide >= 0 && indexToHide < 4 && answerLabels[indexToHide] != null && answerLabels[indexToHide].isVisible()) {
                answerLabels[indexToHide].setVisible(false);
                if (answerDropPanes != null && indexToHide < answerDropPanes.length && answerDropPanes[indexToHide] != null) {
                    answerDropPanes[indexToHide].setVisible(false);
                }
                hiddenCount++;
            }
        }
        lifeline5050Used = true;
        if(lifeline5050 != null) { lifeline5050.setDisable(true); lifeline5050.setOpacity(0.5); }
    }

    public void useLifelineAudience() {
        if(lifelineAudienceUsed || !isGameActive || currentQuestion == null || answerLabels == null) return;
        if (currentQuestion.getCorrectAnswerIndex() < 0 || currentQuestion.getCorrectAnswerIndex() >= 4) {
            System.err.println("Błąd w Pytaniu do publiczności: Nieprawidłowy indeks poprawnej odpowiedzi.");
            return;
        }
        System.out.println("Użyto koła Pytanie do publiczności");
        StringBuilder audienceResults = new StringBuilder("Głosy publiczności:\n");
        Random rand = new Random();
        int[] votes = new int[4];
        int totalVotesCalculated = 0;

        for(int i=0; i<4; i++) {
            if (answerLabels[i] == null || !answerLabels[i].isVisible()) continue;
            if(i == currentQuestion.getCorrectAnswerIndex()) {
                votes[i] = 20 + rand.nextInt(51);
            } else {
                votes[i] = rand.nextInt(31);
            }
            totalVotesCalculated += votes[i];
        }

        for(int i=0; i<4; i++) {
            if (answerLabels[i] == null || !answerLabels[i].isVisible() || answerLabels[i].getText() == null) continue;
            double percentage = (totalVotesCalculated == 0) ? 0 : ((double)votes[i] / totalVotesCalculated) * 100;
            String labelText = answerLabels[i].getText();
            audienceResults.append(String.format("%s... : %.1f%%\n", labelText.substring(0, Math.min(labelText.length(),15)), percentage));
        }
        showInfo("Pytanie do publiczności", audienceResults.toString());
        lifelineAudienceUsed = true;
        if(lifelineAudience != null) { lifelineAudience.setDisable(true); lifelineAudience.setOpacity(0.5); }
    }

    public void useLifelinePhone() {
        if(lifelinePhoneUsed || !isGameActive || currentQuestion == null || answerLabels == null || currentQuestion.getAnswers() == null) return;
        if (currentQuestion.getCorrectAnswerIndex() < 0 || currentQuestion.getCorrectAnswerIndex() >= currentQuestion.getAnswers().length) {
            System.err.println("Błąd w Telefonie do przyjaciela: Nieprawidłowy indeks poprawnej odpowiedzi.");
            showInfo("Telefon do przyjaciela", "Przepraszam, mam problem ze znalezieniem odpowiedzi.");
            lifelinePhoneUsed = true;
            if(lifelinePhone != null) { lifelinePhone.setDisable(true); lifelinePhone.setOpacity(0.5); }
            return;
        }
        System.out.println("Użyto koła Telefon do przyjaciela");
        String correctAnswerText = currentQuestion.getAnswers()[currentQuestion.getCorrectAnswerIndex()];
        Random rand = new Random();
        String friendSuggestion;
        if (rand.nextInt(100) < 75) {
            friendSuggestion = correctAnswerText;
        } else {
            List<String> allAnswers = new ArrayList<>(Arrays.asList(currentQuestion.getAnswers()));
            allAnswers.remove(correctAnswerText);
            if (!allAnswers.isEmpty()) {
                friendSuggestion = allAnswers.get(rand.nextInt(allAnswers.size()));
            } else {
                friendSuggestion = correctAnswerText;
            }
        }
        showInfo("Telefon do przyjaciela", "Twój przyjaciel sugeruje: " + friendSuggestion);
        lifelinePhoneUsed = true;
        if(lifelinePhone != null) { lifelinePhone.setDisable(true); lifelinePhone.setOpacity(0.5); }
    }

    private void showInfo(String title, String message) {
        if (root == null || root.getScene() == null || root.getScene().getWindow() == null || !root.getScene().getWindow().isShowing()) {
            System.out.println("INFO (UI niedostępne dla Alert): " + title + " - " + message);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(root.getScene().getWindow());
        alert.showAndWait();
    }
}