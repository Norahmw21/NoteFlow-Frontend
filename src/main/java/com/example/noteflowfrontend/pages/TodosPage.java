package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.ApiClient;
import com.example.noteflowfrontend.core.JwtUtil;
import com.example.noteflowfrontend.core.dto.ToDoListDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Rectangle;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TodosPage extends BorderPane {

    private final ObservableList<ToDoListDto> tasks = FXCollections.observableArrayList();
    private final TableView<ToDoListDto> table = new TableView<>();

    // Form fields
    private final TextField taskNameField = new TextField();
    private final ComboBox<String> statusBox = new ComboBox<>();
    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();
    private final ComboBox<String> importanceBox = new ComboBox<>();

    // Modern color palette
    private final String primaryColor = "#667eea";
    private final String backgroundColor = "#f8fafc";
    private final String cardBackgroundColor = "#ffffff";
    private final String textColor = "#1a202c";
    private final String subtleTextColor = "#718096";
    private final String borderColor = "#e2e8f0";

    // Status colors
    private final String activeColor = "#48bb78";
    private final String closedColor = "#f56565";

    // Importance colors
    private final String highImportanceColor = "#e53e3e";
    private final String normalImportanceColor = "#38b2ac";
    private final String lowImportanceColor = "#4299e1";

    public TodosPage() {
        setStyle("-fx-background-color: " + backgroundColor + ";");
        setPadding(new Insets(24));

        initializeLayout();
        initializeTable();
        initializeForm();

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        loadTasks();
    }

    private void initializeLayout() {
        // Header Section
        VBox headerSection = createHeaderSection();

        // Main Content Area
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(0));

        // Table Container
        VBox tableContainer = createTableContainer();

        // Form Container
        VBox formContainer = createFormContainer();

        mainContent.getChildren().addAll(tableContainer, formContainer);

        setTop(headerSection);
        setCenter(mainContent);
    }

    private VBox createHeaderSection() {
        VBox headerBox = new VBox(8);
        headerBox.setPadding(new Insets(0, 0, 24, 0));

        Label titleLabel = new Label("Task Manager");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(textColor));

        Label subtitleLabel = new Label("Organize and manage your daily tasks");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.web(subtleTextColor));

        headerBox.getChildren().addAll(titleLabel, subtitleLabel);
        return headerBox;
    }

    private VBox createTableContainer() {
        VBox container = new VBox(16);

        // Table header with actions
        HBox tableHeader = new HBox(12);
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        Label tasksLabel = new Label("Your Tasks");
        tasksLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        tasksLabel.setTextFill(Color.web(textColor));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createModernButton("🔄 Refresh", "#718096", false);
        refreshBtn.setOnAction(e -> loadTasks());

        tableHeader.getChildren().addAll(tasksLabel, spacer, refreshBtn);

        // Table wrapper with shadow
        StackPane tableWrapper = new StackPane();
        tableWrapper.setStyle(
                "-fx-background-color: " + cardBackgroundColor + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 1;"
        );

        // Add subtle shadow
        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setOffsetX(0);
        shadow.setOffsetY(2);
        shadow.setColor(Color.color(0, 0, 0, 0.1));
        tableWrapper.setEffect(shadow);

        tableWrapper.getChildren().add(table);

        container.getChildren().addAll(tableHeader, tableWrapper);
        return container;
    }

    private VBox createFormContainer() {
        VBox container = new VBox(16);

        Label formLabel = new Label("Add or Edit Task");
        formLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        formLabel.setTextFill(Color.web(textColor));

        // Form wrapper with modern styling
        VBox formWrapper = new VBox(20);
        formWrapper.setStyle(
                "-fx-background-color: " + cardBackgroundColor + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 24;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setOffsetX(0);
        shadow.setOffsetY(2);
        shadow.setColor(Color.color(0, 0, 0, 0.1));
        formWrapper.setEffect(shadow);

        // Form fields grid
        GridPane formGrid = createFormGrid();

        // Action buttons
        HBox buttonBox = createButtonBox();

        formWrapper.getChildren().addAll(formGrid, buttonBox);
        container.getChildren().addAll(formLabel, formWrapper);

        return container;
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        // Column constraints for responsive layout
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        col1.setMinWidth(200);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setMinWidth(150);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setHgrow(Priority.ALWAYS);
        col3.setMinWidth(150);

        grid.getColumnConstraints().addAll(col1, col2, col3);

        // Task name (spans 3 columns)
        VBox taskNameBox = createFieldBox("Task Name", taskNameField);
        grid.add(taskNameBox, 0, 0, 3, 1);

        // Status and Importance
        VBox statusBox = createFieldBox("Status", this.statusBox);
        VBox importanceBoxContainer = createFieldBox("Importance", importanceBox);

        grid.add(statusBox, 0, 1);
        grid.add(importanceBoxContainer, 1, 1);

        // Date fields
        VBox startDateBox = createFieldBox("Start Date", startDatePicker);
        VBox endDateBox = createFieldBox("End Date", endDatePicker);

        grid.add(startDateBox, 0, 2);
        grid.add(endDateBox, 1, 2);

        return grid;
    }

    private VBox createFieldBox(String labelText, Control field) {
        VBox box = new VBox(8);

        Label label = new Label(labelText);
        label.setFont(Font.font("System", FontWeight.MEDIUM, 14));
        label.setTextFill(Color.web(textColor));

        styleFormField(field);

        box.getChildren().addAll(label, field);
        return box;
    }

    private void styleFormField(Control field) {
        field.setStyle(
                "-fx-background-color: " + cardBackgroundColor + ";" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12;" +
                        "-fx-font-size: 14;"
        );

        // Focus effects
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                        "-fx-background-color: " + cardBackgroundColor + ";" +
                                "-fx-border-color: " + primaryColor + ";" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 11;" +
                                "-fx-font-size: 14;"
                );
            } else {
                field.setStyle(
                        "-fx-background-color: " + cardBackgroundColor + ";" +
                                "-fx-border-color: " + borderColor + ";" +
                                "-fx-border-width: 1;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 12;" +
                                "-fx-font-size: 14;"
                );
            }
        });
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button createBtn = createModernButton("✓ Add Task", primaryColor, true);
        createBtn.setOnAction(e -> handleCreateTask());

        Button updateBtn = createModernButton("✏ Update", "#f093fb", false);
        updateBtn.setOnAction(e -> handleUpdateTask());

        Button deleteBtn = createModernButton("🗑 Delete", closedColor, false);
        deleteBtn.setOnAction(e -> handleDeleteTask());

        Button clearBtn = createModernButton("↻ Clear", "#718096", false);
        clearBtn.setOnAction(e -> clearForm());

        buttonBox.getChildren().addAll(createBtn, updateBtn, deleteBtn, clearBtn);
        return buttonBox;
    }

    private void initializeTable() {
        table.setStyle("-fx-background-color: transparent;");
        table.setPadding(new Insets(16));
        table.setRowFactory(tv -> createStyledTableRow());

        // Hide table header
        table.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            Pane header = (Pane) table.lookup("TableHeaderRow");
            if (header != null) {
                header.setVisible(false);
                header.setPrefHeight(0);
                header.setMinHeight(0);
                header.setMaxHeight(0);
            }
        });

        TableColumn<ToDoListDto, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getTaskId()));
        idCol.setVisible(false);

        TableColumn<ToDoListDto, String> nameCol = new TableColumn<>("Task");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTaskName()));
        nameCol.setMinWidth(250);
        nameCol.setCellFactory(column -> createTaskNameCell());

        TableColumn<ToDoListDto, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setMinWidth(100);
        statusCol.setCellFactory(column -> createStatusCell());

        TableColumn<ToDoListDto, String> importanceCol = new TableColumn<>("Importance");
        importanceCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTaskImportance()));
        importanceCol.setMinWidth(120);
        importanceCol.setCellFactory(column -> createImportanceCell());

        TableColumn<ToDoListDto, String> datesCol = new TableColumn<>("Duration");
        datesCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(formatDateRange(c.getValue())));
        datesCol.setMinWidth(200);
        datesCol.setCellFactory(column -> createDateCell());

        table.getColumns().addAll(idCol, nameCol, statusCol, importanceCol, datesCol);
        table.setItems(tasks);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private TableRow<ToDoListDto> createStyledTableRow() {
        return new TableRow<ToDoListDto>() {
            @Override
            protected void updateItem(ToDoListDto item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setStyle(
                            "-fx-background-color: transparent;" +
                                    "-fx-border-color: transparent transparent " + borderColor + " transparent;" +
                                    "-fx-border-width: 0 0 1 0;" +
                                    "-fx-padding: 12 8 12 8;"
                    );

                    setOnMouseEntered(e -> setStyle(
                            "-fx-background-color: derive(" + backgroundColor + ", -5%);" +
                                    "-fx-border-color: transparent transparent " + borderColor + " transparent;" +
                                    "-fx-border-width: 0 0 1 0;" +
                                    "-fx-padding: 12 8 12 8;" +
                                    "-fx-background-radius: 6;"
                    ));

                    setOnMouseExited(e -> setStyle(
                            "-fx-background-color: transparent;" +
                                    "-fx-border-color: transparent transparent " + borderColor + " transparent;" +
                                    "-fx-border-width: 0 0 1 0;" +
                                    "-fx-padding: 12 8 12 8;"
                    ));
                } else {
                    setStyle("");
                }
            }
        };
    }

    private TableCell<ToDoListDto, String> createTaskNameCell() {
        return new TableCell<ToDoListDto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox content = new VBox(4);
                    Label taskName = new Label(item);
                    taskName.setFont(Font.font("System", FontWeight.MEDIUM, 15));
                    taskName.setTextFill(Color.web(textColor));

                    content.getChildren().add(taskName);
                    setGraphic(content);
                    setText(null);
                }
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 8;");
            }
        };
    }

    private TableCell<ToDoListDto, String> createStatusCell() {
        return new TableCell<ToDoListDto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(item.toUpperCase());
                    statusLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
                    statusLabel.setPadding(new Insets(4, 8, 4, 8));

                    if ("active".equalsIgnoreCase(item)) {
                        statusLabel.setStyle(
                                "-fx-background-color: " + activeColor + ";" +
                                        "-fx-text-fill: white;" +
                                        "-fx-background-radius: 12;"
                        );
                    } else {
                        statusLabel.setStyle(
                                "-fx-background-color: " + closedColor + ";" +
                                        "-fx-text-fill: white;" +
                                        "-fx-background-radius: 12;"
                        );
                    }

                    setGraphic(statusLabel);
                    setText(null);
                }
                setStyle("-fx-alignment: CENTER; -fx-padding: 8;");
            }
        };
    }

    private TableCell<ToDoListDto, String> createImportanceCell() {
        return new TableCell<ToDoListDto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String emoji = getImportanceEmoji(item);
                    Label importanceLabel = new Label(emoji + " " + item.toUpperCase());
                    importanceLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                    importanceLabel.setPadding(new Insets(4, 8, 4, 8));

                    String color = getImportanceColor(item);
                    importanceLabel.setStyle(
                            "-fx-background-color: " + color + ";" +
                                    "-fx-text-fill: white;" +
                                    "-fx-background-radius: 12;"
                    );

                    setGraphic(importanceLabel);
                    setText(null);
                }
                setStyle("-fx-alignment: CENTER; -fx-padding: 8;");
            }
        };
    }

    private TableCell<ToDoListDto, String> createDateCell() {
        return new TableCell<ToDoListDto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty || item.trim().isEmpty()) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label dateLabel = new Label(item);
                    dateLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
                    dateLabel.setTextFill(Color.web(subtleTextColor));

                    setGraphic(dateLabel);
                    setText(null);
                }
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 8;");
            }
        };
    }

    private String formatDateRange(ToDoListDto task) {
        if (task.getStartDate() == null && task.getEndDate() == null) {
            return "";
        }

        String start = task.getStartDate() != null ? task.getStartDate().toLocalDate().toString() : "Not set";
        String end = task.getEndDate() != null ? task.getEndDate().toLocalDate().toString() : "Not set";

        return start + " → " + end;
    }

    private String getImportanceEmoji(String importance) {
        switch (importance.toLowerCase()) {
            case "high": return "🔥";
            case "normal": return "⚡";
            case "low": return "💧";
            default: return "⚡";
        }
    }

    private String getImportanceColor(String importance) {
        switch (importance.toLowerCase()) {
            case "high": return highImportanceColor;
            case "normal": return normalImportanceColor;
            case "low": return lowImportanceColor;
            default: return normalImportanceColor;
        }
    }

    private void initializeForm() {
        taskNameField.setPromptText("Enter task name...");

        statusBox.getItems().addAll("active", "closed");
        statusBox.setPromptText("Select status");

        startDatePicker.setPromptText("Select start date");
        endDatePicker.setPromptText("Select end date");

        importanceBox.getItems().addAll("high", "normal", "low");
        importanceBox.setPromptText("Select importance level");

        setupImportanceComboBox();
    }

    private void setupImportanceComboBox() {
        importanceBox.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String emoji = getImportanceEmoji(item);
                    setText(emoji + " " + item.toUpperCase());
                    String color = getImportanceColor(item);
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });

        importanceBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String emoji = getImportanceEmoji(item);
                    setText(emoji + " " + item.toUpperCase());
                    String color = getImportanceColor(item);
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
    }

    private Button createModernButton(String text, String color, boolean isPrimary) {
        Button button = new Button(text);

        String baseStyle = isPrimary ?
                "-fx-background-color: linear-gradient(to bottom, " + color + ", derive(" + color + ", -10%));" +
                        "-fx-text-fill: white;" :
                "-fx-background-color: transparent;" +
                        "-fx-border-color: " + color + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-text-fill: " + color + ";";

        button.setStyle(baseStyle +
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;" +
                "-fx-padding: 10 20 10 20;" +
                "-fx-font-weight: " + (isPrimary ? "bold" : "normal") + ";" +
                "-fx-font-size: 13;" +
                "-fx-cursor: hand;"
        );

        // Hover effects
        button.setOnMouseEntered(e -> {
            String hoverStyle = isPrimary ?
                    "-fx-background-color: linear-gradient(to bottom, derive(" + color + ", 10%), " + color + ");" +
                            "-fx-text-fill: white;" :
                    "-fx-background-color: " + color + ";" +
                            "-fx-border-color: " + color + ";" +
                            "-fx-border-width: 1;" +
                            "-fx-text-fill: white;";

            button.setStyle(hoverStyle +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-padding: 10 20 10 20;" +
                    "-fx-font-weight: " + (isPrimary ? "bold" : "normal") + ";" +
                    "-fx-font-size: 13;" +
                    "-fx-cursor: hand;"
            );
        });

        button.setOnMouseExited(e -> {
            button.setStyle(baseStyle +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-padding: 10 20 10 20;" +
                    "-fx-font-weight: " + (isPrimary ? "bold" : "normal") + ";" +
                    "-fx-font-size: 13;" +
                    "-fx-cursor: hand;"
            );
        });

        return button;
    }

    private void handleCreateTask() {
        try {
            if (taskNameField.getText().isEmpty()) {
                showError("Task name is required");
                return;
            }
            if (statusBox.getValue() == null) {
                showError("Status is required");
                return;
            }
            if (importanceBox.getValue() == null) {
                showError("Importance is required");
                return;
            }

            Long uid = JwtUtil.extractUserIdFromBearer();
            if (uid == null) {
                showError("You are not logged in (no userId in JWT).");
                return;
            }

            LocalDateTime startDateTime = startDatePicker.getValue() != null ? startDatePicker.getValue().atStartOfDay() : null;
            LocalDateTime endDateTime = endDatePicker.getValue() != null ? endDatePicker.getValue().atStartOfDay() : null;

            var payload = new java.util.HashMap<String, Object>();
            payload.put("taskName", taskNameField.getText());
            payload.put("status", statusBox.getValue());
            payload.put("startDate", startDateTime);
            payload.put("endDate", endDateTime);
            payload.put("taskImportance", importanceBox.getValue());
            payload.put("user", java.util.Map.of("userId", uid));

            ToDoListDto created = ApiClient.post("/todo", payload, ToDoListDto.class);
            tasks.add(created);
            clearForm();

        } catch (Exception ex) {
            showError("Error creating task: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleUpdateTask() {
        ToDoListDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a task to update");
            return;
        }

        try {
            if (taskNameField.getText().isEmpty()) {
                showError("Task name is required");
                return;
            }
            if (statusBox.getValue() == null) {
                showError("Status is required");
                return;
            }
            if (importanceBox.getValue() == null) {
                showError("Importance is required");
                return;
            }

            Long uid = JwtUtil.extractUserIdFromBearer();
            if (uid == null) {
                showError("You are not logged in (no userId in JWT).");
                return;
            }

            LocalDateTime startDateTime = startDatePicker.getValue() != null ? startDatePicker.getValue().atStartOfDay() : null;
            LocalDateTime endDateTime = endDatePicker.getValue() != null ? endDatePicker.getValue().atStartOfDay() : null;

            var payload = new java.util.HashMap<String, Object>();
            payload.put("taskName", taskNameField.getText());
            payload.put("status", statusBox.getValue());
            payload.put("startDate", startDateTime);
            payload.put("endDate", endDateTime);
            payload.put("taskImportance", importanceBox.getValue());
            payload.put("user", java.util.Map.of("userId", uid));

            ToDoListDto updated = ApiClient.put("/todo/" + selected.getTaskId(), payload, ToDoListDto.class);
            int index = tasks.indexOf(selected);
            tasks.set(index, updated);
            clearForm();

        } catch (Exception ex) {
            showError("Error updating task: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleDeleteTask() {
        ToDoListDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a task to delete");
            return;
        }

        try {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to delete task: " + selected.getTaskName() + "?",
                    ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        Long uid = JwtUtil.extractUserIdFromBearer();
                        if (uid == null) {
                            showError("You are not logged in (no userId in JWT).");
                            return;
                        }

                        ApiClient.delete("/todo/" + selected.getTaskId() + "?userId=" + uid, Void.class);
                        tasks.remove(selected);
                        clearForm();
                    } catch (Exception ex) {
                        showError("Error deleting task: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Exception ex) {
            showError("Error deleting task: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void populateForm(ToDoListDto task) {
        taskNameField.setText(task.getTaskName() != null ? task.getTaskName() : "");
        statusBox.setValue(task.getStatus() != null ? task.getStatus() : "active");

        // Convert LocalDateTime to LocalDate for DatePicker
        if (task.getStartDate() != null) {
            startDatePicker.setValue(task.getStartDate().toLocalDate());
        } else {
            startDatePicker.setValue(null);
        }

        if (task.getEndDate() != null) {
            endDatePicker.setValue(task.getEndDate().toLocalDate());
        } else {
            endDatePicker.setValue(null);
        }

        importanceBox.setValue(task.getTaskImportance() != null ? task.getTaskImportance() : "normal");
    }

    private void clearForm() {
        taskNameField.clear();
        statusBox.setValue(null);
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        importanceBox.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    private void loadTasks() {
        try {
            System.out.println("Attempting to load tasks...");

            Long uid = JwtUtil.extractUserIdFromBearer();
            if (uid == null) {
                showError("You are not logged in (no userId in JWT).");
                return;
            }

            ToDoListDto[] all = ApiClient.get("/todo/user/" + uid, ToDoListDto[].class);
            System.out.println("Successfully loaded " + all.length + " tasks");
            for (ToDoListDto task : all) {
                System.out.println("Task: " + task.getTaskName() +
                        ", Start: " + task.getStartDate() +
                        ", End: " + task.getEndDate());
            }
            tasks.setAll(all);

        } catch (Exception e) {
            System.err.println("Error loading tasks: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading tasks: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }
}