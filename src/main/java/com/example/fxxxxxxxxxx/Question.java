package com.example.fxxxxxxxxxx;

public class Question {
    private String text;
    private String[] answers;
    private int correctAnswerIndex;

    public Question(String text, String[] answers, int correctAnswerIndex) {
        this.text = text;
        this.answers = answers;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getText() {
        return text;
    }

    public String[] getAnswers() {
        return answers;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }
}
