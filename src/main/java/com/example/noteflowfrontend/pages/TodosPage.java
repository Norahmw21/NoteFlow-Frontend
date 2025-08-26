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

import java.time.LocalDateTime;
import java.util.Objects;

public class TodosPage extends BorderPane {

    private final ObservableList<ToDoListDto> tasks = FXCollections.observableArrayList();
    private final TableView<ToDoListDto> table = new TableView<>();

    // Form fields
    private final TextField taskNameField = new TextField();
    private final ComboBox<String> statusBox = new ComboBox<>();
    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();
    private final ComboBox<String> importanceBox = new ComboBox<>();


    // Theme (Cupertino-like)
    private final String bg = "#F7F8FA";                 // app background
    private final String card = "#FFFFFF";               // surfaces
    private final String text = "#111827";               // primary text
    private final String textMute = "#6B7280";           // secondary text
    private final String border = "#E5E7EB";             // subtle border
    private final String shadowRGBA = "rgba(0,0,0,0.08)";

    // Accents & pills
    private final String accent = "#3B82F6";             // blue
    private final String pillActive = "#10B981";         // green
    private final String pillClosed = "#EF4444";         // red
    private final String pillHigh = "#EF4444";
    private final String pillNormal = "#14B8A6";
    private final String pillLow = "#60A5FA";

    // Filter state
    private final ToggleGroup filterGroup = new ToggleGroup();

    public TodosPage() {
        setStyle("-fx-background-color: " + bg + "; -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui; -fx-font-size: 13px;");
        setPadding(new Insets(24));

        setTop(buildHeader());
        setCenter(buildMain());

        initializeTable();
        initializeForm();
        loadTasks();

        // NEW: keep form in sync with selection so Update works
        hookSelectionToForm();
    }

    /* --------------------
     * UI BUILD
     * -------------------- */

    private VBox buildHeader() {
        VBox box = new VBox(12);
        Label title = new Label("Tasks");
        title.setStyle("""
            -fx-font-size: 28px; 
            -fx-text-fill: #1E293B; 
            -fx-font-weight: 600;
            -fx-font-family: 'SF Pro Display', 'Segoe UI', system-ui;
        """);

        Label sub = new Label("Plan, prioritize, and check off your day.");
        sub.setFont(Font.font("System", FontWeight.NORMAL, 14));
        sub.setTextFill(Color.web(textMute));

        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Segmented filter (All / Active / Closed)
        HBox segmented = buildSegmentedControl();




        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = ghostButton("Refresh", "🔄");
        refreshBtn.setOnAction(e -> loadTasks());

        topBar.getChildren().addAll(segmented, spacer, refreshBtn);

        box.getChildren().addAll(title, sub, topBar);
        return box;
    }

    private HBox buildSegmentedControl() {
        ToggleButton all = segment("All");
        ToggleButton active = segment("Active");
        ToggleButton closed = segment("Closed");
        all.setSelected(true);

        all.setOnAction(e -> applyFilter("all"));
        active.setOnAction(e -> applyFilter("active"));
        closed.setOnAction(e -> applyFilter("closed"));

        HBox seg = new HBox(all, active, closed);
        seg.setAlignment(Pos.CENTER_LEFT);
        seg.setPadding(new Insets(2));
        seg.setStyle("""
            -fx-background-color: #F3F4F6;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-padding: 2;
        """);
        seg.setSpacing(2);
        return seg;
    }

    private ToggleButton segment(String label) {
        ToggleButton t = new ToggleButton(label);
        t.setToggleGroup(filterGroup);
        t.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #111827;
            -fx-font-weight: 600;
            -fx-padding: 6 12 6 12;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
        """);
        t.selectedProperty().addListener((o, was, is) -> {
            if (is) {
                t.setStyle("""
                    -fx-background-color: #FFFFFF;
                    -fx-text-fill: #111827;
                    -fx-font-weight: 700;
                    -fx-padding: 6 12 6 12;
                    -fx-background-radius: 8;
                    -fx-border-radius: 8;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 1);
                """);
            } else {
                t.setStyle("""
                    -fx-background-color: transparent;
                    -fx-text-fill: #111827;
                    -fx-font-weight: 600;
                    -fx-padding: 6 12 6 12;
                    -fx-background-radius: 8;
                    -fx-border-radius: 8;
                """);
            }
        });
        return t;
    }

    private VBox buildMain() {
        VBox main = new VBox(20);

        // TABLE CARD
        VBox tableCard = new VBox();
        tableCard.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-background-radius: 14;
            -fx-border-radius: 14;
            -fx-border-color: #E5E7EB;
            -fx-border-width: 1;
            -fx-padding: 8;
        """);
        tableCard.setEffect(dropShadow());
        table.setStyle("-fx-background-color: transparent;");
        table.setPadding(new Insets(8));
        tableCard.getChildren().add(table);

        // FORM CARD
        VBox formCard = new VBox(16);
        formCard.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-background-radius: 14;
            -fx-border-radius: 14;
            -fx-border-color: #E5E7EB;
            -fx-border-width: 1;
            -fx-padding: 20;
        """);
        formCard.setEffect(dropShadow());

        Label formTitle = new Label("Add or Edit Task");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        formTitle.setTextFill(Color.web(text));

        GridPane formGrid = buildFormGrid();

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = cupertinoButton("Add Task", "#F9FAFB", "#F3F4F6");
        addBtn.setOnAction(e -> handleCreateTask());

        Button updateBtn = cupertinoButton("Update", "#F9FAFB", "#F3F4F6");
        updateBtn.setOnAction(e -> handleUpdateTask());

        Button deleteBtn = destructiveButton("Delete");
        deleteBtn.setOnAction(e -> handleDeleteTask());

        Button clearBtn = ghostButton("Clear", "↻");
        clearBtn.setOnAction(e -> clearForm());

        actions.getChildren().addAll(addBtn, updateBtn, deleteBtn, clearBtn);

        formCard.getChildren().addAll(formTitle, formGrid, actions);

        main.getChildren().addAll(tableCard, formCard);
        return main;
    }

    /* --------------------
     * TABLE
     * -------------------- */

    private void initializeTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setItems(tasks);

        // Hide default header
        table.widthProperty().addListener((obs, o, n) -> {
            Pane header = (Pane) table.lookup("TableHeaderRow");
            if (header != null) {
                header.setVisible(false);
                header.setManaged(false);
                header.setPrefHeight(0);
            }
        });

        TableColumn<ToDoListDto, String> nameCol = new TableColumn<>("Task");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                Objects.toString(c.getValue().getTaskName(), "")
        ));
        nameCol.setMinWidth(260);
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }

                VBox v = new VBox(2);
                Label title = new Label(item);
                title.setFont(Font.font("System", FontWeight.SEMI_BOLD, 15));
                title.setTextFill(Color.web(text));

                Label meta = new Label("ID: " + getTableView().getItems().get(getIndex()).getTaskId());
                meta.setTextFill(Color.web(textMute));
                meta.setStyle("-fx-font-size: 11px;");

                v.getChildren().addAll(title, meta);
                setGraphic(v);
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 10 8 10 12; -fx-background-color: transparent;");
            }
        });

        TableColumn<ToDoListDto, String> statusCol = new TableColumn<>("Status");
        statusCol.setMinWidth(120);
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setCellFactory(col -> pillCell(
                s -> s != null && s.equalsIgnoreCase("active") ? pillActive : pillClosed,
                s -> s == null ? "" : s.toUpperCase()
        ));

        TableColumn<ToDoListDto, String> impCol = new TableColumn<>("Importance");
        impCol.setMinWidth(160);
        impCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTaskImportance()));
        impCol.setCellFactory(col -> pillCell(
                s -> switch (s == null ? "" : s.toLowerCase()) {
                    case "high" -> pillHigh;
                    case "low" -> pillLow;
                    default -> pillNormal;
                },
                s -> (s == null ? "" : s).toUpperCase()
        ));

        TableColumn<ToDoListDto, String> dateCol = new TableColumn<>("Duration");
        dateCol.setMinWidth(220);
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(formatDateRange(c.getValue())));
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) { setText(null); setGraphic(null); return; }
                Label l = new Label(item);
                l.setTextFill(Color.web(textMute));
                l.setStyle("-fx-font-size: 12px;");
                setGraphic(l);
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 10 8 10 12;");
            }
        });

        table.getColumns().setAll(nameCol, statusCol, impCol, dateCol);

        // Row hover + divider
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ToDoListDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    String base = "-fx-background-color: transparent; -fx-border-color: transparent transparent " + border + " transparent; -fx-border-width: 0 0 1 0;";
                    setStyle(base);
                    setOnMouseEntered(e -> setStyle("-fx-background-color: #F9FAFB; -fx-border-color: transparent transparent " + border + " transparent; -fx-border-width: 0 0 1 0;"));
                    setOnMouseExited(e -> setStyle(base));
                }
            }
        });
    }

    private <T> TableCell<ToDoListDto, T> pillCell(java.util.function.Function<T, String> colorFn,
                                                   java.util.function.Function<T, String> textFn) {
        return new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label pill = new Label(textFn.apply(item));
                pill.setStyle("""
                    -fx-background-color: %s;
                    -fx-text-fill: white;
                    -fx-background-radius: 999;
                    -fx-padding: 4 10 4 10;
                    -fx-font-weight: 700;
                    -fx-font-size: 11px;
                """.formatted(colorFn.apply(item)));
                setGraphic(pill);
                setStyle("-fx-alignment: CENTER; -fx-padding: 10 8 10 8;");
            }
        };
    }

    /* --------------------
     * FORM
     * -------------------- */

    private GridPane buildFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        ColumnConstraints grow = new ColumnConstraints();
        grow.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(grow, grow);

        addLabeled(grid, 0, 0, "Task Name", taskNameField);
        addLabeled(grid, 1, 0, "Status", statusBox);
        addLabeled(grid, 0, 1, "Start Date", startDatePicker);
        addLabeled(grid, 1, 1, "End Date", endDatePicker);
        addLabeled(grid, 0, 2, "Importance", importanceBox);

        return grid;
    }

    private void addLabeled(GridPane grid, int col, int row, String label, Control field) {
        VBox box = new VBox(6);
        Label l = new Label(label);
        l.setTextFill(Color.web(text));
        l.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        styleField(field);
        box.getChildren().addAll(l, field);
        grid.add(box, col, row);
    }

    private void initializeForm() {
        taskNameField.setPromptText("Enter task name…");

        statusBox.getItems().setAll("active", "closed");
        statusBox.setPromptText("Select status");

        importanceBox.getItems().setAll("high", "normal", "low");
        importanceBox.setPromptText("Select importance");

        startDatePicker.setPromptText("Start date");
        endDatePicker.setPromptText("End date");

        // Colorful choices inside dropdown
        importanceBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item.toUpperCase());
                setStyle("-fx-text-fill: %s; -fx-font-weight: 700;".formatted(
                        switch (item.toLowerCase()) {
                            case "high" -> pillHigh; case "low" -> pillLow; default -> pillNormal;
                        }
                ));
            }
        });
        importanceBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item.toUpperCase());
                setStyle("-fx-text-fill: %s; -fx-font-weight: 700;".formatted(
                        switch (item.toLowerCase()) {
                            case "high" -> pillHigh; case "low" -> pillLow; default -> pillNormal;
                        }
                ));
            }
        });
    }

    // NEW: bind selection -> form fields
    private void hookSelectionToForm() {
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, cur) -> {
            if (cur == null) {
                clearForm();
                return;
            }
            taskNameField.setText(cur.getTaskName());
            statusBox.setValue(cur.getStatus());
            importanceBox.setValue(cur.getTaskImportance());
            startDatePicker.setValue(cur.getStartDate() == null ? null : cur.getStartDate().toLocalDate());
            endDatePicker.setValue(cur.getEndDate() == null ? null : cur.getEndDate().toLocalDate());
        });

        // Also support double-click to focus editing
        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                ToDoListDto cur = table.getSelectionModel().getSelectedItem();
                if (cur != null) {
                    taskNameField.requestFocus();
                    taskNameField.positionCaret(taskNameField.getText() == null ? 0 : taskNameField.getText().length());
                }
            }
        });
    }

    /* --------------------
     * ACTIONS
     * -------------------- */

    private void handleCreateTask() {
        try {
            if (taskNameField.getText().isBlank()) { toastError("Task name is required"); return; }
            if (statusBox.getValue() == null)       { toastError("Status is required"); return; }
            if (importanceBox.getValue() == null)   { toastError("Importance is required"); return; }

            Long uid = JwtUtil.extractUserIdFromBearer();
            if (uid == null) { toastError("You are not logged in (no userId in JWT)."); return; }

            LocalDateTime s = startDatePicker.getValue() == null ? null : startDatePicker.getValue().atStartOfDay();
            LocalDateTime e = endDatePicker.getValue() == null ? null : endDatePicker.getValue().atStartOfDay();

            var payload = new java.util.HashMap<String, Object>();
            payload.put("taskName", taskNameField.getText());
            payload.put("status", statusBox.getValue());
            payload.put("startDate", s);
            payload.put("endDate", e);
            payload.put("taskImportance", importanceBox.getValue());
            payload.put("user", java.util.Map.of("userId", uid));

            ToDoListDto created = ApiClient.post("/todo", payload, ToDoListDto.class);
            tasks.add(0, created);
            clearForm();
        } catch (Exception ex) {
            toastError("Error creating task: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleUpdateTask() {
        ToDoListDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { toastError("Select a task to update"); return; }

        try {
            if (taskNameField.getText().isBlank()) { toastError("Task name is required"); return; }
            if (statusBox.getValue() == null)       { toastError("Status is required"); return; }
            if (importanceBox.getValue() == null)   { toastError("Importance is required"); return; }

            Long uid = JwtUtil.extractUserIdFromBearer();
            if (uid == null) { toastError("You are not logged in (no userId in JWT)."); return; }

            LocalDateTime s = startDatePicker.getValue() == null ? null : startDatePicker.getValue().atStartOfDay();
            LocalDateTime e = endDatePicker.getValue() == null ? null : endDatePicker.getValue().atStartOfDay();

            var payload = new java.util.HashMap<String, Object>();
            payload.put("taskId", selected.getTaskId()); // NEW: include id in body (many APIs require this)
            payload.put("taskName", taskNameField.getText());
            payload.put("status", statusBox.getValue());
            payload.put("startDate", s);
            payload.put("endDate", e);
            payload.put("taskImportance", importanceBox.getValue());
            payload.put("user", java.util.Map.of("userId", uid));

            ToDoListDto updated = ApiClient.put("/todo/" + selected.getTaskId(), payload, ToDoListDto.class);

            int idx = findIndexById(selected.getTaskId());
            if (idx >= 0) {
                tasks.set(idx, updated);
            } else {
                // Fallback: reload if we can’t find it (e.g., filtered different backing instance)
                loadTasks();
            }

            // Keep selection on the updated item (nice UX)
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().select(updated);
            clearForm();
        } catch (Exception ex) {
            toastError("Error updating task: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleDeleteTask() {
        ToDoListDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { toastError("Select a task to delete"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete “" + selected.getTaskName() + "”?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Deletion");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                try {
                    Long uid = JwtUtil.extractUserIdFromBearer();
                    if (uid == null) { toastError("You are not logged in (no userId in JWT)."); return; }
                    ApiClient.delete("/todo/" + selected.getTaskId() + "?userId=" + uid, Void.class);
                    tasks.removeIf(t -> Objects.equals(t.getTaskId(), selected.getTaskId()));
                    clearForm();
                } catch (Exception ex) {
                    toastError("Error deleting task: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
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
            Long uid = JwtUtil.extractUserIdFromBearer();
            if (uid == null) { toastError("You are not logged in (no userId in JWT)."); return; }
            ToDoListDto[] all = ApiClient.get("/todo/user/" + uid, ToDoListDto[].class);
            tasks.setAll(all);
            applyFilter(currentFilterKey());
        } catch (Exception e) {
            toastError("Error loading tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* --------------------
     * HELPERS
     * -------------------- */

    private int findIndexById(Long id) {
        if (id == null) return -1;
        for (int i = 0; i < tasks.size(); i++) {
            if (Objects.equals(tasks.get(i).getTaskId(), id)) return i;
        }
        return -1;
    }

    private String currentFilterKey() {
        Toggle selected = filterGroup.getSelectedToggle();
        if (selected == null) return "all";
        String t = ((ToggleButton) selected).getText().toLowerCase();
        return switch (t) { case "active" -> "active"; case "closed" -> "closed"; default -> "all"; };
    }

    private void applyFilter(String key) {
        if ("all".equals(key)) {
            table.setItems(tasks);
            return;
        }
        table.setItems(tasks.filtered(t -> key.equalsIgnoreCase(t.getStatus())));
    }

    private String formatDateRange(ToDoListDto task) {
        String s = task.getStartDate() == null ? "" : task.getStartDate().toLocalDate().toString();
        String e = task.getEndDate() == null ? "" : task.getEndDate().toLocalDate().toString();
        if (s.isBlank() && e.isBlank()) return "";
        if (s.isBlank()) return "→ " + e;
        if (e.isBlank()) return s + " →";
        return s + " → " + e;
    }

    private void styleField(Control field) {
        field.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-text-fill: #111827;
            -fx-border-color: #E5E7EB;
            -fx-border-width: 1;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-padding: 10;
            -fx-font-size: 13px;
        """);
        field.focusedProperty().addListener((o, was, is) -> {
            if (is) {
                field.setStyle("""
                    -fx-background-color: #FFFFFF;
                    -fx-text-fill: #111827;
                    -fx-border-color: #3B82F6;
                    -fx-border-width: 2;
                    -fx-background-radius: 10;
                    -fx-border-radius: 10;
                    -fx-padding: 9;
                    -fx-font-size: 13px;
                """);
            } else {
                field.setStyle("""
                    -fx-background-color: #FFFFFF;
                    -fx-text-fill: #111827;
                    -fx-border-color: #E5E7EB;
                    -fx-border-width: 1;
                    -fx-background-radius: 10;
                    -fx-border-radius: 10;
                    -fx-padding: 10;
                    -fx-font-size: 13px;
                """);
            }
        });
    }

    private DropShadow dropShadow() {
        DropShadow ds = new DropShadow();
        ds.setRadius(8);
        ds.setOffsetX(0);
        ds.setOffsetY(2);
        ds.setColor(Color.color(0,0,0,0.08));
        return ds;
    }

    /* --------------------
     * BUTTONS
     * -------------------- */

    private Button cupertinoButton(String text, String bgColor, String hoverBg) {
        Button b = new Button(text);
        applyCupertinoStyle(b, bgColor, hoverBg, false);
        return b;
    }

    private Button ghostButton(String text, String leadingIcon) {
        Button b = new Button((leadingIcon == null ? "" : leadingIcon + " ") + text);
        b.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-text-fill: #111827;
            -fx-border-color: #E5E7EB;
            -fx-border-width: 1;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-padding: 8 14 8 14;
            -fx-font-weight: 600;
            -fx-font-size: 13px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);
        """);
        b.setOnMouseEntered(e -> b.setStyle("""
            -fx-background-color: #F3F4F6;
            -fx-text-fill: #111827;
            -fx-border-color: #D1D5DB;
            -fx-border-width: 1;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-padding: 8 14 8 14;
            -fx-font-weight: 600;
            -fx-font-size: 13px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 6, 0, 0, 2);
        """));
        b.setOnMouseExited(e -> b.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-text-fill: #111827;
            -fx-border-color: #E5E7EB;
            -fx-border-width: 1;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-padding: 8 14 8 14;
            -fx-font-weight: 600;
            -fx-font-size: 13px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);
        """));
        return b;
    }

    private Button destructiveButton(String label) {
        Button b = new Button(label);
        b.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-text-fill: #111827;
            -fx-border-color: #FCA5A5;
            -fx-border-width: 1;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-padding: 8 14 8 14;
            -fx-font-weight: 700;
            -fx-font-size: 13px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 3, 0, 0, 1);
        """);
        b.setOnMouseEntered(e -> b.setStyle("""
            -fx-background-color: #FEF2F2;
            -fx-text-fill: #991B1B;
            -fx-border-color: #EF4444;
            -fx-border-width: 1;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-padding: 8 14 8 14;
            -fx-font-weight: 800;
            -fx-font-size: 13px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);
        """));
        b.setOnMouseExited(e -> b.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-text-fill: #111827;
            -fx-border-color: #FCA5A5;
            -fx-border-width: 1;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-padding: 8 14 8 14;
            -fx-font-weight: 700;
            -fx-font-size: 13px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 3, 0, 0, 1);
        """));
        return b;
    }

    private void applyCupertinoStyle(Button b, String bgColor, String hoverBg, boolean bold) {
        b.setStyle("""
            -fx-background-color: %s;
            -fx-text-fill: #111827;
            -fx-border-color: #E5E7EB;
            -fx-border-width: 1;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-padding: 8 16 8 16;
            -fx-font-weight: %s;
            -fx-font-size: 13px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);
        """.formatted(bgColor, bold ? "700" : "600"));
        b.setOnMouseEntered(e -> b.setStyle("""
            -fx-background-color: %s;
            -fx-text-fill: #111827;
            -fx-border-color: #D1D5DB;
            -fx-border-width: 1;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-padding: 8 16 8 16;
            -fx-font-weight: %s;
            -fx-font-size: 13px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 6, 0, 0, 2);
        """.formatted(hoverBg, bold ? "700" : "600")));
        b.setOnMouseExited(e -> b.setStyle("""
            -fx-background-color: %s;
            -fx-text-fill: #111827;
            -fx-border-color: #E5E7EB;
            -fx-border-width: 1;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-padding: 8 16 8 16;
            -fx-font-weight: %s;
            -fx-font-size: 13px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);
        """.formatted(bgColor, bold ? "700" : "600")));
    }

    /* --------------------
     * MISC
     * -------------------- */

    private void toastError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Error");
        a.showAndWait();
    }
}
