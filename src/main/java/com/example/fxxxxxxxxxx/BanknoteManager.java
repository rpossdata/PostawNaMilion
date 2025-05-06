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

    public void initializeBanknotes(int money, Pane banknoteStackPane) {
        banknoteStackPane.getChildren().clear();
        banknotes.clear();
        banknoteLocations.clear();
        int banknoteCount = money / 50000;
        for (int i = 0; i < banknoteCount; i++) {
            Label banknote = new Label("50 000");
            banknote.setStyle("-fx-background-color: #ffcc00; -fx-text-fill: #000000; -fx-font-size: 12pt; -fx-padding: 10; -fx-border-color: #000000; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
            banknote.setMinWidth(80);
            banknote.setMinHeight(40);
            banknote.setLayoutX(10 + (i % 5) * 5);
            banknote.setLayoutY(5 + (i / 5) * 5);
            banknoteStackPane.getChildren().add(banknote);
            banknotes.add(banknote);
            banknoteLocations.put(banknote, banknoteStackPane);

            banknote.setOnDragDetected(event -> {
                Dragboard db = banknote.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                int index = banknotes.indexOf(banknote);
                content.putString(String.valueOf(index));
                db.setContent(content);
                event.consume();
            });

            banknote.setOnDragDone(event -> {
                if (event.getTransferMode() == TransferMode.MOVE) {
                    Label moneyLabel = null;
                    Label[] answerStakeLabels = new Label[0];
                    Pane[] answerDropPanes = new Pane[0];
                    updateRemainingMoney(moneyLabel, answerStakeLabels, answerDropPanes);
                }
                event.consume();
            });
        }
    }

    public void updateRemainingMoney(Label moneyLabel, Label[] answerStakeLabels, Pane[] answerDropPanes) {
        int totalStake = 0;
        for (int i = 0; i < 4; i++) {
            int stake = (int) answerDropPanes[i].getChildren().stream().filter(banknoteLocations::containsKey).count() * 50000;
            answerStakeLabels[i].setText(formatCurrencyDisplay(stake) + " zł");
            totalStake += stake;
        }
        int remaining = 1000000 - totalStake; // Zakładamy początkową kwotę 1 000 000
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
}
