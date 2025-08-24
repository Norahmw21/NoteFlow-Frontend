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

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TodosPage extends VBox {

    private final ObservableList<ToDoListDto> tasks = FXCollections.observableArrayList();
    private final TableView<ToDoListDto> table = new TableView<>();

    // Form fields
    private final TextField taskNameField = new TextField();
    private final ComboBox<String> statusBox = new ComboBox<>();
    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();
    private final ComboBox<String> importanceBox = new ComboBox<>();

    // Colors
    private final String primaryColor = "#4623E9";
    private final String secondaryColor = "#EAABF0";
    private final String accentColor = "#DABFFF";
    private final String backgroundColor = "#F5F5F5";
    private final String textColor = "#333333";

    // Importance colors
    private final String highImportanceColor = "#FF5252";  // Red
    private final String normalImportanceColor = "#4CAF50"; // Green
    private final String lowImportanceColor = "#2196F3";   // Blue

    public TodosPage() {
        super(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: " + backgroundColor + ";");

        // Header
        Label headerLabel = new Label("My Tasks");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        headerLabel.setTextFill(Color.web(primaryColor));
        headerLabel.setPadding(new Insets(0, 0, 15, 0));

        initializeTable();
        initializeForm();

        getChildren().addAll(headerLabel, table, createFormLayout());

        // Add selection listener
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        loadTasks();
    }

    private void initializeTable() {
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10;");
        table.setPadding(new Insets(10));

        TableColumn<ToDoListDto, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getTaskId()));
        idCol.setVisible(false); // Hide ID column as it's not needed for display

        TableColumn<ToDoListDto, String> nameCol = new TableColumn<>("Task");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTaskName()));
        nameCol.setMinWidth(200);

        TableColumn<ToDoListDto, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setMinWidth(100);
        statusCol.setCellFactory(column -> new TableCell<ToDoListDto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toUpperCase());
                    if ("active".equalsIgnoreCase(item)) {
                        setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 10; -fx-border-radius: 10; -fx-text-fill: #388E3C; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: #FFEBEE; -fx-background-radius: 10; -fx-border-radius: 10; -fx-text-fill: #D32F2F; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<ToDoListDto, String> importanceCol = new TableColumn<>("Importance");
        importanceCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTaskImportance()));
        importanceCol.setMinWidth(120);
        importanceCol.setCellFactory(column -> new TableCell<ToDoListDto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toUpperCase());
                    switch (item.toLowerCase()) {
                        case "high":
                            setStyle("-fx-background-color: " + highImportanceColor + "; -fx-background-radius: 10; -fx-border-radius: 10; -fx-text-fill: white; -fx-alignment: CENTER; -fx-font-weight: bold;");
                            break;
                        case "normal":
                            setStyle("-fx-background-color: " + normalImportanceColor + "; -fx-background-radius: 10; -fx-border-radius: 10; -fx-text-fill: white; -fx-alignment: CENTER; -fx-font-weight: bold;");
                            break;
                        case "low":
                            setStyle("-fx-background-color: " + lowImportanceColor + "; -fx-background-radius: 10; -fx-border-radius: 10; -fx-text-fill: white; -fx-alignment: CENTER; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        TableColumn<ToDoListDto, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStartDate() != null ? c.getValue().getStartDate().toLocalDate().toString() : ""));
        startDateCol.setMinWidth(100);

        TableColumn<ToDoListDto, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getEndDate() != null ? c.getValue().getEndDate().toLocalDate().toString() : ""));
        endDateCol.setMinWidth(100);

        table.getColumns().addAll(idCol, nameCol, statusCol, importanceCol, startDateCol, endDateCol);
        table.setItems(tasks);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Set row factory to add some padding between rows
        table.setRowFactory(tv -> new TableRow<ToDoListDto>() {
            @Override
            protected void updateItem(ToDoListDto item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setStyle("-fx-border-color: transparent transparent #EEEEEE transparent; -fx-padding: 5 0 5 0;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void initializeForm() {
        taskNameField.setPromptText("Task Name");
        taskNameField.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-padding: 8;");

        statusBox.getItems().addAll("active", "closed");
        statusBox.setPromptText("Select Status");
        statusBox.setStyle("-fx-background-radius: 5; -fx-border-radius: 5;");

        startDatePicker.setPromptText("Start Date");
        startDatePicker.setStyle("-fx-background-radius: 5; -fx-border-radius: 5;");

        endDatePicker.setPromptText("End Date");
        endDatePicker.setStyle("-fx-background-radius: 5; -fx-border-radius: 5;");

        importanceBox.getItems().addAll("high", "normal", "low");
        importanceBox.setPromptText("Select Importance");
        importanceBox.setStyle("-fx-background-radius: 5; -fx-border-radius: 5;");

        // Set cell factory to color the importance options
        importanceBox.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toUpperCase());
                    switch (item.toLowerCase()) {
                        case "high":
                            setStyle("-fx-background-color: " + highImportanceColor + "; -fx-text-fill: white;");
                            break;
                        case "normal":
                            setStyle("-fx-background-color: " + normalImportanceColor + "; -fx-text-fill: white;");
                            break;
                        case "low":
                            setStyle("-fx-background-color: " + lowImportanceColor + "; -fx-text-fill: white;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Set button cell to show colored text
        importanceBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toUpperCase());
                    switch (item.toLowerCase()) {
                        case "high":
                            setStyle("-fx-text-fill: " + highImportanceColor + ";");
                            break;
                        case "normal":
                            setStyle("-fx-text-fill: " + normalImportanceColor + ";");
                            break;
                        case "low":
                            setStyle("-fx-text-fill: " + lowImportanceColor + ";");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }

    private HBox createFormLayout() {
        Button createBtn = createStyledButton("Add Task", primaryColor);
        createBtn.setOnAction(e -> handleCreateTask());

        Button updateBtn = createStyledButton("Update", accentColor);
        updateBtn.setOnAction(e -> handleUpdateTask());

        Button deleteBtn = createStyledButton("Delete", "#FF5252");
        deleteBtn.setOnAction(e -> handleDeleteTask());

        Button clearBtn = createStyledButton("Clear", "#9E9E9E");
        clearBtn.setOnAction(e -> clearForm());

        Button refreshBtn = createStyledButton("Refresh", "#FFC107");
        refreshBtn.setOnAction(e -> loadTasks());

        HBox form = new HBox(10,
                taskNameField, statusBox,
                startDatePicker, endDatePicker,
                importanceBox,
                createBtn, updateBtn, deleteBtn, clearBtn, refreshBtn
        );
        form.setPadding(new Insets(15, 0, 0, 0));
        form.setAlignment(Pos.CENTER_LEFT);

        return form;
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 5; " +
                "-fx-border-radius: 5; " +
                "-fx-padding: 8 15 8 15; " +
                "-fx-font-weight: bold;");

        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: derive(" + color + ", 20%); " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5; " +
                        "-fx-border-radius: 5; " +
                        "-fx-padding: 8 15 8 15; " +
                        "-fx-font-weight: bold;")
        );

        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5; " +
                        "-fx-border-radius: 5; " +
                        "-fx-padding: 8 15 8 15; " +
                        "-fx-font-weight: bold;")
        );

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