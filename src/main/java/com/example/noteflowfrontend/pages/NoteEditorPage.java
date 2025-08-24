package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.NoteApi;
import com.example.noteflowfrontend.core.dto.NoteDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.ScaleTransition;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

public class NoteEditorPage extends BorderPane {
    private final TextField titleField = new TextField();
    private final HTMLEditor htmlEditor = new HTMLEditor();

    // Drawing (use the fixed event-based model)
    private final Canvas canvas = new Canvas(900, 600);
    private GraphicsContext gc;
    private final ColorPicker colorPicker = new ColorPicker(Color.BLACK);
    private final Slider sizeSlider = new Slider(1, 30, 3);
    private final ToggleButton eraserBtn = new ToggleButton("Eraser");

    private Long noteId; // null for new
    private String mode = "text"; // or "draw"

    // Event-based drawing model (compatible with Jackson)
    private final List<Map<String, Object>> events = new ArrayList<>();
    private static final ObjectMapper M = new ObjectMapper();

    // Header controls (so we can wire handlers cleanly)
    private Button saveBtn;
    private Button favoriteBtn;
    private Button trashBtn;
    private RadioButton textToggle;
    private RadioButton drawToggle;

    public NoteEditorPage(NoteDto existing, String startMode) {
        setPadding(new Insets(24));
        setStyle("-fx-background-color: linear-gradient(to bottom, #F8FAFC, #F1F5F9);");

        if (startMode != null) mode = startMode;

        setupHeader(existing);
        setupMainContent();
        setupEventHandlers();

        if (existing != null) {
            loadExistingNote(existing);
        }
    }

    // ---------- UI: Header ----------
    private void setupHeader(NoteDto existing) {
        var exportMenu = createExportMenu();

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

        // Mode toggle
        var modeToggle = new ToggleGroup();
        textToggle = new RadioButton("Text Editor");
        drawToggle = new RadioButton("Drawing Canvas");
        styleRadioButton(textToggle);
        styleRadioButton(drawToggle);
        textToggle.setToggleGroup(modeToggle);
        drawToggle.setToggleGroup(modeToggle);
        if ("draw".equalsIgnoreCase(mode)) drawToggle.setSelected(true); else textToggle.setSelected(true);

        HBox modeSection = new HBox(16, textToggle, drawToggle);
        modeSection.setAlignment(Pos.CENTER_LEFT);

        // Action buttons
        saveBtn = createFormalButton("Save", "#374151", "#1F2937");
        favoriteBtn = createFormalButton("Favorite", "#6B7280", "#4B5563");
        trashBtn = createFormalButton("Delete", "#6B7280", "#4B5563");

        HBox buttonSection = new HBox(12, saveBtn, favoriteBtn, trashBtn, exportMenu);
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
    }

    // ---------- UI: Center ----------
    private void setupMainContent() {
        htmlEditor.setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-border-color: #E5E7EB;
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);
        """);

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

        // Store for switching
        center.setUserData(new VBox[]{textPane, drawPane});
    }

    private VBox buildDrawingTools() {
        Label toolsLabel = new Label("Drawing Tools");
        toolsLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: #374151;
            -fx-font-weight: 600;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);

        // Size slider visuals
        sizeSlider.setShowTickMarks(true);
        sizeSlider.setShowTickLabels(true);
        sizeSlider.setMajorTickUnit(5);
        sizeSlider.setBlockIncrement(1);
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
        sizeSlider.valueProperty().addListener((obs, o, n) -> sizeValue.setText(String.format("%.0f px", n.doubleValue())));

        Label colorLabel = new Label("Color:");
        colorLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #6B7280;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);

        Label sizeLabel = new Label("Brush Size:");
        sizeLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #6B7280;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);

        styleToggleButton(eraserBtn);

        Button clearBtn = createModernButton("Clear Canvas", "#6B7280", "#4B5563");
        clearBtn.setOnAction(e -> {
            animateButtonPress(clearBtn);
            clearCanvas();
            events.clear();
        });

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

        // Canvas container
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

        setupCanvasEvents(); // set gc + events handlers

        return drawingArea;
    }

    // ---------- Canvas logic (event-based, robust) ----------
    private void setupCanvasEvents() {
        gc = canvas.getGraphicsContext2D();
        clearCanvas(); // ensure white bg

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.setLineWidth(sizeSlider.getValue());
            gc.setStroke(eraserBtn.isSelected() ? Color.WHITE : colorPicker.getValue());
            gc.stroke();

            Map<String, Object> down = new HashMap<>();
            down.put("m", "down");
            down.put("x", e.getX());
            down.put("y", e.getY());
            down.put("c", toHex(colorPicker.getValue()));
            down.put("w", sizeSlider.getValue());
            down.put("er", eraserBtn.isSelected());
            events.add(down);
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();

            Map<String, Object> drag = new HashMap<>();
            drag.put("m", "drag");
            drag.put("x", e.getX());
            drag.put("y", e.getY());
            events.add(drag);
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            Map<String, Object> up = new HashMap<>();
            up.put("m", "up");
            events.add(up);
        });
    }

    private void clearCanvas() {
        if (gc == null) return;
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /** Replay saved events to the canvas and keep per-stroke style values. */
    private void redrawFromEvents() {
        if (gc == null) return;
        clearCanvas();

        boolean pathOpen = false;
        for (Map<String, Object> ev : events) {
            String m = String.valueOf(ev.get("m"));
            if ("down".equals(m)) {
                if (pathOpen) { gc.closePath(); pathOpen = false; }
                double x = toD(ev.get("x"), 0), y = toD(ev.get("y"), 0);
                double w = toD(ev.get("w"), 3);
                boolean er = toB(ev.get("er"), false);
                String c = ev.get("c") == null ? "#000000" : String.valueOf(ev.get("c"));

                gc.setLineWidth(w);
                gc.setStroke(er ? Color.WHITE : Color.web(c));
                gc.beginPath();
                gc.moveTo(x, y);
                gc.stroke();
                pathOpen = true;
            } else if ("drag".equals(m)) {
                double x = toD(ev.get("x"), 0), y = toD(ev.get("y"), 0);
                gc.lineTo(x, y);
                gc.stroke();
            } else if ("up".equals(m)) {
                if (pathOpen) { gc.closePath(); pathOpen = false; }
            }
        }
        if (pathOpen) gc.closePath();
    }

    private String serializeEvents() throws Exception {
        if (events.isEmpty()) return ""; // store empty when no drawing
        return M.writeValueAsString(events);
    }

    // ---------- Handlers ----------
    private void setupEventHandlers() {
        saveBtn.setOnAction(e -> {
            animateButtonPress(saveBtn);
            handleSave();
        });

        favoriteBtn.setOnAction(e -> {
            animateButtonPress(favoriteBtn);
            handleFavorite(favoriteBtn);
        });

        trashBtn.setOnAction(e -> {
            animateButtonPress(trashBtn);
            handleTrash();
        });

        textToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) switchMode("text");
        });
        drawToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) switchMode("draw");
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
        favoriteBtn.setText(existing.favorite() ? "Unfavorite" : "Favorite");

        // Rehydrate drawing if present
        String dj = existing.drawingJson();
        if (dj != null && !dj.isBlank()) {
            try {
                List<Map<String, Object>> parsed = M.readValue(
                        dj, new TypeReference<List<Map<String, Object>>>() {});
                events.clear();
                events.addAll(parsed);
                redrawFromEvents();
            } catch (Exception ex) {
                System.err.println("Failed to parse drawingJson: " + ex.getMessage());
            }
        }
    }

    private void handleSave() {
        try {
            String title = titleField.getText();
            String textHtml = "text".equals(mode) ? htmlEditor.getHtmlText() : null;
            String drawingJson = "draw".equals(mode) ? serializeEvents() : null;

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

    // ---------- Export (from your version, compatible with event-based canvas) ----------
    private MenuButton createExportMenu() {
        MenuItem exportHtml = new MenuItem("Export text as HTML");
        exportHtml.setOnAction(e -> exportTextHtml());

        MenuItem exportPng = new MenuItem("Export drawing as PNG");
        exportPng.setOnAction(e -> exportCanvasPng());

        MenuItem exportPdf = new MenuItem("Export both as PDF");
        exportPdf.setOnAction(e -> exportAsPdf());

        MenuButton mb = new MenuButton("Export", null, exportHtml, exportPng, exportPdf);
        mb.setStyle("""
        -fx-background-color: #374151;
        -fx-text-fill: white;
        -fx-padding: 10px 16px;
        -fx-background-radius: 8px;
        -fx-font-size: 14px;
        -fx-font-weight: 600;
        -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        -fx-cursor: hand;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);
    """);

        mb.setOnMouseEntered(e -> mb.setStyle(mb.getStyle().replace("#374151", "#1F2937")));
        mb.setOnMouseExited(e -> mb.setStyle(mb.getStyle().replace("#1F2937", "#374151")));
        return mb;
    }

    private void exportTextHtml() {
        try {
            String html = htmlEditor.getHtmlText();
            if (html == null) html = "";

            FileChooser fc = new FileChooser();
            fc.setTitle("Export Text as HTML");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));
            File file = fc.showSaveDialog(getScene().getWindow());
            if (file == null) return;

            if (!file.getName().toLowerCase().endsWith(".html")) {
                file = new File(file.getParentFile(), file.getName() + ".html");
            }
            Files.writeString(file.toPath(), html, StandardCharsets.UTF_8);
            showModernInfo("Exported", "Saved: " + file.getName());
        } catch (Exception ex) {
            showModernError("Export Failed", ex.getMessage());
        }
    }

    private void exportCanvasPng() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Export Drawing as PNG");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
            File file = fc.showSaveDialog(getScene().getWindow());
            if (file == null) return;

            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getParentFile(), file.getName() + ".png");
            }

            // Canvas already has a white background via clearCanvas()
            BufferedImage bi = SwingFXUtils.fromFXImage(canvas.snapshot(null, null), null);
            ImageIO.write(bi, "png", file);
            showModernInfo("Exported", "Saved: " + file.getName());
        } catch (Exception ex) {
            showModernError("Export Failed", ex.getMessage());
        }
    }

    // PDF with two pages: text snapshot (scaled) then drawing
    private void exportAsPdf() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Export Note as PDF");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File file = fc.showSaveDialog(getScene().getWindow());
            if (file == null) return;

            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getParentFile(), file.getName() + ".pdf");
            }

            BufferedImage textImg = snapshotNodeToImage(getTextSnapshotNode(), 2.0); // crisp text
            BufferedImage drawImg = SwingFXUtils.fromFXImage(canvas.snapshot(null, null), null);

            try (PDDocument doc = new PDDocument()) {
                addImageAsPdfPage(doc, textImg);
                addImageAsPdfPage(doc, drawImg);
                doc.save(file);
            }

            showModernInfo("Exported", "Saved: " + file.getName());
        } catch (NoClassDefFoundError e) {
            showModernError("PDF Feature Disabled",
                    "PDF export requires PDFBox. Add the dependency to your build.");
        } catch (Exception ex) {
            showModernError("Export Failed", ex.getMessage());
        }
    }

    private BufferedImage snapshotNodeToImage(javafx.scene.Node node, double scale) {
        SnapshotParameters sp = new SnapshotParameters();
        sp.setTransform(Transform.scale(scale, scale));
        return SwingFXUtils.fromFXImage(node.snapshot(sp, null), null);
    }

    private javafx.scene.Node getTextSnapshotNode() {
        javafx.scene.Node web = htmlEditor.lookup(".web-view");
        return web != null ? web : htmlEditor;
    }

    private void addImageAsPdfPage(PDDocument doc, BufferedImage img) throws Exception {
        PDPage page = new PDPage(PDRectangle.LETTER);
        doc.addPage(page);

        var pdImg = LosslessFactory.createFromImage(doc, img);
        float margin = 36f; // 0.5 inch
        float pageW = page.getMediaBox().getWidth();
        float pageH = page.getMediaBox().getHeight();
        float imgW = pdImg.getWidth();
        float imgH = pdImg.getHeight();
        float scale = Math.min((pageW - 2*margin) / imgW, (pageH - 2*margin) / imgH);
        float drawW = imgW * scale;
        float drawH = imgH * scale;
        float x = (pageW - drawW) / 2f;
        float y = pageH - margin - drawH;

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            cs.drawImage(pdImg, x, y, drawW, drawH);
        }
    }

    // ---------- Styles & helpers ----------
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

        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace(bgColor, hoverColor)));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace(hoverColor, bgColor)));
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

        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace(bgColor, hoverColor)));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace(hoverColor, bgColor)));
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

    private static String toHex(Color c) {
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private static double toD(Object o, double def) {
        if (o == null) return def;
        try { return (o instanceof Number n) ? n.doubleValue() : Double.parseDouble(String.valueOf(o)); }
        catch (Exception e) { return def; }
    }
    private static boolean toB(Object o, boolean def) {
        if (o == null) return def;
        if (o instanceof Boolean b) return b;
        String s = String.valueOf(o).toLowerCase();
        return "true".equals(s) || "1".equals(s) || "yes".equals(s);
    }

    private void showModernError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("""
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
        alert.getDialogPane().setStyle("""
            -fx-background-color: #FFFFFF;
            -fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui;
        """);
        alert.showAndWait();
    }
}
