package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.ApiClient;
import com.example.noteflowfrontend.core.dto.ToDoListDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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

    public TodosPage() {
        super(10);
        setPadding(new Insets(10));

        initializeTable();
        initializeForm();

        getChildren().addAll(table, createFormLayout());

        // Add selection listener
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        loadTasks();
    }

    private void initializeTable() {
        TableColumn<ToDoListDto, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getTaskId()));

        TableColumn<ToDoListDto, String> nameCol = new TableColumn<>("Task");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTaskName()));

        TableColumn<ToDoListDto, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));

        TableColumn<ToDoListDto, String> importanceCol = new TableColumn<>("Importance");
        importanceCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTaskImportance()));

        // Add date columns to see what's coming from backend
        TableColumn<ToDoListDto, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStartDate() != null ? c.getValue().getStartDate().toString() : ""));

        TableColumn<ToDoListDto, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getEndDate() != null ? c.getValue().getEndDate().toString() : ""));

        table.getColumns().addAll(idCol, nameCol, statusCol, importanceCol, startDateCol, endDateCol);
        table.setItems(tasks);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void initializeForm() {
        taskNameField.setPromptText("Task Name");

        statusBox.getItems().addAll("PENDING", "IN_PROGRESS", "DONE");
        statusBox.setPromptText("Select Status");

        startDatePicker.setPromptText("Start Date");
        endDatePicker.setPromptText("End Date");

        importanceBox.getItems().addAll("high", "normal", "low");
        importanceBox.setPromptText("Select Importance");
    }

    private HBox createFormLayout() {
        Button createBtn = new Button("Add Task");
        createBtn.setOnAction(e -> handleCreateTask());

        Button updateBtn = new Button("Update Selected");
        updateBtn.setOnAction(e -> handleUpdateTask());

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setOnAction(e -> handleDeleteTask());

        Button clearBtn = new Button("Clear Form");
        clearBtn.setOnAction(e -> clearForm());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> loadTasks());

        HBox form = new HBox(10,
                taskNameField, statusBox,
                startDatePicker, endDatePicker,
                importanceBox,
                createBtn, updateBtn, deleteBtn, clearBtn, refreshBtn
        );
        form.setPadding(new Insets(10));

        return form;
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

            // Convert LocalDate to LocalDateTime
            LocalDateTime startDateTime = startDatePicker.getValue() != null ?
                    startDatePicker.getValue().atStartOfDay() : null;
            LocalDateTime endDateTime = endDatePicker.getValue() != null ?
                    endDatePicker.getValue().atStartOfDay() : null;

            ToDoListDto dto = new ToDoListDto(
                    null,
                    taskNameField.getText(),
                    statusBox.getValue(),
                    startDateTime,  // Now passing LocalDateTime
                    endDateTime,    // Now passing LocalDateTime
                    importanceBox.getValue(),
                    null
            );

            ToDoListDto created = ApiClient.post("/todo", dto, ToDoListDto.class);
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

            // Convert LocalDate to LocalDateTime
            LocalDateTime startDateTime = startDatePicker.getValue() != null ?
                    startDatePicker.getValue().atStartOfDay() : null;
            LocalDateTime endDateTime = endDatePicker.getValue() != null ?
                    endDatePicker.getValue().atStartOfDay() : null;

            // Update the selected DTO
            selected.setTaskName(taskNameField.getText());
            selected.setStatus(statusBox.getValue());
            selected.setStartDate(startDateTime);  // Now passing LocalDateTime
            selected.setEndDate(endDateTime);      // Now passing LocalDateTime
            selected.setTaskImportance(importanceBox.getValue());

            ToDoListDto updated = ApiClient.put("/todo/" + selected.getTaskId(), selected, ToDoListDto.class);
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
                        // Pass Void.class as the second parameter
                        ApiClient.delete("/todo/" + selected.getTaskId(), Void.class);
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
        statusBox.setValue(task.getStatus() != null ? task.getStatus() : "PENDING");

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
            // First, let's debug what we're getting from the backend
            System.out.println("Attempting to load tasks...");

            ToDoListDto[] all = ApiClient.get("/todo", ToDoListDto[].class);
            System.out.println("Successfully loaded " + all.length + " tasks");

            // Debug: print each task to see the date format
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
