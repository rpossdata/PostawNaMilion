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
    private Button lifeline5050, lifelineAudience, lifelinePhone, confirmButton;
    private ProgressBar timerProgress, levelProgress;
    private Pane banknoteStackPane;
    private Timeline timer;
    private Canvas particleCanvas;
    private List<Particle> particles = new ArrayList<>();
    private int timeLeft = 120;
    private int currentQuestionIndex = 0;
    private int money = 1000000;
    private int correctStreak = 0;
    private boolean lifeline5050Used = false;
    private boolean lifelineAudienceUsed = false;
    private boolean lifelinePhoneUsed = false;
    private Question currentQuestion;
    private QuestionLoader questionLoader;
    private BanknoteManager banknoteManager;
    private UIFactory uiFactory;

    public GameController() {
        questionLoader = new QuestionLoader();
        banknoteManager = new BanknoteManager();
        uiFactory = new UIFactory(banknoteManager, this);
    }

    public void initializeUI(StackPane root) {
        particleCanvas = new Canvas();
        particleCanvas.widthProperty().bind(root.widthProperty());
        particleCanvas.heightProperty().bind(root.heightProperty());
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle(particleCanvas.getWidth(), particleCanvas.getHeight()));
        }
        AnimationTimer particleTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                GraphicsContext gc = particleCanvas.getGraphicsContext2D();
                gc.setFill(Color.web("#1a1a2e"));
                gc.fillRect(0, 0, particleCanvas.getWidth(), particleCanvas.getHeight());
                for (Particle p : particles) {
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
    }

    public void startGame() {
        questionLoader.loadQuestions();
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timer.setCycleCount(Timeline.INDEFINITE);
        loadNextQuestion();
        timer.play();
    }

    public void handleWindowMaximized() {
        particles.clear();
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle(particleCanvas.getWidth(), particleCanvas.getHeight()));
        }
    }

    private void updateTimer() {
        timeLeft--;
        timerLabel.setText(timeLeft + "s");
        timerProgress.setProgress(timeLeft / (currentQuestionIndex >= 6 ? 90.0 : 120.0));

        if (timeLeft <= 30 && timeLeft % 2 == 0) {
            FadeTransition ft = new FadeTransition(Duration.millis(200), timerLabel);
            ft.setFromValue(1.0);
            ft.setToValue(0.3);
            ft.setAutoReverse(true);
            ft.setCycleCount(2);
            ft.play();
        }

        if (timeLeft <= 0) {
            timer.stop();
            showError("Czas minął! Przegrałeś!");
            javafx.application.Platform.exit();
            System.exit(0);
        }
    }

    private void loadNextQuestion() {
        int level = currentQuestionIndex + 1;
        levelLabel.setText("Poziom: " + level);
        levelProgress.setProgress(level / 10.0);

        questionLabel.setStyle("-fx-font-size: 22pt; -fx-font-weight: bold; -fx-text-fill: #00eaff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 8, 0, 2, 2);");

        if (!questionLoader.hasQuestions(level)) {
            triggerConfetti();
            showInfo("Gratulacje! Wygrałeś " + formatCurrencyDisplay(money) + " zł!");
            timer.stop();
            return;
        }

        currentQuestion = questionLoader.getRandomQuestion(level);
        questionLabel.setText(currentQuestion.getText());

        TranslateTransition tt = new TranslateTransition(Duration.millis(800), questionLabel);
        tt.setFromY(-50);
        tt.setToY(0);
        questionLabel.setTranslateY(-50);
        tt.play();

        for (int i = 0; i < 4; i++) {
            answerLabels[i].setText(currentQuestion.getAnswers()[i]);
            answerLabels[i].setStyle("-fx-font-size: 16pt; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-background-color: #2a2a4a; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 2, 2);");
            answerLabels[i].setVisible(true);
            answerLabels[i].setOpacity(1.0);
            answerLabels[i].setEffect(null);
            answerDropPanes[i].setVisible(true);
            answerDropPanes[i].getChildren().clear();
            answerStakeLabels[i].setText("0 zł");

            ScaleTransition st = new ScaleTransition(Duration.millis(500), answerLabels[i]);
            st.setFromX(0.1);
            st.setFromY(0.1);
            st.setToX(1.0);
            st.setToY(1.0);
            answerLabels[i].setScaleX(0.1);
            answerLabels[i].setScaleY(0.1);
            st.play();
        }

        banknoteManager.initializeBanknotes(money, banknoteStackPane);
        timeLeft = level >= 6 ? 90 : 120;
        timerLabel.setText(timeLeft + "s");
        timerProgress.setProgress(1.0);
        banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);
    }

    public void confirmAnswer() {
        int totalStake = 0;
        int correctStake = 0;
        int[] stakes = new int[4];

        for (int i = 0; i < 4; i++) {
            int stake = (int) answerDropPanes[i].getChildren().stream().filter(banknoteManager.getBanknoteLocations()::containsKey).count() * 50000;
            stakes[i] = stake;
            totalStake += stake;
            if (i == currentQuestion.getCorrectAnswerIndex()) {
                correctStake = stake;
            }
        }

        if (totalStake != money) {
            showError("Musisz postawić dokładnie " + formatCurrencyDisplay(money) + " zł!");
            return;
        }

        for (int i = 0; i < 4; i++) {
            if (stakes[i] == money && i != currentQuestion.getCorrectAnswerIndex()) {
                showError("Postawiłeś wszystko na błędną odpowiedź! Koniec gry!");
                timer.stop();
                javafx.application.Platform.exit();
                System.exit(0);
                return;
            }
        }

        if (correctStake == 0) {
            showError("Nie postawiłeś nic na poprawną odpowiedź. Przegrałeś!");
            timer.stop();
            javafx.application.Platform.exit();
            System.exit(0);
            return;
        }

        int winnings = correctStake;

        if (correctStake > 0) {
            ScaleTransition st = new ScaleTransition(Duration.millis(500), answerLabels[currentQuestion.getCorrectAnswerIndex()]);
            st.setFromX(1.0);
            st.setFromY(1.0);
            st.setToX(1.2);
            st.setToY(1.2);
            st.setAutoReverse(true);
            st.setCycleCount(2);
            st.play();
        }

        money = winnings;
        correctStreak++;
        // Usunięto logikę mnożnika i komunikat o serii 3 odpowiedzi

        // Sprawdzanie i odnawianie kół ratunkowych po serii 3 poprawnych odpowiedzi
        if (correctStreak >= 3 && (lifeline5050Used || lifelineAudienceUsed || lifelinePhoneUsed)) {
            List<String> usedLifelines = new ArrayList<>();
            if (lifeline5050Used) usedLifelines.add("5050");
            if (lifelineAudienceUsed) usedLifelines.add("Audience");
            if (lifelinePhoneUsed) usedLifelines.add("Phone");
            if (!usedLifelines.isEmpty()) {
                String recharged = usedLifelines.get(new Random().nextInt(usedLifelines.size()));
                switch (recharged) {
                    case "5050":
                        lifeline5050Used = false;
                        lifeline5050.setDisable(false);
                        lifeline5050.setStyle(uiFactory.getButtonBaseStyle());
                        break;
                    case "Audience":
                        lifelineAudienceUsed = false;
                        lifelineAudience.setDisable(false);
                        lifelineAudience.setStyle(uiFactory.getButtonBaseStyle());
                        break;
                    case "Phone":
                        lifelinePhoneUsed = false;
                        lifelinePhone.setDisable(false);
                        lifelinePhone.setStyle(uiFactory.getButtonBaseStyle());
                        break;
                }
                showInfo("Odnowiono koło ratunkowe: " + recharged + "!");
                correctStreak = 0;
            }
        }

        currentQuestionIndex++;
        loadNextQuestion();
    }

    public void use5050() {
        if (lifeline5050Used) return;
        lifeline5050Used = true;
        lifeline5050.setDisable(true);
        lifeline5050.setStyle(uiFactory.getButtonDisabledStyle());
        int correctIndex = currentQuestion.getCorrectAnswerIndex();
        List<Integer> indices = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        indices.remove((Integer) correctIndex);
        Collections.shuffle(indices);
        for (int i = 0; i < 2; i++) {
            int index = indices.get(i);
            FadeTransition ft = new FadeTransition(Duration.millis(500), answerLabels[index]);
            ft.setToValue(0);
            ft.setOnFinished(e -> {
                answerLabels[index].setVisible(false);
                answerLabels[index].setOpacity(0.0);
                answerDropPanes[index].setVisible(false);
            });
            ft.play();
        }
    }

    public void useAudience() {
        if (lifelineAudienceUsed) return;
        lifelineAudienceUsed = true;
        lifelineAudience.setDisable(true);
        lifelineAudience.setStyle(uiFactory.getButtonDisabledStyle());
        String[] votes = new String[4];
        Random rand = new Random();
        int correct = currentQuestion.getCorrectAnswerIndex();
        int remaining = 100;
        for (int i = 0; i < 4; i++) {
            if (i == correct) {
                votes[i] = (rand.nextInt(41) + 40) + "%";
                remaining -= Integer.parseInt(votes[i].replace("%", ""));
            } else {
                votes[i] = (rand.nextInt(Math.min(remaining, 21))) + "%";
                remaining -= Integer.parseInt(votes[i].replace("%", ""));
            }
        }
        StringBuilder result = new StringBuilder("Głosy publiki:\n");
        for (int i = 0; i < 4; i++) {
            result.append(currentQuestion.getAnswers()[i]).append(": ").append(votes[i]).append("\n");
        }
        showInfo(result.toString());
    }

    public void usePhone() {
        if (lifelinePhoneUsed) return;
        lifelinePhoneUsed = true;
        lifelinePhone.setDisable(true);
        lifelinePhone.setStyle(uiFactory.getButtonDisabledStyle());
        int correct = currentQuestion.getCorrectAnswerIndex();
        String suggestion = currentQuestion.getAnswers()[correct];
        showInfo("Dziekan: Słuchaj, to będzie: " + suggestion);
    }

    private void triggerConfetti() {
        for (int i = 0; i < 100; i++) {
            particles.add(new Particle(particleCanvas.getWidth(), particleCanvas.getHeight(), true));
        }
        Timeline confettiTimer = new Timeline(new KeyFrame(Duration.seconds(5), e -> particles.removeIf(p -> p.isConfetti)));
        confettiTimer.play();
    }

    private String formatCurrencyDisplay(int amount) {
        return String.format("%,d", amount).replace(",", " ");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.getDialogPane().setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: #ffffff; -fx-border-color: #00eaff; -fx-border-width: 2; -fx-border-radius: 10;");
        alert.getDialogPane().lookup(".content").setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14pt;");
        ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().lookup(".button-bar");
        buttonBar.getButtons().forEach(btn -> btn.setStyle("-fx-background-color: #00eaff; -fx-text-fill: #1a1a2e; -fx-background-radius: 8; -fx-font-size: 14pt;"));
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.getDialogPane().setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: #ffffff; -fx-border-color: #00eaff; -fx-border-width: 2; -fx-border-radius: 10; -fx-padding: 15;");
        alert.getDialogPane().lookup(".content").setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14pt;");
        ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().lookup(".button-bar");
        buttonBar.getButtons().forEach(btn -> btn.setStyle("-fx-background-color: #00eaff; -fx-text-fill: #1a1a2e; -fx-background-radius: 8; -fx-font-size: 14pt;"));
        alert.showAndWait();
    }
}
