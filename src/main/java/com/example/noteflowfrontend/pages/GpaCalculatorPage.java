package com.example.noteflowfrontend.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class GpaCalculatorPage extends VBox {

    // Color scheme from AppShell
    private static final String PRIMARY_LIGHT = "#DABFFF";
    private static final String PRIMARY_DARK = "#4F518C";
    private static final String CARD_BG = "#FFFFFF";
    private static final String TEXT_PRIMARY = "#2C2A4A";
    private static final String TEXT_SECONDARY = "#6C7293";
    private static final String BORDER_COLOR = "#E5E9F2";

    public GpaCalculatorPage() {
        this.setSpacing(20);
        this.setPadding(new Insets(20));
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-background-color: #FAFBFF;");

        // Title
        Text title = new Text("GPA Calculator");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setStyle(String.format("-fx-fill: %s;", TEXT_PRIMARY));

        // Scale selector
        HBox scaleBox = new HBox(15);
        scaleBox.setAlignment(Pos.CENTER_LEFT);
        scaleBox.setPadding(new Insets(0, 0, 10, 0));

        Label scaleLbl = new Label("GPA Scale:");
        scaleLbl.setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 14;", TEXT_PRIMARY));

        ComboBox<String> scaleCombo = new ComboBox<>();
        scaleCombo.getItems().addAll("4.0 Scale", "5.0 Scale");
        scaleCombo.setValue("4.0 Scale");
        scaleCombo.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; -fx-border-radius: 6; -fx-background-radius: 6;",
                CARD_BG, BORDER_COLOR
        ));

        scaleBox.getChildren().addAll(scaleLbl, scaleCombo);

        // Main container with modern styling
        VBox inputContainer = new VBox(15);
        inputContainer.setPadding(new Insets(20));
        inputContainer.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-width: 1;",
                CARD_BG, BORDER_COLOR
        ));

        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(15);
        inputGrid.setVgap(12);
        inputGrid.setPadding(new Insets(10));

        //  headers
        String headerStyle = String.format(
                "-fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 0 0 8 0;",
                PRIMARY_DARK
        );

        Label courseHeader = new Label("COURSE");
        courseHeader.setStyle(headerStyle);
        Label creditsHeader = new Label("CREDITS");
        creditsHeader.setStyle(headerStyle);
        Label gradeHeader = new Label("GRADE");
        gradeHeader.setStyle(headerStyle);

        inputGrid.add(courseHeader, 0, 0);
        inputGrid.add(creditsHeader, 1, 0);
        inputGrid.add(gradeHeader, 2, 0);

        // Add initial rows
        for (int i = 0; i < 4; i++) {
            addCourseRow(inputGrid, i + 1);
        }

        // Modern buttons
        Button addRowButton = new Button("+ Add Course");
        addRowButton.setStyle(String.format(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: %s; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-radius: 6; " +
                        "-fx-border-width: 1; " +
                        "-fx-font-size: 13; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 8 16;",
                PRIMARY_DARK, BORDER_COLOR
        ));

        Button calculateButton = new Button("Calculate GPA");
        calculateButton.setStyle(String.format(
                "-fx-background-color: linear-gradient(from 0%% 0%% to 100%% 0%%, %s 0%%, %s 100%%); " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-font-size: 14; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 10 24;",
                PRIMARY_LIGHT, PRIMARY_DARK
        ));

        HBox buttonBox = new HBox(15, addRowButton, calculateButton);
        buttonBox.setAlignment(Pos.CENTER);


        VBox resultContainer = new VBox(5);
        resultContainer.setAlignment(Pos.CENTER);
        resultContainer.setPadding(new Insets(15));
        resultContainer.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-radius: 8;",
                "#F8FAFF", BORDER_COLOR
        ));

        Text resultText = new Text();
        resultText.setFont(Font.font("System", FontWeight.BOLD, 16));

        Text subtitleText = new Text("Based on your course inputs");
        subtitleText.setStyle(String.format("-fx-fill: %s; -fx-font-size: 12;", TEXT_SECONDARY));

        resultContainer.getChildren().addAll(resultText, subtitleText);
        resultContainer.setVisible(false);

        // Button actions
        addRowButton.setOnAction(e -> {
            int nextRow = inputGrid.getRowCount();
            addCourseRow(inputGrid, nextRow);
        });

        calculateButton.setOnAction(e -> {
            double totalQualityPoints = 0;
            double totalCredits = 0;
            boolean hasError = false;
            boolean hasCourses = false;

            double scale = scaleCombo.getValue().equals("5.0 Scale") ? 5.0 : 4.0;

            for (int row = 1; row < inputGrid.getRowCount(); row++) {
                TextField courseField = (TextField) getNodeAt(inputGrid, 0, row);
                TextField creditsField = (TextField) getNodeAt(inputGrid, 1, row);
                ComboBox<String> gradeCombo = (ComboBox<String>) getNodeAt(inputGrid, 2, row);

                if (courseField == null || creditsField == null || gradeCombo == null) continue;

                String creditsText = creditsField.getText().trim();
                String grade = gradeCombo.getValue();

                // Skip empty rows
                if (creditsText.isEmpty() && (grade == null || grade.isEmpty())) {
                    continue;
                }

                hasCourses = true;

                // Validate input
                if (creditsText.isEmpty() || grade == null || grade.isEmpty()) {
                    showError(resultText, resultContainer, "Please fill in all fields for each course");
                    hasError = true;
                    break;
                }

                try {
                    double credits = Double.parseDouble(creditsText);
                    if (credits <= 0) {
                        showError(resultText, resultContainer, "Credits must be greater than 0");
                        hasError = true;
                        break;
                    }

                    double gradePoints = convertGradeToPoints(grade, scale);
                    totalQualityPoints += credits * gradePoints;
                    totalCredits += credits;

                } catch (NumberFormatException ex) {
                    showError(resultText, resultContainer, "Please enter valid numbers for credits");
                    hasError = true;
                    break;
                }
            }

            if (!hasError) {
                if (hasCourses && totalCredits > 0) {
                    double gpa = totalQualityPoints / totalCredits;
                    showSuccess(resultText, resultContainer,
                            String.format("Your GPA: %.2f/%.1f", gpa, scale));
                } else {
                    showWarning(resultText, resultContainer, "Please enter at least one course with valid data");
                }
            }
        });

        inputContainer.getChildren().addAll(inputGrid, buttonBox);
        this.getChildren().addAll(title, scaleBox, inputContainer, resultContainer);
    }

    private void addCourseRow(GridPane grid, int row) {
        TextField courseField = new TextField();
        courseField.setPromptText("Course Name");
        courseField.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; -fx-border-radius: 4;",
                CARD_BG, BORDER_COLOR
        ));

        TextField creditsField = new TextField();
        creditsField.setPromptText("3");
        creditsField.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; -fx-border-radius: 4;",
                CARD_BG, BORDER_COLOR
        ));

        ComboBox<String> gradeCombo = new ComboBox<>();
        gradeCombo.getItems().addAll("A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F");
        gradeCombo.setPromptText("Select Grade");
        gradeCombo.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; -fx-border-radius: 4;",
                CARD_BG, BORDER_COLOR
        ));

        grid.add(courseField, 0, row);
        grid.add(creditsField, 1, row);
        grid.add(gradeCombo, 2, row);
    }

    private double convertGradeToPoints(String grade, double scale) {
        if (scale == 5.0) {
            // 5.0 scale with A+
            return switch (grade) {
                case "A+" -> 5.0;
                case "A" -> 4.75;
                case "A-" -> 4.5;
                case "B+" -> 4.25;
                case "B" -> 4.0;
                case "B-" -> 3.75;
                case "C+" -> 3.5;
                case "C" -> 3.25;
                case "C-" -> 3.0;
                case "D+" -> 2.5;
                case "D" -> 2.0;
                case "D-" -> 1.5;
                case "F" -> 0.0;
                default -> 0.0;
            };
        } else {
            // Standard 4.0 scale with A+
            return switch (grade) {
                case "A+" -> 4.3;
                case "A" -> 4.0;
                case "A-" -> 3.7;
                case "B+" -> 3.3;
                case "B" -> 3.0;
                case "B-" -> 2.7;
                case "C+" -> 2.3;
                case "C" -> 2.0;
                case "C-" -> 1.7;
                case "D+" -> 1.3;
                case "D" -> 1.0;
                case "D-" -> 0.7;
                case "F" -> 0.0;
                default -> 0.0;
            };
        }
    }

    private javafx.scene.Node getNodeAt(GridPane grid, int col, int row) {
        for (javafx.scene.Node n : grid.getChildren()) {
            Integer c = GridPane.getColumnIndex(n);
            Integer r = GridPane.getRowIndex(n);
            int cc = c == null ? 0 : c;
            int rr = r == null ? 0 : r;
            if (cc == col && rr == row) return n;
        }
        return null;
    }

    private void showError(Text resultText, VBox container, String message) {
        resultText.setText(message);
        resultText.setFill(Color.RED);
        container.setVisible(true);
    }

    private void showSuccess(Text resultText, VBox container, String message) {
        resultText.setText(message);
        resultText.setFill(Color.web(PRIMARY_DARK));
        container.setVisible(true);
    }

    private void showWarning(Text resultText, VBox container, String message) {
        resultText.setText(message);
        resultText.setFill(Color.ORANGE);
        container.setVisible(true);
    }
}