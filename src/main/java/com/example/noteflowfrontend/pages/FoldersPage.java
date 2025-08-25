package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.NoteApi;
import com.example.noteflowfrontend.core.dto.NoteDto;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class FoldersPage extends BorderPane {
    private final FlowPane cardGrid = new FlowPane(16, 16);
    private final TextField search = new TextField();
    private final Button newTextBtn = new Button("✎ New Note");
    private final Button newDrawBtn = new Button("🎨 New Drawing");

    // NEW: filters
    private final TextField tagNameFilter = new TextField();
    private final ColorPicker tagColorFilter = new ColorPicker();
    private final Button clearFiltersBtn = new Button("Clear");

    private List<NoteDto> notes = new ArrayList<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    public FoldersPage() {
        setPadding(new Insets(24));
        setStyle("-fx-background-color: linear-gradient(to bottom, #F8FAFC, #F1F5F9);");
        setupHeader();
        setupMainContent();
        setupEventHandlers();
        reload();
    }

    private void reload() {
        cardGrid.getChildren().clear();
        cardGrid.getChildren().add(createLoadingIndicator());

        Task<List<NoteDto>> loadNotesTask = new Task<>() {
            @Override
            protected List<NoteDto> call() throws Exception {
                String name = tagNameFilter.getText();
                String color = (tagColorFilter.getValue() == null) ? null : toHex(tagColorFilter.getValue());
                return NoteApi.list(name, color);
            }
        };

        loadNotesTask.setOnSucceeded(event -> {
            notes = loadNotesTask.getValue();
            render();
        });

        loadNotesTask.setOnFailed(event -> {
            cardGrid.getChildren().clear();
            showErrorState(loadNotesTask.getException());
        });

        executorService.submit(loadNotesTask);
    }

    private void performNoteAction(NoteAction action, NoteDto note) {
        Task<Void> noteActionTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                switch (action) {
                    case TOGGLE_FAVORITE -> NoteApi.setFavorite(note.id(), !note.favorite());
                    case MOVE_TO_TRASH -> NoteApi.setTrashed(note.id(), true);
                }
                return null;
            }
        };
        noteActionTask.setOnSucceeded(event -> reload());
        noteActionTask.setOnFailed(event -> showErr(noteActionTask.getException()));
        executorService.submit(noteActionTask);
    }

    private enum NoteAction {
        TOGGLE_FAVORITE,
        MOVE_TO_TRASH
    }

    private void render() {
        cardGrid.getChildren().clear();
        String q = search.getText() == null ? "" : search.getText().trim().toLowerCase();

        List<NoteDto> filteredNotes = notes.stream()
                .filter(n -> q.isEmpty() ||
                        (n.title() != null && n.title().toLowerCase().contains(q)) ||
                        (n.textHtml() != null && n.textHtml().toLowerCase().contains(q)))
                .collect(Collectors.toList());

        if (filteredNotes.isEmpty()) {
            if (q.isEmpty()) {
                showEmptyState();
            } else {
                showNoResultsState(q);
            }
        } else {
            for (var note : filteredNotes) {
                VBox noteCard = createNoteCard(note);
                cardGrid.getChildren().add(noteCard);
                FadeTransition fade = new FadeTransition(Duration.millis(200), noteCard);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                fade.play();
            }
        }
    }

    private VBox createNoteCard(NoteDto note) {
        Label title = new Label(note.title() == null ? "Untitled Note" : note.title());
        title.setStyle("""
            -fx-font-size: 16px; -fx-text-fill: #1E293B; -fx-font-weight: 600;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui; -fx-wrap-text: true;
        """);
        title.setMaxWidth(180);

        String typeIcon = note.drawingJson() != null ? "🎨" : "📝";
        String typeText = note.drawingJson() != null ? "Drawing" : "Text Note";
        Label typeLabel = new Label(typeIcon + " " + typeText);
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B; -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;");

        Label favLabel = new Label(note.favorite() ? "⭐" : "");
        favLabel.setStyle("-fx-font-size: 14px;");

        Button menuBtn = new Button("⋯");
        menuBtn.setStyle("""
            -fx-background-color: #F8FAFC; -fx-text-fill: #64748B; -fx-padding: 8px 12px;
            -fx-background-radius: 8px; -fx-font-size: 16px; -fx-font-weight: bold;
            -fx-border-color: #E2E8F0; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-cursor: hand;
        """);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem openItem = new MenuItem("📖 Open");
        openItem.setStyle("-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;");
        openItem.setOnAction(e -> openEditor(note, note.drawingJson() != null ? "draw" : "text"));

        MenuItem favItem = new MenuItem(note.favorite() ? "⭐ Unfavorite" : "☆ Favorite");
        favItem.setStyle("-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;");
        favItem.setOnAction(e -> performNoteAction(NoteAction.TOGGLE_FAVORITE, note));

        MenuItem deleteItem = new MenuItem("❌ Delete");
        deleteItem.setStyle("-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;");
        deleteItem.setOnAction(e -> performNoteAction(NoteAction.MOVE_TO_TRASH, note));

        contextMenu.getItems().addAll(openItem, favItem, deleteItem);

        menuBtn.setOnMouseEntered(e -> menuBtn.setStyle(menuBtn.getStyle().replace("#F8FAFC", "#F1F5F9")));
        menuBtn.setOnMouseExited(e -> menuBtn.setStyle(menuBtn.getStyle().replace("#F1F5F9", "#F8FAFC")));
        menuBtn.setOnAction(e -> {
            animateButtonPress(menuBtn);
            contextMenu.show(menuBtn, javafx.geometry.Side.BOTTOM, 0, 0);
        });

        // NEW: tag chip
        String chipBg = (note.tagColor() == null || note.tagColor().isBlank()) ? "#E5E7EB" : note.tagColor();
        String chipText = (note.tagName() == null || note.tagName().isBlank()) ? "Untagged" : note.tagName();

        Label tagChip = new Label(chipText);
        tagChip.setStyle(
                "-fx-background-color: " + chipBg + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 4px 8px;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: 600;"
        );


        HBox titleRow = new HBox(8, title, favLabel);
        titleRow.setAlignment(Pos.TOP_LEFT);
        VBox leftContent = new VBox(12, titleRow, typeLabel, tagChip);
        leftContent.setAlignment(Pos.TOP_LEFT);
        HBox topRow = new HBox(leftContent, new Region(), menuBtn);
        topRow.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(topRow.getChildren().get(1), Priority.ALWAYS);

        VBox content = new VBox(topRow);
        content.setPadding(new Insets(20));
        content.setPrefSize(220, 220);
        content.setMinSize(220, 220);
        content.setMaxSize(220, 220);
        content.setStyle("""
            -fx-background-color: #FFFFFF; -fx-background-radius: 16px; -fx-border-color: #E2E8F0;
            -fx-border-width: 1px; -fx-border-radius: 16px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);
        """);

        content.setOnMouseEntered(e -> content.setStyle(content.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 20, 0, 0, 8);"));
        content.setOnMouseExited(e -> content.setStyle(content.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 20, 0, 0, 8);", "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);")));

        return new VBox(content);
    }

    private void setupHeader() {
        search.setPromptText("🔍 Search your notes...");
        search.setStyle("""
            -fx-background-color: #FFFFFF; -fx-border-color: transparent; -fx-border-radius: 12px;
            -fx-background-radius: 12px; -fx-padding: 12px 16px; -fx-font-size: 14px;
            -fx-prompt-text-fill: #94A3B8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);
        """);
        search.setPrefWidth(300);
        styleModernButton(newTextBtn, "#3B82F6", "#2563EB");
        styleModernButton(newDrawBtn, "#8B5CF6", "#7C3AED");

        // NEW: filters UI
        tagNameFilter.setPromptText("Tag name");
        tagNameFilter.setPrefWidth(160);
        tagColorFilter.setPromptText("Tag color");
        tagColorFilter.setValue(null); // no default
        styleModernButton(clearFiltersBtn, "#9CA3AF", "#6B7280");

        Label title = new Label("My Notes");
        title.setStyle("""
            -fx-font-size: 28px; -fx-text-fill: #1E293B; -fx-font-weight: 600;
            -fx-font-family: 'SF Pro Display', 'Segoe UI', system-ui;
        """);
        Label subtitle = new Label("Organize your thoughts beautifully");
        subtitle.setStyle("""
            -fx-font-size: 14px; -fx-text-fill: #64748B;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);
        VBox titleBox = new VBox(4, title, subtitle);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        HBox filterBox = new HBox(8, new Label("Filter:"), tagNameFilter, tagColorFilter, clearFiltersBtn);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        HBox buttonGroup = new HBox(12, newTextBtn, newDrawBtn);
        buttonGroup.setAlignment(Pos.CENTER_RIGHT);

        var topSection = new HBox(24, titleBox, new Region(), search, filterBox, buttonGroup);
        topSection.setAlignment(Pos.CENTER_LEFT);
        topSection.setPadding(new Insets(0, 0, 24, 0));
        HBox.setHgrow(topSection.getChildren().get(1), Priority.ALWAYS);
        setTop(topSection);
    }

    private void setupMainContent() {
        cardGrid.setPadding(new Insets(8));
        cardGrid.setAlignment(Pos.TOP_LEFT);
        cardGrid.setPrefWrapLength(0);
        var scroller = new ScrollPane(cardGrid);
        scroller.setFitToWidth(true);
        scroller.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        setCenter(scroller);
    }

    private void setupEventHandlers() {
        newTextBtn.setOnAction(e -> {
            animateButtonPress(newTextBtn);
            openEditor(null, "text");
        });
        newDrawBtn.setOnAction(e -> {
            animateButtonPress(newDrawBtn);
            openEditor(null, "draw");
        });
        search.textProperty().addListener((o, a, b) -> render());
        search.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                search.setStyle(search.getStyle() + "-fx-border-color: #3B82F6; -fx-border-width: 2px;");
            } else {
                search.setStyle(search.getStyle().replace("-fx-border-color: #3B82F6; -fx-border-width: 2px;", ""));
            }
        });

        // NEW: filter interactions
        tagNameFilter.textProperty().addListener((o, a, b) -> reload());
        tagColorFilter.valueProperty().addListener((o, a, b) -> reload());
        clearFiltersBtn.setOnAction(e -> {
            tagNameFilter.clear();
            tagColorFilter.setValue(null);
            reload();
        });
    }

    private void styleModernButton(Button btn, String bgColor, String hoverColor) {
        btn.setStyle(String.format("""
            -fx-background-color: %s; -fx-text-fill: white; -fx-padding: 12px 20px;
            -fx-background-radius: 12px; -fx-font-weight: 600; -fx-font-size: 14px;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui; -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);
        """, bgColor));
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace(bgColor, hoverColor)));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace(hoverColor, bgColor)));
    }

    private void animateButtonPress(Button btn) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(100), btn);
        scale.setFromX(1.0); scale.setFromY(1.0); scale.setToX(0.95); scale.setToY(0.95);
        scale.setAutoReverse(true); scale.setCycleCount(2); scale.play();
    }

    private Label createLoadingIndicator() {
        Label loading = new Label("Loading your notes...");
        loading.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748B; -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;");
        loading.setAlignment(Pos.CENTER);
        return loading;
    }

    private void showEmptyState() {
        VBox emptyState = new VBox(16);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(60));
        emptyState.setPrefWidth(400);
        Label icon = new Label("📝");
        icon.setStyle("-fx-font-size: 48px;");
        Label title = new Label("No notes yet");
        title.setStyle("-fx-font-size: 20px; -fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-family: 'SF Pro Display', 'Segoe UI', system-ui;");
        Label subtitle = new Label("Create your first note to get started");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;");
        emptyState.getChildren().addAll(icon, title, subtitle);
        cardGrid.getChildren().add(emptyState);
    }

    private void showNoResultsState(String query) {
        VBox noResults = new VBox(16);
        noResults.setAlignment(Pos.CENTER);
        noResults.setPadding(new Insets(60));
        noResults.setPrefWidth(400);
        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 48px;");
        Label title = new Label("No results found");
        title.setStyle("-fx-font-size: 20px; -fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-family: 'SF Pro Display', 'Segoe UI', system-ui;");
        Label subtitle = new Label("Try searching with different keywords");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;");
        noResults.getChildren().addAll(icon, title, subtitle);
        cardGrid.getChildren().add(noResults);
    }

    private void showErrorState(Throwable ex) {
        VBox errorState = new VBox(16);
        errorState.setAlignment(Pos.CENTER);
        errorState.setPadding(new Insets(60));
        errorState.setPrefWidth(400);
        Label icon = new Label("⚠️");
        icon.setStyle("-fx-font-size: 48px;");
        Label title = new Label("Something went wrong");
        title.setStyle("-fx-font-size: 20px; -fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-family: 'SF Pro Display', 'Segoe UI', system-ui;");
        Label subtitle = new Label("Failed to load notes: " + ex.getMessage());
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;");
        Button retryBtn = new Button("Try Again");
        styleModernButton(retryBtn, "#3B82F6", "#2563EB");
        retryBtn.setOnAction(e -> reload());
        errorState.getChildren().addAll(icon, title, subtitle, retryBtn);
        cardGrid.getChildren().add(errorState);
    }

    private void openEditor(NoteDto existing, String startMode) {
        var dialog = new Dialog<Void>();
        dialog.setTitle(existing == null ? "New Note" : "Edit Note");
        var editor = new NoteEditorPage(existing, startMode);
        dialog.getDialogPane().setContent(editor);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefSize(1000, 760);
        dialog.getDialogPane().setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 16px;");
        dialog.showAndWait();
        reload();
    }

    private void showErr(Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.showAndWait();
    }

    private static String toHex(Color c) {
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }
}
