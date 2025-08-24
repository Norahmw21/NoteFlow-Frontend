package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.NoteApi;
import com.example.noteflowfrontend.core.dto.NoteDto;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.concurrent.Task; // 1. IMPORT TASK
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoritesPage extends BorderPane {
    private final FlowPane cardGrid = new FlowPane(16, 16);

    // 2. CREATE A REUSABLE THREAD POOL FOR BACKGROUND TASKS
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true); // Allows the app to exit even if this thread is running
        return t;
    });

    public FavoritesPage() {
        setPadding(new Insets(24));
        setStyle("-fx-background-color: linear-gradient(to bottom, #F8FAFC, #F1F5F9);");
        setupHeader();
        setupMainContent();
        reload();
    }

    // =======================================================================
    // REWRITTEN METHODS USING TASK
    // =======================================================================

    private void reload() {
        cardGrid.getChildren().clear();
        cardGrid.getChildren().add(createLoadingIndicator());

        // 3. CREATE A TASK FOR LOADING NOTES
        Task<List<NoteDto>> loadFavoritesTask = new Task<>() {
            @Override
            protected List<NoteDto> call() throws Exception {
                // This runs on a background thread
                return NoteApi.listFavorites();
            }
        };

        // 4. DEFINE WHAT HAPPENS ON SUCCESS (runs on FX thread)
        loadFavoritesTask.setOnSucceeded(event -> {
            List<NoteDto> notes = loadFavoritesTask.getValue();
            cardGrid.getChildren().clear();
            if (notes.isEmpty()) {
                showEmptyState();
            } else {
                notes.forEach(n -> {
                    VBox noteCard = createNoteCard(n);
                    cardGrid.getChildren().add(noteCard);
                    // Add subtle entrance animation
                    FadeTransition fade = new FadeTransition(Duration.millis(200), noteCard);
                    fade.setFromValue(0.0);
                    fade.setToValue(1.0);
                    fade.play();
                });
            }
        });

        // 5. DEFINE WHAT HAPPENS ON FAILURE (runs on FX thread)
        loadFavoritesTask.setOnFailed(event -> {
            cardGrid.getChildren().clear();
            showErrorState(loadFavoritesTask.getException());
        });

        // 6. SUBMIT THE TASK TO THE EXECUTOR
        executorService.submit(loadFavoritesTask);
    }

    private void removeFromFavorites(NoteDto note) {
        // 7. CREATE A TASK FOR THE UNFAVORITE ACTION
        Task<Void> unfavoriteTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // This runs on a background thread
                NoteApi.setFavorite(note.id(), false);
                return null;
            }
        };

        // 8. ON SUCCESS, RELOAD THE LIST (runs on FX thread)
        unfavoriteTask.setOnSucceeded(event -> reload());

        // 9. ON FAILURE, SHOW AN ERROR (runs on FX thread)
        unfavoriteTask.setOnFailed(event -> showErr(unfavoriteTask.getException()));

        // 10. SUBMIT THE TASK
        executorService.submit(unfavoriteTask);
    }

    // =======================================================================
    // MINOR CHANGE IN createNoteCard TO USE THE NEW TASK-BASED METHOD
    // =======================================================================

    private VBox createNoteCard(NoteDto note) {
        // ... (most of the method is unchanged)
        Label title = new Label(note.title() == null ? "Untitled Note" : note.title());
        title.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: #1E293B;
            -fx-font-weight: 600;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
            -fx-wrap-text: true;
        """);
        title.setMaxWidth(180);

        String typeIcon = note.drawingJson() != null ? "Drawing" : "Text Note";
        Label typeLabel = new Label(typeIcon);
        typeLabel.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #64748B;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);

        Label favLabel = new Label("★");
        favLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: #F59E0B;
        """);

        Button menuBtn = new Button("⋯");
        menuBtn.setStyle("""
            -fx-background-color: #F8FAFC;
            -fx-text-fill: #64748B;
            -fx-padding: 8px 12px;
            -fx-background-radius: 8px;
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-border-color: #E2E8F0;
            -fx-border-width: 1px;
            -fx-border-radius: 8px;
            -fx-cursor: hand;
        """);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem openItem = new MenuItem("Open");
        openItem.setStyle("-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;");
        openItem.setOnAction(e -> openEditor(note));

        MenuItem unfavItem = new MenuItem("Remove from Favorites");
        unfavItem.setStyle("-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;");
        // 11. THIS IS THE ONLY CHANGE IN THIS METHOD
        unfavItem.setOnAction(e -> removeFromFavorites(note));

        contextMenu.getItems().addAll(openItem, unfavItem);

        menuBtn.setOnMouseEntered(e -> menuBtn.setStyle(menuBtn.getStyle().replace("#F8FAFC", "#F1F5F9")));
        menuBtn.setOnMouseExited(e -> menuBtn.setStyle(menuBtn.getStyle().replace("#F1F5F9", "#F8FAFC")));
        menuBtn.setOnAction(e -> {
            animateButtonPress(menuBtn);
            contextMenu.show(menuBtn, javafx.geometry.Side.BOTTOM, 0, 0);
        });

        HBox titleRow = new HBox(8, title, favLabel);
        titleRow.setAlignment(Pos.TOP_LEFT);
        VBox leftContent = new VBox(12, titleRow, typeLabel);
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
            -fx-background-color: #FFFFFF;
            -fx-background-radius: 16px;
            -fx-border-color: #E2E8F0;
            -fx-border-width: 1px;
            -fx-border-radius: 16px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);
        """);

        content.setOnMouseEntered(e -> content.setStyle(content.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 20, 0, 0, 8);"));
        content.setOnMouseExited(e -> content.setStyle(content.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 20, 0, 0, 8);", "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);")));

        return new VBox(content);
    }

    // ... (The rest of the file is unchanged and correct)
    private void setupHeader() {
        Label title = new Label("Favorite Notes");
        title.setStyle("""
            -fx-font-size: 28px; 
            -fx-text-fill: #1E293B; 
            -fx-font-weight: 600;
            -fx-font-family: 'SF Pro Display', 'Segoe UI', system-ui;
        """);
        Label subtitle = new Label("Your starred notes for quick access");
        subtitle.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #64748B;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);
        VBox titleBox = new VBox(4, title, subtitle);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Button refreshBtn = createModernButton("Refresh", "#6B7280", "#4B5563");
        refreshBtn.setOnAction(e -> {
            animateButtonPress(refreshBtn);
            reload();
        });
        HBox buttonGroup = new HBox(refreshBtn);
        buttonGroup.setAlignment(Pos.CENTER_RIGHT);
        var topSection = new HBox(24, titleBox, new Region(), buttonGroup);
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
        scroller.setStyle("""
            -fx-background: transparent;
            -fx-background-color: transparent;
            -fx-border-color: transparent;
        """);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        setCenter(scroller);
    }

    private void openEditor(NoteDto note) {
        var dialog = new Dialog<Void>();
        dialog.setTitle("Edit Note");
        var editor = new NoteEditorPage(note, note.drawingJson() != null ? "draw" : "text");
        dialog.getDialogPane().setContent(editor);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefSize(1000, 760);
        dialog.getDialogPane().setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-background-radius: 16px;
        """);
        dialog.showAndWait();
        reload();
    }

    private Button createModernButton(String text, String bgColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-padding: 10px 16px;
            -fx-background-radius: 8px;
            -fx-font-size: 14px;
            -fx-font-weight: 600;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);
        """, bgColor));
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace(bgColor, hoverColor)));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace(hoverColor, bgColor)));
        return btn;
    }

    private void animateButtonPress(Button btn) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(100), btn);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(0.95);
        scale.setToY(0.95);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }

    private Label createLoadingIndicator() {
        Label loading = new Label("Loading your favorite notes...");
        loading.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: #64748B;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);
        loading.setAlignment(Pos.CENTER);
        return loading;
    }

    private void showEmptyState() {
        VBox emptyState = new VBox(16);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(60));
        emptyState.setPrefWidth(400);
        Label icon = new Label("⭐");
        icon.setStyle("-fx-font-size: 48px;");
        Label title = new Label("No favorite notes yet");
        title.setStyle("""
            -fx-font-size: 20px;
            -fx-text-fill: #374151;
            -fx-font-weight: 600;
            -fx-font-family: 'SF Pro Display', 'Segoe UI', system-ui;
        """);
        Label subtitle = new Label("Mark notes as favorites to see them here");
        subtitle.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #6B7280;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);
        emptyState.getChildren().addAll(icon, title, subtitle);
        cardGrid.getChildren().add(emptyState);
    }

    private void showErrorState(Throwable ex) {
        VBox errorState = new VBox(16);
        errorState.setAlignment(Pos.CENTER);
        errorState.setPadding(new Insets(60));
        errorState.setPrefWidth(400);
        Label icon = new Label("⚠️");
        icon.setStyle("-fx-font-size: 48px;");
        Label title = new Label("Something went wrong");
        title.setStyle("""
            -fx-font-size: 20px;
            -fx-text-fill: #374151;
            -fx-font-weight: 600;
            -fx-font-family: 'SF Pro Display', 'Segoe UI', system-ui;
        """);
        Label subtitle = new Label("Failed to load favorite notes: " + ex.getMessage());
        subtitle.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #6B7280;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);
        Button retryBtn = createModernButton("Try Again", "#3B82F6", "#2563EB");
        retryBtn.setOnAction(e -> {
            animateButtonPress(retryBtn);
            reload();
        });
        errorState.getChildren().addAll(icon, title, subtitle, retryBtn);
        cardGrid.getChildren().add(errorState);
    }

    private void showErr(Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(ex.getMessage());
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);
        alert.showAndWait();
    }
}
