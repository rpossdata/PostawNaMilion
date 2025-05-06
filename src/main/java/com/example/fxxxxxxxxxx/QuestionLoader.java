package com.example.fxxxxxxxxxx;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class QuestionLoader {
    private Map<Integer, List<Question>> questionsByLevel = new HashMap<>();

    public void loadQuestions() {
        try {
            java.net.URL resource = getClass().getResource("/questions.txt");
            if (resource == null) {
                throw new IllegalStateException("Plik questions.txt nie został znaleziony w folderze zasobów (src/main/resources)");
            }
            List<String> lines = Files.readAllLines(Paths.get(resource.toURI()), StandardCharsets.UTF_8);
            int currentLevel = 0;
            String currentQuestionText = null;
            String[] currentAnswers = null;
            int currentCorrectAnswer = -1;

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.equals("---")) {
                    if (currentQuestionText != null && currentAnswers != null && currentCorrectAnswer != -1 && currentLevel >= 1 && currentLevel <= 10) {
                        questionsByLevel.computeIfAbsent(currentLevel, k -> new ArrayList<>()).add(new Question(currentQuestionText, currentAnswers, currentCorrectAnswer));
                    }
                    currentQuestionText = null;
                    currentAnswers = null;
                    currentCorrectAnswer = -1;
                    continue;
                }

                if (line.startsWith("Poziom: ")) {
                    currentLevel = Integer.parseInt(line.substring("Poziom: ".length()).trim());
                } else if (line.startsWith("Pytanie: ")) {
                    currentQuestionText = line.substring("Pytanie: ".length()).trim();
                } else if (line.startsWith("Odpowiedzi: ")) {
                    String answersStr = line.substring("Odpowiedzi: ".length()).trim();
                    currentAnswers = answersStr.split("\\|");
                    if (currentAnswers.length != 4) {
                        throw new IllegalArgumentException("Każde pytanie musi mieć dokładnie 4 odpowiedzi: " + answersStr);
                    }
                } else if (line.startsWith("Poprawna: ")) {
                    currentCorrectAnswer = Integer.parseInt(line.substring("Poprawna: ".length()).trim());
                    if (currentCorrectAnswer < 0 || currentCorrectAnswer > 3) {
                        throw new IllegalArgumentException("Poprawna odpowiedź musi być w zakresie 0-3: " + currentCorrectAnswer);
                    }
                }
            }

            boolean hasQuestions = questionsByLevel.values().stream().anyMatch(list -> !list.isEmpty());
            if (!hasQuestions) {
                throw new IllegalStateException("Nie wczytano żadnych pytań z pliku questions.txt - plik jest pusty lub ma nieprawidłowy format");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean hasQuestions(int level) {
        return questionsByLevel.containsKey(level) && !questionsByLevel.get(level).isEmpty();
    }

    public Question getRandomQuestion(int level) {
        List<Question> questions = questionsByLevel.get(level);
        int randomIndex = new Random().nextInt(questions.size());
        return questions.remove(randomIndex);
    }
}
