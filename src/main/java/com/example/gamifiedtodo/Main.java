package com.example.gamifiedtodo;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.Random;

public class Main extends Application {

    private int totalXP = 0;
    private int currentLevel = 1;
    private Set<String> earnedRewards = new HashSet<>();

    private Label xpLabel = new Label("XP: 0 | Level: 1");
    private Label xpPercentageLabel = new Label("0%");
    private ProgressBar xpProgressBar = new ProgressBar(0);
    private ListView<HBox> taskListView = new ListView<>();
    private FlowPane rewardsPane = new FlowPane();

    private Pane confettiPane = new Pane();
    private VBox root;

    private int calculateLevel(int xp) {
        return (int) Math.sqrt(xp / 10);
    }

    private int getXPForCurrentLevel(int level) {
        return (int) Math.pow(level, 2) * 10;
    }

    private void updateXP(int earnedXP) {
        totalXP += earnedXP;

        int oldLevel = currentLevel;
        currentLevel = calculateLevel(totalXP);

        if (currentLevel > oldLevel) {
            animateLevelUpVisual();
            playLevelUpSound();
            showConfettiAnimation();
        }

        int xpForCurrentLevel = getXPForCurrentLevel(currentLevel);
        int xpForNextLevel = getXPForCurrentLevel(currentLevel + 1);

        int xpInCurrentLevel = totalXP - xpForCurrentLevel;
        int xpNeeded = xpForNextLevel - xpForCurrentLevel;

        double progress = Math.max(0, Math.min(1.0, (double) xpInCurrentLevel / xpNeeded));

        xpLabel.setText("XP: " + totalXP + " | Level: " + currentLevel);
        xpPercentageLabel.setText((int) (progress * 100) + "%");

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5), new KeyValue(xpProgressBar.progressProperty(), progress))
        );
        timeline.play();

        checkRewards();
    }

    private void animateLevelUpVisual() {
        ScaleTransition st = new ScaleTransition(Duration.millis(400), xpLabel);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.3);
        st.setToY(1.3);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
    }

    private void playLevelUpSound() {
        try {
            URL soundURL = getClass().getResource("/levelup.wav");
            if (soundURL != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } else {
                System.err.println("\u274C levelup.wav not found in resources!");
            }
        } catch (Exception e) {
            System.err.println("\u274C Sound error: " + e.getMessage());
        }
    }

    private void showConfettiAnimation() {
        int particleCount = 80;
        for (int i = 0; i < particleCount; i++) {
            Circle confetti = new Circle(4);
            confetti.setFill(Color.hsb(Math.random() * 360, 1.0, 1.0));
            confetti.setTranslateX(Math.random() * root.getWidth());
            confetti.setTranslateY(0);

            root.getChildren().add(confetti);

            double endY = 400 + Math.random() * 300;
            double endX = confetti.getTranslateX() + (Math.random() - 0.5) * 200;

            Timeline fall = new Timeline(
                    new KeyFrame(Duration.seconds(0),
                            new KeyValue(confetti.translateYProperty(), confetti.getTranslateY()),
                            new KeyValue(confetti.translateXProperty(), confetti.getTranslateX())
                    ),
                    new KeyFrame(Duration.seconds(2 + Math.random()),
                            new KeyValue(confetti.translateYProperty(), endY),
                            new KeyValue(confetti.translateXProperty(), endX),
                            new KeyValue(confetti.opacityProperty(), 0)
                    )
            );

            fall.setOnFinished(e -> root.getChildren().remove(confetti));
            fall.play();
        }
    }

    private void checkRewards() {
        if (totalXP >= 50 && !earnedRewards.contains("🎖️ Bronze Achiever")) {
            showReward("🎖️ Bronze Achiever", "You reached 50 XP!");
        } else if (totalXP >= 100 && !earnedRewards.contains("🏅 Silver Star")) {
            showReward("🏅 Silver Star", "You reached 100 XP!");
        } else if (totalXP >= 200 && !earnedRewards.contains("🥇 Gold Hero")) {
            showReward("🥇 Gold Hero", "You reached 200 XP!");
        }
    }

    private void showReward(String icon, String message) {
        earnedRewards.add(icon);
        Alert rewardPopup = new Alert(Alert.AlertType.INFORMATION);
        rewardPopup.setTitle("🏆 New Reward!");
        rewardPopup.setHeaderText(icon + " " + message);
        rewardPopup.setContentText("Keep completing tasks to unlock more rewards!");
        rewardPopup.show();

        Label badge = new Label(icon);
        badge.setStyle("-fx-font-size: 24px;");
        rewardsPane.getChildren().add(badge);
    }

    private String getStyledText(Task task) {
        String emoji = switch (task.getDifficulty().toLowerCase()) {
            case "easy" -> "\uD83D\uDFE2";
            case "medium" -> "\uD83D\uDFE0";
            case "hard" -> "\uD83D\uDD34";
            default -> "\u26AA";
        };
        return emoji + " " + task.toString();
    }

    private String getDifficultyClass(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> "easy-task";
            case "medium" -> "medium-task";
            case "hard" -> "hard-task";
            default -> "default-task";
        };
    }

    private void loadTasksFromDB() {
        List<Task> tasks = Database.getAllTasks();
        for (Task task : tasks) {
            addTaskToUI(task);
            if (task.isDone()) {
                updateXP(task.getXpReward());
            }
        }
    }

    private void addTaskToUI(Task task) {
        CheckBox checkBox = new CheckBox();
        Label taskLabel = new Label(getStyledText(task));
        taskLabel.getStyleClass().add(getDifficultyClass(task.getDifficulty()));
        checkBox.setSelected(task.isDone());

        checkBox.setOnAction(event -> {
            if (checkBox.isSelected() && !task.isDone()) {
                task.setDone(true);
                updateXP(task.getXpReward());
                taskLabel.setText(getStyledText(task));
                Database.markTaskDone(task.getId());
            }
        });

        HBox taskBox = new HBox(10, checkBox, taskLabel);
        taskBox.getStyleClass().add("task-box");

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("\uD83D\uDDD1 Delete Task");
        deleteItem.setOnAction(e -> {
            Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDelete.setTitle("Delete Task");
            confirmDelete.setHeaderText("Are you sure you want to delete this task?");
            confirmDelete.setContentText(task.getTitle());
            confirmDelete.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK) {
                    Database.deleteTaskById(task.getId());
                    taskListView.getItems().remove(taskBox);
                }
            });
        });
        contextMenu.getItems().add(deleteItem);
        taskBox.setOnContextMenuRequested(ev -> contextMenu.show(taskBox, ev.getScreenX(), ev.getScreenY()));

        taskListView.getItems().add(taskBox);
    }

    @Override
    public void start(Stage primaryStage) {
        Label title = new Label("\uD83C\uDFC2 Gamified To-Do List");
        title.getStyleClass().add("title");
        xpLabel.getStyleClass().add("xp");

        xpProgressBar.setPrefWidth(250);
        xpProgressBar.getStyleClass().add("xp-bar");

        xpPercentageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
        HBox xpBox = new HBox(10, xpProgressBar, xpPercentageLabel);

        rewardsPane.setHgap(10);
        rewardsPane.setVgap(10);
        rewardsPane.setPadding(new Insets(10));

        TextField taskField = new TextField();
        taskField.setPromptText("Enter task title");

        ComboBox<String> difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll("Easy", "Medium", "Hard");
        difficultyCombo.setValue("Medium");

        Button addButton = new Button("Add Task");
        Button resetButton = new Button("\uD83D\uDDD1️ Reset Progress");
        ToggleButton themeToggle = new ToggleButton("\uD83C\uDF19 Dark Mode");

        root = new VBox(10, title, xpLabel, xpBox, rewardsPane, taskListView, taskField, difficultyCombo, addButton, resetButton, themeToggle, confettiPane);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root");

        Scene scene = new Scene(root, 400, 700);

        URL lightThemeURL = getClass().getResource("/style.css");
        URL darkThemeURL = getClass().getResource("/dark.css");

        if (lightThemeURL != null) {
            scene.getStylesheets().add(lightThemeURL.toExternalForm());
        }

        themeToggle.setOnAction(e -> {
            scene.getStylesheets().clear();
            if (themeToggle.isSelected()) {
                if (darkThemeURL != null) {
                    scene.getStylesheets().add(darkThemeURL.toExternalForm());
                    themeToggle.setText("\u2600\uFE0F Light Mode");
                }
            } else {
                if (lightThemeURL != null) {
                    scene.getStylesheets().add(lightThemeURL.toExternalForm());
                    themeToggle.setText("\uD83C\uDF19 Dark Mode");
                }
            }
        });

        addButton.setOnAction(e -> {
            String taskTitle = taskField.getText();
            String difficulty = difficultyCombo.getValue();
            if (!taskTitle.isEmpty()) {
                Task newTask = new Task(taskTitle, difficulty);
                Database.saveTask(newTask);
                addTaskToUI(newTask);
                taskField.clear();
            }
        });

        resetButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Reset");
            confirm.setHeaderText("Are you sure?");
            confirm.setContentText("This will delete ALL tasks and reset your XP. Continue?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Database.deleteAllTasks();
                    taskListView.getItems().clear();
                    totalXP = 0;
                    currentLevel = 1;
                    earnedRewards.clear();
                    rewardsPane.getChildren().clear();
                    xpProgressBar.setProgress(0);
                    xpLabel.setText("XP: 0 | Level: 1");
                    xpPercentageLabel.setText("0%");
                }
            });
        });

        loadTasksFromDB();

        primaryStage.setScene(scene);
        primaryStage.setTitle("Gamified To-Do");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
