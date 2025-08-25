package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.NoteApi;
import com.example.noteflowfrontend.core.dto.NoteDto;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;

public class TrashPage extends BorderPane {
    private final FlowPane cardGrid = new FlowPane(16, 16);
    private final Label emptyStateLabel = new Label("Trash is empty. Deleted notes will appear here.");

    public TrashPage() {
        setPadding(new Insets(24));
        setStyle("-fx-background-color: linear-gradient(to bottom, #F8FAFC, #F1F5F9);");

        setupHeader();
        setupMainContent();

        reload();
    }

    // --- Helper: always run code on the FX thread ---
    private void runFx(Runnable r) {
        if (Platform.isFxApplicationThread()) r.run();
        else Platform.runLater(r);
    }

    private void setupHeader() {
        // Clean, modern title
        Label title = new Label("Trash");
        title.setStyle("""
            -fx-font-size: 28px; 
            -fx-text-fill: #1E293B; 
            -fx-font-weight: 600;
            -fx-font-family: 'SF Pro Display', 'Segoe UI', system-ui;
        """);

        // Subtitle for context
        Label subtitle = new Label("Notes moved to trash will be permanently deleted after 30 days");
        subtitle.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #64748B;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);

        VBox titleBox = new VBox(4, title, subtitle);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        // Refresh button
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
        // Empty state styling
        emptyStateLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: #94A3B8;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);
        emptyStateLabel.setAlignment(Pos.CENTER);

        cardGrid.setPadding(new Insets(8));
        cardGrid.setAlignment(Pos.TOP_LEFT);
        cardGrid.setPrefWrapLength(0); // Allow wrapping

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

    private void reload() {
        // These UI touches must always be on the FX thread
        runFx(() -> {
            cardGrid.getChildren().clear();
            cardGrid.getChildren().add(createLoadingIndicator());
        });

        new Thread(() -> {
            try {
                List<NoteDto> notes = NoteApi.listTrash();
                Platform.runLater(() -> {
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
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    cardGrid.getChildren().clear();
                    showErrorState(ex);
                });
            }
        }).start();
    }

    private VBox createNoteCard(NoteDto note) {
        // Note title
        Label title = new Label(note.title() == null ? "Untitled Note" : note.title());
        title.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: #1E293B;
            -fx-font-weight: 600;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
            -fx-wrap-text: true;
        """);
        title.setMaxWidth(180);

        // Note type indicator
        String typeIcon = note.drawingJson() != null ? "Drawing" : "Text Note";
        Label typeLabel = new Label(typeIcon);
        typeLabel.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #64748B;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);

        // Three dots menu button
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

        // Context menu
        ContextMenu contextMenu = new ContextMenu();

        MenuItem restoreItem = new MenuItem("Restore");
        restoreItem.setStyle("-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;");
        restoreItem.setOnAction(e -> {
            new Thread(() -> {
                try {
                    NoteApi.setTrashed(note.id(), false);
                    // Call reload from FX thread
                    Platform.runLater(this::reload);
                } catch (Exception ex) {
                    showErr(ex);
                }
            }).start();
        });

        MenuItem deleteForeverItem = new MenuItem("Delete Forever");
        deleteForeverItem.setStyle("-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;");
        deleteForeverItem.setOnAction(e -> {
            new Thread(() -> {
                try {
                    NoteApi.deletePermanent(note.id());
                    // Call reload from FX thread
                    Platform.runLater(this::reload);
                } catch (Exception ex) {
                    showErr(ex);
                }
            }).start();
        });

        contextMenu.getItems().addAll(restoreItem, deleteForeverItem);

        // Menu button hover effects
        menuBtn.setOnMouseEntered(e -> {
            menuBtn.setStyle(menuBtn.getStyle().replace("#F8FAFC", "#F1F5F9"));
        });

        menuBtn.setOnMouseExited(e -> {
            menuBtn.setStyle(menuBtn.getStyle().replace("#F1F5F9", "#F8FAFC"));
        });

        // Show context menu on click
        menuBtn.setOnAction(e -> {
            animateButtonPress(menuBtn);
            contextMenu.show(menuBtn, javafx.geometry.Side.BOTTOM, 0, 0);
        });

        // Layout for square card
        HBox titleRow = new HBox(8, title);
        titleRow.setAlignment(Pos.TOP_LEFT);

        VBox leftContent = new VBox(12, titleRow, typeLabel);
        leftContent.setAlignment(Pos.TOP_LEFT);

        HBox topRow = new HBox(leftContent, new Region(), menuBtn);
        topRow.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(topRow.getChildren().get(1), Priority.ALWAYS);

        VBox content = new VBox(topRow);
        content.setPadding(new Insets(20));
        content.setPrefSize(220, 220);  // Square dimensions
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

        // Hover effect
        content.setOnMouseEntered(e -> {
            content.setStyle(content.getStyle() +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 20, 0, 0, 8);");
        });

        content.setOnMouseExited(e -> {
            content.setStyle(content.getStyle().replace(
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 20, 0, 0, 8);",
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"));
        });

        VBox card = new VBox(content);
        card.setPadding(new Insets(4));
        return card;
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

        btn.setOnMouseEntered(e -> {
            btn.setStyle(btn.getStyle().replace(bgColor, hoverColor));
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(btn.getStyle().replace(hoverColor, bgColor));
        });

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
        Label loading = new Label("Loading trash notes...");
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

        Label icon = new Label("🗑");
        icon.setStyle("-fx-font-size: 48px; -fx-text-fill: #374151;");

        Label title = new Label("Trash is empty");
        title.setStyle("""
            -fx-font-size: 20px;
            -fx-text-fill: #374151;
            -fx-font-weight: 600;
            -fx-font-family: 'SF Pro Display', 'Segoe UI', system-ui;
        """);

        Label subtitle = new Label("Deleted notes will appear here");
        subtitle.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #6B7280;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);

        emptyState.getChildren().addAll(icon, title, subtitle);
        cardGrid.getChildren().add(emptyState);
    }

    private void showErrorState(Exception ex) {
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

        Label subtitle = new Label("Failed to load trash notes: " + ex.getMessage());
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

    private void showErr(Exception ex) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred");
            alert.setContentText(ex.getMessage());

            // Style the alert
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("""
                -fx-background-color: #FFFFFF;
                -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
            """);

            alert.showAndWait();
        });
    }
}
