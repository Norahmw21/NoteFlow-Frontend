package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.NoteApi;
import com.example.noteflowfrontend.core.dto.NoteDto;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.web.HTMLEditor;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class NoteEditorPage extends BorderPane {
    private final TextField titleField = new TextField();
    private final HTMLEditor htmlEditor = new HTMLEditor();

    // Drawing
    private final Canvas canvas = new Canvas(600, 400);
    private final ColorPicker colorPicker = new ColorPicker();
    private final Slider sizeSlider = new Slider(1, 30, 3);
    private final ToggleButton eraserBtn = new ToggleButton("Eraser");

    private Long noteId; // null for new
    private String mode = "text"; // or "draw"

    // For a super-simple drawing JSON -> we capture strokes (x,y)
    private final List<String> strokes = new ArrayList<>();

    public NoteEditorPage(NoteDto existing, String startMode) {
        setPadding(new Insets(24));
        setStyle("-fx-background-color: linear-gradient(to bottom, #F8FAFC, #F1F5F9);");

        if (startMode != null) mode = startMode;

        setupHeader(existing);
        setupMainContent();
        setupEventHandlers(existing);

        // Load existing note data
        if (existing != null) {
            loadExistingNote(existing);
        }
    }

    private void setupHeader(NoteDto existing) {
        // Title section
        Label titleLabel = new Label("Note Title");
        titleLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #374151;
            -fx-font-weight: 600;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);

        titleField.setPromptText("Enter your note title...");
        titleField.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-border-color: #E5E7EB;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            -fx-padding: 12px 16px;
            -fx-font-size: 16px;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
            -fx-prompt-text-fill: #9CA3AF;
        """);
        titleField.setPrefWidth(400);

        // Add focus styling to title field
        titleField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                titleField.setStyle(titleField.getStyle() +
                        "-fx-border-color: #3B82F6; -fx-border-width: 2px;");
            } else {
                titleField.setStyle(titleField.getStyle().replace(
                        "-fx-border-color: #3B82F6; -fx-border-width: 2px;", ""));
            }
        });

        VBox titleSection = new VBox(8, titleLabel, titleField);
        titleSection.setAlignment(Pos.CENTER_LEFT);

        // Mode toggle section
        var modeToggle = new ToggleGroup();
        var textToggle = new RadioButton("Text Editor");
        var drawToggle = new RadioButton("Drawing Canvas");

        styleRadioButton(textToggle);
        styleRadioButton(drawToggle);

        textToggle.setToggleGroup(modeToggle);
        drawToggle.setToggleGroup(modeToggle);

        if ("draw".equalsIgnoreCase(mode)) {
            drawToggle.setSelected(true);
        } else {
            textToggle.setSelected(true);
        }

        HBox modeSection = new HBox(16, textToggle, drawToggle);
        modeSection.setAlignment(Pos.CENTER_LEFT);

        // Action buttons with formal styling
        var saveBtn = createFormalButton("Save", "#374151", "#1F2937");
        var favoriteBtn = createFormalButton("Favorite", "#6B7280", "#4B5563");
        var trashBtn = createFormalButton("Delete", "#6B7280", "#4B5563");

        HBox buttonSection = new HBox(12, saveBtn, favoriteBtn, trashBtn);
        buttonSection.setAlignment(Pos.CENTER_RIGHT);

        // Header layout
        HBox topRow = new HBox(24, titleSection, new Region(), modeSection);
        topRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(topRow.getChildren().get(1), Priority.ALWAYS);

        HBox bottomRow = new HBox(new Region(), buttonSection);
        bottomRow.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(bottomRow.getChildren().get(0), Priority.ALWAYS);

        VBox header = new VBox(16, topRow, bottomRow);
        header.setPadding(new Insets(0, 0, 24, 0));

        setTop(header);

        // Store references for event handlers
        saveBtn.setUserData("save");
        favoriteBtn.setUserData("favorite");
        trashBtn.setUserData("trash");
        textToggle.setUserData("text");
        drawToggle.setUserData("draw");
    }

    private void setupMainContent() {
        // Style HTML Editor
        htmlEditor.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-border-color: #E5E7EB;
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);
        """);

        // Text editor pane
        VBox textPane = new VBox(htmlEditor);
        textPane.setPadding(new Insets(8));
        textPane.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-background-radius: 12px;
            -fx-border-color: #E5E7EB;
            -fx-border-width: 1px;
            -fx-border-radius: 12px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);
        """);

        // Drawing pane
        VBox drawPane = new VBox(buildDrawingTools());
        drawPane.setPadding(new Insets(8));
        drawPane.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-background-radius: 12px;
            -fx-border-color: #E5E7EB;
            -fx-border-width: 1px;
            -fx-border-radius: 12px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);
        """);

        var center = new StackPane(textPane, drawPane);
        setCenter(center);

        // Show correct mode
        textPane.setVisible("text".equals(mode));
        textPane.setManaged("text".equals(mode));
        drawPane.setVisible("draw".equals(mode));
        drawPane.setManaged("draw".equals(mode));

        // Store references for mode switching
        center.setUserData(new VBox[]{textPane, drawPane});
    }

    private VBox buildDrawingTools() {
        // Drawing tools header
        Label toolsLabel = new Label("Drawing Tools");
        toolsLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: #374151;
            -fx-font-weight: 600;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);

        // Color picker section
        Label colorLabel = new Label("Color:");
        colorLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #6B7280;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);

        colorPicker.setStyle("""
            -fx-background-radius: 8px;
            -fx-border-radius: 8px;
        """);

        // Size slider section
        Label sizeLabel = new Label("Brush Size:");
        sizeLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #6B7280;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);

        sizeSlider.setStyle("""
            -fx-background-color: #F3F4F6;
            -fx-background-radius: 8px;
        """);
        sizeSlider.setPrefWidth(150);

        Label sizeValue = new Label(String.format("%.0f px", sizeSlider.getValue()));
        sizeValue.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #9CA3AF;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);

        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            sizeValue.setText(String.format("%.0f px", newVal.doubleValue()));
        });

        // Eraser button
        styleToggleButton(eraserBtn);

        // Clear canvas button
        Button clearBtn = createModernButton("Clear Canvas", "#6B7280", "#4B5563");
        clearBtn.setOnAction(e -> {
            animateButtonPress(clearBtn);
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            strokes.clear();
        });

        // Tools layout
        HBox colorSection = new HBox(8, colorLabel, colorPicker);
        colorSection.setAlignment(Pos.CENTER_LEFT);

        HBox sizeSection = new HBox(8, sizeLabel, sizeSlider, sizeValue);
        sizeSection.setAlignment(Pos.CENTER_LEFT);

        HBox toolsRow = new HBox(24, colorSection, sizeSection, eraserBtn, clearBtn);
        toolsRow.setAlignment(Pos.CENTER_LEFT);
        toolsRow.setPadding(new Insets(16));
        toolsRow.setStyle("""
            -fx-background-color: #F8FAFC;
            -fx-background-radius: 8px;
            -fx-border-color: #E5E7EB;
            -fx-border-width: 1px;
            -fx-border-radius: 8px;
        """);

        // Canvas with border
        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-border-color: #E5E7EB;
            -fx-border-width: 2px;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
        """);
        canvasContainer.setPadding(new Insets(8));

        VBox drawingArea = new VBox(16, toolsLabel, toolsRow, canvasContainer);
        drawingArea.setPadding(new Insets(16));

        setupCanvasEvents();

        return drawingArea;
    }

    private void setupCanvasEvents() {
        GraphicsContext g = canvas.getGraphicsContext2D();

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            g.beginPath();
            g.moveTo(e.getX(), e.getY());
            g.setLineWidth(sizeSlider.getValue());
            if (eraserBtn.isSelected()) {
                g.setStroke(javafx.scene.paint.Color.WHITE);
            } else {
                g.setStroke(colorPicker.getValue());
            }
            g.stroke();
            strokes.add(String.format("{\"m\":\"down\",\"x\":%.1f,\"y\":%.1f,\"c\":\"%s\",\"w\":%.1f,\"er\":%s}",
                    e.getX(), e.getY(), colorPicker.getValue().toString(), sizeSlider.getValue(), eraserBtn.isSelected()));
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            g.lineTo(e.getX(), e.getY());
            g.stroke();
            strokes.add(String.format("{\"m\":\"drag\",\"x\":%.1f,\"y\":%.1f}", e.getX(), e.getY()));
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            strokes.add("{\"m\":\"up\"}");
        });
    }

    private void setupEventHandlers(NoteDto existing) {
        // Find buttons and toggles from the scene graph
        lookupAll(".button").forEach(node -> {
            if (node instanceof Button btn && btn.getUserData() != null) {
                String action = btn.getUserData().toString();
                switch (action) {
                    case "save" -> btn.setOnAction(e -> {
                        animateButtonPress(btn);
                        handleSave();
                    });
                    case "favorite" -> btn.setOnAction(e -> {
                        animateButtonPress(btn);
                        handleFavorite(btn);
                    });
                    case "trash" -> btn.setOnAction(e -> {
                        animateButtonPress(btn);
                        handleTrash();
                    });
                }
            }
        });

        // Mode toggle handling
        lookupAll(".radio-button").forEach(node -> {
            if (node instanceof RadioButton radio && radio.getUserData() != null) {
                radio.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        String newMode = radio.getUserData().toString();
                        switchMode(newMode);
                    }
                });
            }
        });
    }

    private void switchMode(String newMode) {
        mode = newMode;
        StackPane center = (StackPane) getCenter();
        VBox[] panes = (VBox[]) center.getUserData();

        if (panes != null && panes.length >= 2) {
            VBox textPane = panes[0];
            VBox drawPane = panes[1];

            textPane.setVisible("text".equals(mode));
            textPane.setManaged("text".equals(mode));
            drawPane.setVisible("draw".equals(mode));
            drawPane.setManaged("draw".equals(mode));
        }
    }

    private void loadExistingNote(NoteDto existing) {
        noteId = existing.id();
        titleField.setText(existing.title() == null ? "" : existing.title());
        if (existing.textHtml() != null) {
            htmlEditor.setHtmlText(existing.textHtml());
        }

        // Update favorite button text
        lookupAll(".button").forEach(node -> {
            if (node instanceof Button btn && "favorite".equals(btn.getUserData())) {
                btn.setText(existing.favorite() ? "Unfavorite" : "Favorite");
            }
        });
    }

    private void handleSave() {
        try {
            String title = titleField.getText();
            String textHtml = "text".equals(mode) ? htmlEditor.getHtmlText() : null;
            String drawingJson = "draw".equals(mode) ? serializeStrokes() : null;

            if (noteId == null) {
                var n = NoteApi.create(title, textHtml, drawingJson);
                noteId = n.id();
                showModernInfo("Note Created", "Your note has been successfully created!");
            } else {
                NoteApi.update(noteId, title, textHtml, drawingJson);
                showModernInfo("Note Saved", "Your changes have been saved successfully!");
            }
        } catch (Exception ex) {
            showModernError("Save Failed", "Failed to save note: " + ex.getMessage());
        }
    }

    private void handleFavorite(Button favoriteBtn) {
        if (noteId == null) return;
        try {
            boolean toValue = favoriteBtn.getText().contains("Unfavorite") ? false : true;
            var n = NoteApi.setFavorite(noteId, toValue);
            favoriteBtn.setText(n.favorite() ? "Unfavorite" : "Favorite");
            showModernInfo("Favorite Updated",
                    n.favorite() ? "Note added to favorites!" : "Note removed from favorites!");
        } catch (Exception ex) {
            showModernError("Favorite Failed", "Failed to update favorite status: " + ex.getMessage());
        }
    }

    private void handleTrash() {
        if (noteId == null) return;
        try {
            NoteApi.setTrashed(noteId, true);
            showModernInfo("Moved to Trash", "Note has been moved to trash successfully!");
        } catch (Exception ex) {
            showModernError("Trash Failed", "Failed to move note to trash: " + ex.getMessage());
        }
    }

    private Button createFormalButton(String text, String bgColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-padding: 10px 20px;
            -fx-background-radius: 6px;
            -fx-font-size: 14px;
            -fx-font-weight: 500;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
            -fx-cursor: hand;
            -fx-border-color: transparent;
            -fx-border-width: 1px;
            -fx-border-radius: 6px;
        """, bgColor));

        btn.setOnMouseEntered(e -> {
            btn.setStyle(btn.getStyle().replace(bgColor, hoverColor));
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(btn.getStyle().replace(hoverColor, bgColor));
        });

        return btn;
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

    private void styleRadioButton(RadioButton radio) {
        radio.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #374151;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
            -fx-font-weight: 500;
        """);
    }

    private void styleToggleButton(ToggleButton toggle) {
        toggle.setStyle("""
            -fx-background-color: #F3F4F6;
            -fx-text-fill: #374151;
            -fx-padding: 8px 16px;
            -fx-background-radius: 8px;
            -fx-font-size: 14px;
            -fx-font-weight: 500;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
            -fx-cursor: hand;
            -fx-border-color: #E5E7EB;
            -fx-border-width: 1px;
            -fx-border-radius: 8px;
        """);

        toggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                toggle.setStyle(toggle.getStyle().replace("#F3F4F6", "#3B82F6").replace("#374151", "white"));
            } else {
                toggle.setStyle(toggle.getStyle().replace("#3B82F6", "#F3F4F6").replace("white", "#374151"));
            }
        });
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

    private String serializeStrokes() {
        // Very simple JSON array string (good enough for backend storage)
        return "[" + String.join(",", strokes) + "]";
    }

    private void showModernError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);

        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);

        alert.showAndWait();
    }

    private void showModernInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(title);
        alert.setContentText(message);

        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);

        alert.showAndWait();
    }
}

