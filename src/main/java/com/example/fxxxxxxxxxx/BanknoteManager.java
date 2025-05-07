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
import java.util.stream.Collectors;


public class BanknoteManager {
    private List<Label> banknotes = new ArrayList<>();
    private Map<Label, Pane> banknoteLocations = new HashMap<>();
    private static final int BANKNOTE_VALUE = 50000;
    private static final int MAX_VISIBLE_BANKNOTES = 20; // Maksymalna liczba widocznych banknotów w stosie

    public void initializeBanknotes(int money, Pane banknoteStackPane) {
        System.out.println("Initializing banknotes: money=" + money + ", banknoteStackPane=" + banknoteStackPane);
        if (banknoteStackPane == null) {
            System.err.println("banknoteStackPane is null in initializeBanknotes!");
            return;
        }
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
            banknote.setLayoutX(15); // Ustawienie dla głównego stosu
            banknote.setLayoutY(10); // Ustawienie dla głównego stosu
            banknote.setRotate(0);
            banknote.setMouseTransparent(false);
            banknote.setUserData(i);

            if (i < MAX_VISIBLE_BANKNOTES) {
                banknoteStackPane.getChildren().add(banknote);
            } else {
                banknote.setVisible(false); // Ukryj nadmiarowe banknoty, ale dodaj do drzewa sceny
                banknoteStackPane.getChildren().add(0, banknote); // Dodaj na spód stosu wizualnego
            }
            banknotes.add(banknote);
            banknoteLocations.put(banknote, banknoteStackPane); // Ustaw lokalizację

            // Efekty hover i drag and drop (bez zmian)
            banknote.setOnMouseEntered(event -> {
                banknote.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #4a7fc8, #3a5f9b);" +
                                "-fx-text-fill: #ffffff;" + // Styl jak wyżej
                                "-fx-font-family: 'Arial'; -fx-font-size: 16pt; -fx-font-weight: bold; -fx-padding: 12 20 12 20; -fx-border-color: #2a3f6e; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0.3, 3, 3);"
                );
                banknote.setScaleX(1.05);
                banknote.setScaleY(1.05);
            });

            banknote.setOnMouseExited(event -> {
                banknote.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #3a5f9b, #2b3f6e);" +
                                "-fx-text-fill: #e0e6ff;" + // Styl jak na początku
                                "-fx-font-family: 'Arial'; -fx-font-size: 16pt; -fx-font-weight: bold; -fx-padding: 12 20 12 20; -fx-border-color: #1e2a44; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0.2, 2, 2);"
                );
                banknote.setScaleX(1.0);
                banknote.setScaleY(1.0);
            });

            banknote.setOnDragDetected(event -> {
                System.out.println("Drag detected for banknote index: " + banknotes.indexOf(banknote));
                Dragboard db = banknote.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                int index = banknotes.indexOf(banknote); // Użyj indeksu z listy 'banknotes'
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
                // Nie konsumuj zdarzenia tutaj, aby umożliwić obsługę w onDragDropped
                // event.consume(); // Usunięto, lub zależnie od potrzeby
            });
        }
        System.out.println("Initialized " + banknoteCount + " banknotes");
    }

    public void updateRemainingMoney(Label moneyLabel, Label[] answerStakeLabels, Pane[] answerDropPanes) {
        if (moneyLabel == null || answerStakeLabels == null || answerDropPanes == null) {
            System.err.println("UI components not initialized in updateRemainingMoney");
            return;
        }

        int totalMoneyInGame = banknotes.size() * BANKNOTE_VALUE; // Całkowita kwota w grze
        int totalStake = 0;

        for (int i = 0; i < answerDropPanes.length; i++) {
            if (answerStakeLabels[i] != null && answerDropPanes[i] != null) {
                // Liczymy banknoty, które SĄ W `banknoteLocations` i ich wartością jest ten `answerDropPanes[i]`
                int stakeInPane = 0;
                for(Map.Entry<Label, Pane> entry : banknoteLocations.entrySet()){
                    if(entry.getValue() == answerDropPanes[i]){
                        stakeInPane += BANKNOTE_VALUE;
                    }
                }
                answerStakeLabels[i].setText(formatCurrencyDisplay(stakeInPane) + " zł");
                totalStake += stakeInPane;
            }
        }

        // Pieniądze pozostałe na głównym stosie (nie obstawione)
        int moneyOnStack = 0;
        Pane mainStackPane = null;
        if (!banknotes.isEmpty()) { // Znajdź główny stos (pierwszy banknot powinien tam być na początku)
            mainStackPane = banknoteLocations.get(banknotes.get(0)); // Zakładając, że stos jest tam gdzie pierwszy banknot
            // Bardziej niezawodne byłoby przekazanie stackPane jako argument, jeśli to możliwe
        }

        for(Map.Entry<Label, Pane> entry : banknoteLocations.entrySet()){
            // Jeśli lokalizacja banknotu nie jest żadnym z paneli odpowiedzi, zakładamy że jest na stosie
            boolean onAnswerPane = false;
            for(Pane answerPane : answerDropPanes) {
                if(entry.getValue() == answerPane) {
                    onAnswerPane = true;
                    break;
                }
            }
            if(!onAnswerPane) { // Zakładamy, że to jest "stack" lub inne miejsce nie będące odpowiedzią
                moneyOnStack += BANKNOTE_VALUE;
            }
        }
        // Poprzednia logika zakładała, że `totalStake` jest odjęty od 1000000
        // Prawidłowo, `remaining` to pieniądze, które nie są w `answerDropPanes`
        // int remaining = totalMoneyInGame - totalStake; // Pieniądze nieobstawione

        moneyLabel.setText("Pozostało: " + formatCurrencyDisplay(moneyOnStack) + " zł"); // Pokaż pieniądze na stosie

        if (moneyOnStack < 0) { // To nie powinno się zdarzyć
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

    // Poprawiona metoda placeBanknoteInPane
    public void placeBanknoteInPane(Label banknote, Pane targetPane) {
        if (banknote == null || targetPane == null) return;

        // Usuń z poprzedniego rodzica wizualnego, jeśli istnieje
        if (banknote.getParent() != null && banknote.getParent() instanceof Pane) {
            ((Pane) banknote.getParent()).getChildren().remove(banknote);
        }

        // Liczba banknotów już wizualnie w targetPane
        long visualBanknoteCountInTarget = targetPane.getChildren().stream().filter(node -> node instanceof Label).count();

        banknote.setLayoutX(8);  // Ustawienia dla paneli odpowiedzi
        banknote.setLayoutY(8);
        banknote.setRotate(0);

        if (visualBanknoteCountInTarget < MAX_VISIBLE_BANKNOTES) {
            banknote.setVisible(true);
            targetPane.getChildren().add(banknote);
        } else {
            banknote.setVisible(false);
            targetPane.getChildren().add(0, banknote); // Dodaj na spód stosu wizualnego
        }
        // Aktualizuj logiczną lokalizację banknotu
        banknoteLocations.put(banknote, targetPane);
    }

    /**
     * Przenosi wszystkie banknoty z sourceStackPane (logicznie) do targetPane.
     * @param targetPane Panel docelowy (jeden z paneli odpowiedzi).
     * @param sourceStackPane Panel źródłowy (główny stos banknotów).
     */
    public void moveAllBanknotesToPane(Pane targetPane, Pane sourceStackPane) {
        System.out.println("Moving all banknotes from stack ("+ sourceStackPane +") to pane: " + targetPane);
        if (targetPane == null || sourceStackPane == null) {
            System.err.println("TargetPane or SourceStackPane is null in moveAllBanknotesToPane");
            return;
        }

        List<Label> banknotesToMove = new ArrayList<>();
        // Zidentyfikuj banknoty, które są LOGICZNIE na głównym stosie
        for (Label banknote : banknotes) {
            if (banknoteLocations.get(banknote) == sourceStackPane) {
                banknotesToMove.add(banknote);
            }
        }

        System.out.println("Found " + banknotesToMove.size() + " banknotes on stack to move.");
        for (Label banknote : banknotesToMove) {
            // placeBanknoteInPane zajmie się usunięciem z poprzedniego rodzica (jeśli trzeba)
            // i dodaniem do nowego, oraz aktualizacją banknoteLocations
            placeBanknoteInPane(banknote, targetPane);
        }
        System.out.println("Moved " + banknotesToMove.size() + " banknotes to " + targetPane);
    }

    /**
     * Przenosi wszystkie banknoty z danego panelu odpowiedzi z powrotem na główny stos.
     * @param currentAnswerPane Panel odpowiedzi, z którego banknoty mają być przeniesione.
     * @param stackPane Główny stos banknotów, dokąd mają trafić.
     */
    public void moveBanknotesFromPaneToStack(Pane currentAnswerPane, Pane stackPane) {
        if (currentAnswerPane == null || stackPane == null || currentAnswerPane == stackPane) {
            System.err.println("Invalid panes in moveBanknotesFromPaneToStack or trying to move to self");
            return;
        }

        System.out.println("Moving banknotes from " + currentAnswerPane + " back to stack " + stackPane);
        List<Label> banknotesToMoveBack = new ArrayList<>();

        // Zidentyfikuj banknoty LOGICZNIE w currentAnswerPane
        for (Label banknote : banknotes) {
            if (banknoteLocations.get(banknote) == currentAnswerPane) {
                banknotesToMoveBack.add(banknote);
            }
        }

        System.out.println("Found " + banknotesToMoveBack.size() + " banknotes in " + currentAnswerPane + " to move back to stack.");
        for (Label banknote : banknotesToMoveBack) {
            // Usuń z currentAnswerPane wizualnie
            if (banknote.getParent() != null) {
                ((Pane) banknote.getParent()).getChildren().remove(banknote);
            }

            // Dodaj do stackPane wizualnie i zaktualizuj logikę
            // Użyj logiki podobnej do initializeBanknotes dla pozycjonowania na stosie
            banknote.setLayoutX(15); // Pozycja na głównym stosie
            banknote.setLayoutY(10); // Pozycja na głównym stosie
            banknote.setRotate(0);

            long visualBanknotesInStack = stackPane.getChildren().stream().filter(node -> node instanceof Label).count();
            if (visualBanknotesInStack < MAX_VISIBLE_BANKNOTES) {
                banknote.setVisible(true);
                stackPane.getChildren().add(banknote);
            } else {
                banknote.setVisible(false);
                stackPane.getChildren().add(0, banknote); // Dodaj na spód stosu wizualnego
            }
            banknoteLocations.put(banknote, stackPane); // Zaktualizuj logiczną lokalizację
        }
        System.out.println("Moved " + banknotesToMoveBack.size() + " banknotes from " + currentAnswerPane + " to stack.");
    }
}