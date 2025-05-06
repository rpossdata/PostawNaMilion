package com.example.fxxxxxxxxxx;

import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BanknoteManager {
    private List<Label> banknotes = new ArrayList<>();
    private Map<Label, Pane> banknoteLocations = new HashMap<>();
    private static final int BANKNOTE_VALUE = 50000;
    private static final int MAX_VISIBLE_BANKNOTES = 20;

    public void initializeBanknotes(int money, Pane banknoteStackPane) {
        System.out.println("Initializing banknotes: money=" + money + ", banknoteStackPane=" + banknoteStackPane);
        banknoteStackPane.getChildren().clear();
        banknotes.clear();
        banknoteLocations.clear();
        int banknoteCount = money / BANKNOTE_VALUE;

        for (int i = 0; i < banknoteCount; i++) {
            Label banknote = new Label("50 000 zł");
            banknote.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #3a5f9b, #2b3f6e);" +
                            "-fx-text-fill: #e0e6ff;" +
                            "-fx-font-family: 'Arial';" +
                            "-fx-font-size: 16pt;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 12 20 12 20;" +
                            "-fx-border-color: #1e2a44;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0.2, 2, 2);" +
                            "-fx-background-insets: 0;" +
                            "-fx-border-insets: 0;"
            );
            banknote.setMinWidth(120);
            banknote.setMinHeight(60);
            // Stack banknotes directly on top of each other
            banknote.setLayoutX(15);
            banknote.setLayoutY(10);
            banknote.setRotate(0); // No rotation
            banknote.setMouseTransparent(false);
            banknote.setUserData(i);
            if (i < MAX_VISIBLE_BANKNOTES) {
                banknoteStackPane.getChildren().add(banknote);
            } else {
                banknote.setVisible(false);
                banknoteStackPane.getChildren().add(0, banknote);
            }
            banknotes.add(banknote);
            banknoteLocations.put(banknote, banknoteStackPane);

            // Hover effect
            banknote.setOnMouseEntered(event -> {
                banknote.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #4a7fc8, #3a5f9b);" +
                                "-fx-text-fill: #ffffff;" +
                                "-fx-font-family: 'Arial';" +
                                "-fx-font-size: 16pt;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 12 20 12 20;" +
                                "-fx-border-color: #2a3f6e;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0.3, 3, 3);"
                );
                banknote.setScaleX(1.05);
                banknote.setScaleY(1.05);
            });

            banknote.setOnMouseExited(event -> {
                banknote.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #3a5f9b, #2b3f6e);" +
                                "-fx-text-fill: #e0e6ff;" +
                                "-fx-font-family: 'Arial';" +
                                "-fx-font-size: 16pt;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 12 20 12 20;" +
                                "-fx-border-color: #1e2a44;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0.2, 2, 2);"
                );
                banknote.setScaleX(1.0);
                banknote.setScaleY(1.0);
            });

            banknote.setOnDragDetected(event -> {
                System.out.println("Drag detected for banknote index: " + banknotes.indexOf(banknote));
                Dragboard db = banknote.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                int index = banknotes.indexOf(banknote);
                content.putString(String.valueOf(index));
                db.setContent(content);
                banknote.setOpacity(0.6);
                event.consume();
            });

            banknote.setOnDragDone(event -> {
                System.out.println("Drag done for banknote index: " + banknotes.indexOf(banknote));
                banknote.setOpacity(1.0);
                banknote.setScaleX(1.0);
                banknote.setScaleY(1.0);
                event.consume();
            });
        }
        System.out.println("Initialized " + banknoteCount + " banknotes");
    }

    public void updateRemainingMoney(Label moneyLabel, Label[] answerStakeLabels, Pane[] answerDropPanes) {
        if (moneyLabel == null || answerStakeLabels == null || answerDropPanes == null) {
            System.err.println("UI components not initialized in updateRemainingMoney");
            return;
        }

        int totalStake = 0;
        for (int i = 0; i < Math.min(4, answerDropPanes.length); i++) {
            if (i < answerStakeLabels.length && answerDropPanes[i] != null) {
                int stake = (int) answerDropPanes[i].getChildren().stream()
                        .filter(banknoteLocations::containsKey)
                        .count() * BANKNOTE_VALUE;
                answerStakeLabels[i].setText(formatCurrencyDisplay(stake) + " zł");
                totalStake += stake;
            }
        }
        int remaining = 1000000 - totalStake;
        moneyLabel.setText("Pozostało: " + formatCurrencyDisplay(remaining) + " zł");

        if (remaining < 0) {
            moneyLabel.setStyle("-fx-font-size: 18pt; -fx-font-weight: bold; -fx-text-fill: #ff5555; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 1, 1);");
        } else {
            moneyLabel.setStyle("-fx-font-size: 18pt; -fx-font-weight: bold; -fx-text-fill: #00ff88; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 1, 1);");
        }
    }

    private String formatCurrencyDisplay(int amount) {
        return String.format("%,d", amount).replace(",", " ");
    }

    public List<Label> getBanknotes() {
        return banknotes;
    }

    public Map<Label, Pane> getBanknoteLocations() {
        return banknoteLocations;
    }

    public void placeBanknoteInPane(Label banknote, Pane targetPane) {
        int currentCount = (int) targetPane.getChildren().stream()
                .filter(node -> node instanceof Label && banknoteLocations.containsKey(node))
                .count();
        if (currentCount < MAX_VISIBLE_BANKNOTES) {
            banknote.setVisible(true);
            banknote.setLayoutX(8);
            banknote.setLayoutY(8); // Stack directly on top
            banknote.setRotate(0); // No rotation
            targetPane.getChildren().add(banknote);
        } else {
            banknote.setVisible(false);
            targetPane.getChildren().add(0, banknote);
        }
    }
}