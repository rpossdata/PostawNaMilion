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
import javafx.scene.input.ClipboardContent; // Ten import może nie być potrzebny, jeśli nie implementujesz przeciągania danych z aplikacji
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
        leaderboard = new Leaderboard(this); // Przekazanie instancji GameController do Leaderboard
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
                for (Particle p : new ArrayList<>(particles)) { // Kopiowanie listy, aby uniknąć ConcurrentModificationException
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
            endGame("Czas minął! Twój wynik to " + formatCurrencyDisplay(money) + " zł!");
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
            if (answerDropPanes[i] != null) { // Dodatkowe sprawdzenie null
                answerDropPanes[i].setVisible(true);
                answerDropPanes[i].getChildren().clear();
            }
            if (answerStakeLabels[i] != null) { // Dodatkowe sprawdzenie null
                answerStakeLabels[i].setText("0 zł");
            }


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
            if (allInButtons[i] != null) { // Dodatkowe sprawdzenie null
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
                    if (banknoteManager.getBanknotes() != null) { // Sprawdzenie czy lista banknotów nie jest null
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
                    }
                    banknoteManager.updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);
                });
            }
        }
    }

    public void confirmAnswer() {
        if (!isGameActive || currentQuestion == null) return;

        int totalStake = 0;
        int correctStake = 0;
        int[] stakes = new int[4];

        for (int i = 0; i < 4; i++) {
            int stakeOnAnswer = 0;
            if (answerDropPanes[i] != null && banknoteManager != null && banknoteManager.getBanknoteLocations() != null) {
                stakeOnAnswer = (int) answerDropPanes[i].getChildren().stream()
                        .filter(node -> node instanceof Label && banknoteManager.getBanknoteLocations().containsKey(node))
                        .count() * BANKNOTE_VALUE;
            }
            stakes[i] = stakeOnAnswer;
            totalStake += stakeOnAnswer;
            if (i == currentQuestion.getCorrectAnswerIndex()) {
                correctStake = stakeOnAnswer;
            }
        }

        if (totalStake < MINIMUM_BET) {
            showError("Musisz postawić co najmniej " + formatCurrencyDisplay(MINIMUM_BET) + " zł!");
            return;
        }

        int correctIndex = currentQuestion.getCorrectAnswerIndex();
        if (correctIndex >= 0 && correctIndex < answerLabels.length && answerLabels[correctIndex] != null) { // Dodatkowe sprawdzenie null
            answerLabels[correctIndex].setStyle(
                    "-fx-font-size: 16pt; -fx-font-weight: bold; -fx-text-fill: #1a1a2e; " +
                            "-fx-background-color: #00ff88; -fx-padding: 15; -fx-background-radius: 10; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,255,0,0.7), 15, 0, 0, 0);"
            );
            ScaleTransition stCorrect = new ScaleTransition(Duration.millis(600), answerLabels[correctIndex]);
            stCorrect.setFromX(1.0);
            stCorrect.setFromY(1.0);
            stCorrect.setToX(1.15);
            stCorrect.setToY(1.15);
            stCorrect.setAutoReverse(true);
            stCorrect.setCycleCount(3);
            stCorrect.play();
        }

        for (int i = 0; i < 4; i++) {
            if (i != correctIndex && stakes[i] > 0 && answerLabels[i] != null) { // Dodatkowe sprawdzenie null
                answerLabels[i].setStyle(
                        "-fx-font-size: 16pt; -fx-font-weight: bold; -fx-text-fill: #ffffff; " +
                                "-fx-background-color: #ff5555; -fx-padding: 15; -fx-background-radius: 10; " +
                                "-fx-effect: dropshadow(gaussian, rgba(255,0,0,0.7), 15, 0, 0, 0);"
                );
                ScaleTransition stIncorrect = new ScaleTransition(Duration.millis(600), answerLabels[i]);
                stIncorrect.setFromX(1.0);
                stIncorrect.setFromY(1.0);
                stIncorrect.setToX(1.15);
                stIncorrect.setToY(1.15);
                stIncorrect.setAutoReverse(true);
                stIncorrect.setCycleCount(3);
                stIncorrect.play();
            }
        }

        if (correctStake == totalStake && totalStake > 0) {
            showInfo("Gratulacje! Poprawna odpowiedź!");
            Timeline delayTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
                currentQuestionIndex++;
                if (currentQuestionIndex >= 10) {
                    triggerConfetti();
                    endGame("Gratulacje! Wygrałeś " + formatCurrencyDisplay(money) + " zł!");
                } else {
                    loadNextQuestion();
                }
            }));
            delayTimeline.play();
        } else {
            int incorrectStake = totalStake - correctStake;
            money -= incorrectStake;
            Timeline delayTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
                endGame("Zła odpowiedź! Twój wynik to " + formatCurrencyDisplay(money) + " zł!");
            }));
            delayTimeline.play();
        }
    }

    public void surrender() {
        if (!isGameActive) return;
        endGame("Poddałeś się! Twój wynik to " + formatCurrencyDisplay(money) + " zł!");
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
            if (answerLabels[indexToHide] != null && answerLabels[indexToHide].isVisible()) { // Dodatkowe sprawdzenie null
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

        percentages[correctIdx] = 40 + random.nextInt(31); // Od 40 do 70
        sum += percentages[correctIdx];

        List<Integer> otherIndices = new ArrayList<>();
        for (int i = 0; i < 4; i++) if (i != correctIdx) otherIndices.add(i);

        for (int i = 0; i < otherIndices.size(); i++) {
            int idx = otherIndices.get(i);
            if (i == otherIndices.size() - 1) { // Ostatni pozostały indeks
                percentages[idx] = 100 - sum;
            } else {
                // Pozostali dostają losowe wartości, dbając by suma nie przekroczyła 100
                // i by każdy miał przynajmniej małą szansę (np. 0-5%)
                int maxForThis = Math.max(0, 100 - sum - (otherIndices.size() - 1 - i) * 0); // *0 aby nie rezerwować minimum
                percentages[idx] = random.nextInt(Math.min(20, maxForThis + 1)); // Losuje od 0 do min(20, maxForThis)
            }
            sum += percentages[idx];
        }
        // Korekta jeśli suma nie jest dokładnie 100
        if (sum != 100) {
            int diff = 100 - sum;
            // Rozdziel różnicę lub dodaj do losowego/poprawnego
            if (otherIndices.size() > 0) {
                percentages[otherIndices.get(random.nextInt(otherIndices.size()))] += diff;
            } else { // Powinno się zdarzyć tylko jeśli są tylko 2 odpowiedzi, a jedna jest poprawna
                percentages[correctIdx] += diff;
            }
            // Upewnij się, że żadna wartość nie jest ujemna po korekcie
            for(int k=0; k<4; ++k) if(percentages[k] < 0) percentages[k] = 0;
            // Ponownie znormalizuj, jeśli to konieczne (ostateczność)
            int finalSumCheck = 0; for(int p : percentages) finalSumCheck += p;
            if(finalSumCheck != 100 && finalSumCheck != 0) { // jeśli suma nie jest 0 (np. wszystkie są 0)
                for(int k=0; k<4; ++k) percentages[k] = (int)Math.round(percentages[k] * 100.0 / finalSumCheck);
                finalSumCheck = 0; for(int p : percentages) finalSumCheck += p;
                if(finalSumCheck != 100 && percentages.length > 0) percentages[random.nextInt(percentages.length)] += (100 - finalSumCheck); // Ostateczna drobna korekta
            }
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
        if (rand.nextInt(100) < 25) { // 25% szans na złą odpowiedź
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
        if (buttonBar != null && buttonBar.getButtons() != null) { // Dodatkowe sprawdzenie null
            buttonBar.getButtons().forEach(btn -> btn.setStyle("-fx-background-color: #00eaff; -fx-text-fill: #1a1a2e; -fx-background-radius: 8; -fx-font-size: 14pt;"));
        }
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
            root.getChildren().add(particleCanvas); // Dodaj tło z cząsteczkami jeśli istnieje
        }

        VBox leaderboardPane = leaderboard.createLeaderboardPanel(); // Tworzenie panelu liderów
        // Poniższy fragment może nie być już potrzebny jeśli przycisk "Zamknij" jest konfigurowany w Leaderboard.java
        // Jeśli jednak chcesz dynamicznie przypisać akcję tutaj:
        leaderboardPane.getChildren().stream()
                .filter(node -> node instanceof Button && "Zamknij".equals(((Button) node).getText()))
                .map(node -> (Button) node)
                .findFirst()
                .ifPresent(closeButton -> {
                    // Akcja przycisku "Zamknij" jest już ustawiona w Leaderboard.createLeaderboardPanel
                    // Jeśli chcesz ją nadpisać lub dodać coś:
                    // closeButton.setOnAction(e -> closeLeaderboard());
                });


        root.getChildren().add(leaderboardPane); // Dodawanie panelu liderów do głównego kontenera
    }

    // Metoda do zamykania tabeli wyników i np. powrotu do panelu podawania pseudonimu
    public void closeLeaderboard() { // ZMIENIONO NA PUBLIC
        System.out.println("Zamykanie tabeli wyników i pokazywanie panelu pseudonimu.");
        showNicknamePanel(); // Lub inna logika, np. reset gry, powrót do menu głównego itp.
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

                    int finalI = i; // Dla użycia w lambdzie
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
                    event.acceptTransferModes(TransferMode.MOVE); // Poprawiona linia
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
            particles.add(new Particle(particleCanvas.getWidth(), particleCanvas.getHeight(), true)); // true dla confetti
        }
        Timeline confettiTimer = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            particles.removeIf(Particle::isConfetti); // Użycie referencji do metody
        }));
        confettiTimer.setCycleCount(1); // Wykonaj tylko raz
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
        if (alert.getDialogPane().lookup(".content.label") != null) { // Sprawdzenie null
            alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14pt;");
        }
        ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().lookup(".button-bar");
        if (buttonBar != null && buttonBar.getButtons() != null) { // Sprawdzenie null
            buttonBar.getButtons().forEach(btn -> btn.setStyle("-fx-background-color: #ff5555; -fx-text-fill: #1a1a2e; -fx-background-radius: 8; -fx-font-size: 14pt;"));
        }
        alert.showAndWait();
    }

    public void showInfo(String message) {
        System.out.println("Pokazywanie dialogu informacji: " + message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Informacja");
        alert.setHeaderText(null);
        alert.getDialogPane().setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: #ffffff; -fx-border-color: #00eaff; -fx-border-width: 2; -fx-border-radius: 10; -fx-padding: 15;");
        if (alert.getDialogPane().lookup(".content.label") != null) { // Sprawdzenie null
            alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14pt;");
        }
        ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().lookup(".button-bar");
        if (buttonBar != null && buttonBar.getButtons() != null) { // Sprawdzenie null
            buttonBar.getButtons().forEach(btn -> btn.setStyle("-fx-background-color: #00eaff; -fx-text-fill: #1a1a2e; -fx-background-radius: 8; -fx-font-size: 14pt;"));
        }
        alert.showAndWait();
    }
}