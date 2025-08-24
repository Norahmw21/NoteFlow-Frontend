package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.NoteApi;
import com.example.noteflowfrontend.core.dto.NoteDto;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.web.HTMLEditor;

import java.util.ArrayList;
import java.util.List;

public class NoteEditorPage extends BorderPane {
    private final TextField titleField = new TextField();
    private final HTMLEditor htmlEditor = new HTMLEditor();

    // Drawing
    private final Canvas canvas = new Canvas(900, 600);
    private final ColorPicker colorPicker = new ColorPicker();
    private final Slider sizeSlider = new Slider(1, 30, 3);
    private final ToggleButton eraserBtn = new ToggleButton("Eraser");

    private Long noteId; // null for new
    private String mode = "text"; // or "draw"

    // For a super-simple drawing JSON -> we capture strokes (x,y)
    private final List<String> strokes = new ArrayList<>();

    public NoteEditorPage(NoteDto existing, String startMode) {
        setPadding(new Insets(16));
        if (startMode != null) mode = startMode;

        var topBar = new HBox(10);
        var modeToggle = new ToggleGroup();
        var textToggle = new RadioButton("Text");
        var drawToggle = new RadioButton("Draw");
        textToggle.setToggleGroup(modeToggle);
        drawToggle.setToggleGroup(modeToggle);
        if ("draw".equalsIgnoreCase(mode)) drawToggle.setSelected(true); else textToggle.setSelected(true);

        titleField.setPromptText("Note title");
        titleField.setPrefWidth(320);

        var saveBtn = new Button("Save");
        var trashBtn = new Button("Move to Trash");
        var favoriteBtn = new Button("★ Favorite");
        topBar.getChildren().addAll(new Label("Title:"), titleField, new Separator(), textToggle, drawToggle, new Separator(), favoriteBtn, trashBtn, saveBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);

        setTop(topBar);

        // Center stack: either htmlEditor or drawing canvas
        StackPane textPane = new StackPane(htmlEditor);
        StackPane drawPane = new StackPane(buildDrawingTools());
        var center = new StackPane(textPane, drawPane);
        setCenter(center);

        // Show correct mode
        textPane.setVisible("text".equals(mode)); textPane.setManaged("text".equals(mode));
        drawPane.setVisible("draw".equals(mode)); drawPane.setManaged("draw".equals(mode));

        modeToggle.selectedToggleProperty().addListener((o, a, b) -> {
            mode = b == drawToggle ? "draw" : "text";
            textPane.setVisible("text".equals(mode));
            textPane.setManaged("text".equals(mode));
            drawPane.setVisible("draw".equals(mode));
            drawPane.setManaged("draw".equals(mode));
        });

        // Existing note?
        if (existing != null) {
            noteId = existing.id();
            titleField.setText(existing.title() == null ? "" : existing.title());
            if (existing.textHtml() != null) htmlEditor.setHtmlText(existing.textHtml());
            // Drawing: keep simple – we won’t parse existing JSON into strokes here for brevity
            if (existing.favorite()) favoriteBtn.setText("★ Unfavorite");
        }

        favoriteBtn.setOnAction(e -> {
            if (noteId == null) return;
            try {
                boolean toValue = favoriteBtn.getText().contains("Unfavorite") ? false : true;
                var n = NoteApi.setFavorite(noteId, toValue);
                favoriteBtn.setText(n.favorite() ? "★ Unfavorite" : "★ Favorite");
            } catch (Exception ex) {
                showErr(ex);
            }
        });

        trashBtn.setOnAction(e -> {
            if (noteId == null) return;
            try {
                NoteApi.setTrashed(noteId, true);
                showInfo("Moved to Trash");
            } catch (Exception ex) {
                showErr(ex);
            }
        });

        saveBtn.setOnAction(e -> {
            try {
                String title = titleField.getText();
                String textHtml = "text".equals(mode) ? htmlEditor.getHtmlText() : null;
                String drawingJson = "draw".equals(mode) ? serializeStrokes() : null;

                if (noteId == null) {
                    var n = NoteApi.create(title, textHtml, drawingJson);
                    noteId = n.id();
                    showInfo("Note created");
                } else {
                    NoteApi.update(noteId, title, textHtml, drawingJson);
                    showInfo("Note saved");
                }
            } catch (Exception ex) {
                showErr(ex);
            }
        });
    }

    private VBox buildDrawingTools() {
        var tools = new HBox(10);
        tools.getChildren().addAll(new Label("Color:"), colorPicker, new Label("Size:"), sizeSlider, eraserBtn);

        // Canvas events
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

        var pane = new VBox(12, tools, new StackPane(canvas));
        pane.setPadding(new Insets(8));
        return pane;
    }

    private String serializeStrokes() {
        // Very simple JSON array string (good enough for backend storage)
        return "[" + String.join(",", strokes) + "]";
    }

    private void showErr(Exception ex) {
        new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
    }
    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}
