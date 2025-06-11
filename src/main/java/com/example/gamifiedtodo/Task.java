package com.example.gamifiedtodo;

public class Task {
    private int id;
    private String title;
    private String difficulty;
    private boolean isDone;
    private int xpReward;

    public Task(int id, String title, String difficulty, boolean isDone, int xpReward) {
        this.id = id;
        this.title = title;
        this.difficulty = difficulty;
        this.isDone = isDone;
        this.xpReward = xpReward;
    }

    public Task(String title, String difficulty) {
        this(-1, title, difficulty, false, calculateXP(difficulty));
    }

    public static int calculateXP(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> 10;
            case "medium" -> 20;
            case "hard" -> 30;
            default -> 15;
        };
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDifficulty() { return difficulty; }
    public boolean isDone() { return isDone; }
    public int getXpReward() { return xpReward; }

    public void setDone(boolean done) { isDone = done; }

    @Override
    public String toString() {
        return title + " (" + difficulty + ") " + (isDone ? "[Done]" : "[Pending]");
    }
}
