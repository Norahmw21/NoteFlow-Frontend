package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.NoteApi;
import com.example.noteflowfrontend.core.dto.NoteDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.ScaleTransition;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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

    // ==== THEME (matches AppShell) ====
    private static final String PRIMARY_LIGHT = "#DABFFF";   // lavender
    private static final String PRIMARY_DARK  = "#4F518C";   // dark purple-blue
    private static final String SURFACE       = "#FFFFFF";   // cards
    private static final String CANVAS_BG     = "#FAFBFF";   // app background
    private static final String BORDER        = "#E5E9F2";   // subtle border
    private static final String TEXT_PRIMARY  = "#2C2A4A";   // headings
    private static final String TEXT_SECOND   = "#6C7293";   // secondary text

    private final TextField titleField = new TextField();
    private final HTMLEditor htmlEditor = new HTMLEditor();

    // Tags
    private final TextField tagNameField = new TextField();
    private final ColorPicker tagColorPicker = new ColorPicker();

    // Drawing
    private final Canvas canvas = new Canvas(640, 360);
    private GraphicsContext gc;
    private final ColorPicker colorPicker = new ColorPicker(Color.BLACK);
    private final Slider sizeSlider = new Slider(1, 30, 3);
    private final ToggleButton eraserBtn = new ToggleButton("Eraser");

    private Long noteId; // null for new
    private String mode = "text"; // or "draw"

    // event-based drawing model
    private final List<Map<String, Object>> events = new ArrayList<>();
    private static final ObjectMapper M = new ObjectMapper();

    // Header controls
    private Button saveBtn;
    private Button favoriteBtn;
    private Button trashBtn;
    private MenuButton exportMenu;
    private RadioButton textToggle;
    private RadioButton drawToggle;

    public NoteEditorPage(NoteDto existing, String startMode) {
        setPadding(new Insets(24));
        setStyle("-fx-background-color: linear-gradient(to bottom, " + CANVAS_BG + ", #F1F5FF);");

        if (startMode != null) mode = startMode;

        setupHeader(existing);
        setupMainContent();
        setupEventHandlers();

        if (existing != null) {
            loadExistingNote(existing);
        }
    }

    // ---------- UI: Header (Word-style ribbon) ----------
    private void setupHeader(NoteDto existing) {
        exportMenu = createExportMenu();

        // Shared sizes
        final double INPUT_H = 38;
        final double BTN_H   = 36;
        final double BTN_W   = 118;

        // --- Title field
        titleField.setPromptText("Enter note title…");
        titleField.setStyle("""
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            -fx-padding: 0 12px;
            -fx-font-size: 14px;
            -fx-text-fill: %s;
            -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
            -fx-prompt-text-fill: %s;
        """.formatted(SURFACE, BORDER, TEXT_PRIMARY, TEXT_SECOND));
        titleField.setPrefHeight(INPUT_H);
        titleField.setMinHeight(INPUT_H);
        titleField.setMaxHeight(INPUT_H);
        titleField.setPrefWidth(360);

        // --- Tag name
        tagNameField.setPromptText("Course / Topic");
        tagNameField.setStyle("""
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            -fx-padding: 0 12px;
            -fx-font-size: 14px;
            -fx-text-fill: %s;
            -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
            -fx-prompt-text-fill: %s;
        """.formatted(SURFACE, BORDER, TEXT_PRIMARY, TEXT_SECOND));
        tagNameField.setPrefHeight(INPUT_H);
        tagNameField.setMinHeight(INPUT_H);
        tagNameField.setMaxHeight(INPUT_H);
        tagNameField.setPrefWidth(200);

        // --- Tag color picker (styled to match)
        tagColorPicker.setStyle("""
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            -fx-padding: 0 8px;
        """.formatted(SURFACE, BORDER));
        tagColorPicker.setPrefHeight(INPUT_H);
        tagColorPicker.setMinHeight(INPUT_H);
        tagColorPicker.setMaxHeight(INPUT_H);
        tagColorPicker.setPrefWidth(120);

        // --- Mode toggle
        var modeToggle = new ToggleGroup();
        textToggle = new RadioButton("Text");
        drawToggle = new RadioButton("Draw");
        styleRadioButton(textToggle);
        styleRadioButton(drawToggle);
        textToggle.setToggleGroup(modeToggle);
        drawToggle.setToggleGroup(modeToggle);
        if ("draw".equalsIgnoreCase(mode)) drawToggle.setSelected(true); else textToggle.setSelected(true);

        // --- Actions
        saveBtn     = createGhostButton("Save");
        favoriteBtn = createGhostButton("Favorite");
        trashBtn    = createDangerGhostButton("Delete");
        for (Button b : new Button[]{saveBtn, favoriteBtn, trashBtn}) {
            b.setPrefHeight(BTN_H);
            b.setMinHeight(BTN_H);
            b.setMaxHeight(BTN_H);
            b.setPrefWidth(BTN_W);
        }
        exportMenu.setPrefHeight(BTN_H);
        exportMenu.setMinHeight(BTN_H);
        exportMenu.setMaxHeight(BTN_H);
        exportMenu.setPrefWidth(BTN_W);

        // ===== Ribbon groups (like Word) =====
        // Each group: label + pill container for controls
        VBox groupNote   = ribbonGroup("Note", new HBox(8, titleField, tagNameField, tagColorPicker));
        VBox groupMode   = ribbonGroup("Mode", new HBox(8, textToggle, drawToggle));
        VBox groupAction = ribbonGroup("Actions", new HBox(8, saveBtn, favoriteBtn, trashBtn));
        VBox groupExport = ribbonGroup("Export", new HBox(8, exportMenu));

        // Ribbon bar
        HBox ribbonRow = new HBox(16, groupNote, groupMode, groupAction, groupExport);
        ribbonRow.setAlignment(Pos.CENTER_LEFT);
        ribbonRow.setPadding(new Insets(10, 12, 8, 12));
        ribbonRow.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #FFFFFF, #FBFCFF);
            -fx-border-color: %s;
            -fx-border-width: 0 0 1 0;
        """.formatted(BORDER));

        // Scroll if narrow (keeps single row)
        ScrollPane ribbonScroll = new ScrollPane(ribbonRow);
        ribbonScroll.setFitToHeight(true);
        ribbonScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        ribbonScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        ribbonScroll.setPannable(true);
        ribbonScroll.setStyle("""
            -fx-background-color: transparent;
            -fx-background-insets: 0;
            -fx-padding: 0;
        """);

        // Put ribbon at top
        VBox topBox = new VBox(ribbonScroll);
        topBox.setSpacing(10);
        topBox.setPadding(new Insets(0, 0, 14, 0));
        setTop(topBox);
    }

    // Helper to make a “Word ribbon group” (title + rounded container)
    private VBox ribbonGroup(String title, Node contentRow) {
        HBox pill = new HBox(contentRow);
        pill.setAlignment(Pos.CENTER_LEFT);
        pill.setPadding(new Insets(8));
        pill.setStyle("""
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-background-radius: 10px;
            -fx-border-radius: 10px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 6, 0, 0, 1);
        """.formatted(SURFACE, BORDER));

        Label lbl = new Label(title);
        lbl.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: %s;
            -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
        """.formatted(TEXT_SECOND));

        VBox box = new VBox(6, pill, lbl);
        box.setAlignment(Pos.TOP_LEFT);
        return box;
    }

    // ---------- UI: Center ----------
    private void setupMainContent() {
        htmlEditor.setStyle("""
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);
        """.formatted(SURFACE, BORDER));

        VBox textPane = new VBox(htmlEditor);
        textPane.setPadding(new Insets(10));
        textPane.setStyle("""
            -fx-background-color: %s;
            -fx-background-radius: 12px;
            -fx-border-color: %s;
            -fx-border-width: 1px;
            -fx-border-radius: 12px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 12, 0, 0, 4);
        """.formatted(SURFACE, BORDER));
        VBox.setVgrow(htmlEditor, Priority.ALWAYS);
        htmlEditor.setPrefHeight(520);

        VBox drawPane = new VBox(buildDrawingTools());
        drawPane.setPadding(new Insets(10));
        drawPane.setStyle("""
            -fx-background-color: %s;
            -fx-background-radius: 12px;
            -fx-border-color: %s;
            -fx-border-width: 1px;
            -fx-border-radius: 12px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 12, 0, 0, 4);
        """.formatted(SURFACE, BORDER));

        var center = new StackPane(textPane, drawPane);
        center.setPadding(new Insets(4, 0, 0, 0));
        setCenter(center);

        textPane.setVisible("text".equals(mode));
        textPane.setManaged("text".equals(mode));
        drawPane.setVisible("draw".equals(mode));
        drawPane.setManaged("draw".equals(mode));

        center.setUserData(new VBox[]{textPane, drawPane});
    }

    private VBox buildDrawingTools() {
        Label toolsLabel = new Label("Drawing Tools");
        toolsLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: %s;
            -fx-font-weight: 700;
            -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
        """.formatted(TEXT_PRIMARY));

        sizeSlider.setShowTickMarks(true);
        sizeSlider.setShowTickLabels(true);
        sizeSlider.setMajorTickUnit(5);
        sizeSlider.setBlockIncrement(1);
        sizeSlider.setStyle("""
            -fx-background-color: #F3F4F8;
            -fx-background-radius: 8px;
        """);
        sizeSlider.setPrefWidth(160);

        Label sizeValue = new Label(String.format("%.0f px", sizeSlider.getValue()));
        sizeValue.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: %s;
            -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
        """.formatted(TEXT_SECOND));
        sizeSlider.valueProperty().addListener((obs, o, n) -> sizeValue.setText(String.format("%.0f px", n.doubleValue())));

        Label colorLabel = new Label("Color:");
        colorLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: %s;
            -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
        """.formatted(TEXT_SECOND));

        Label sizeLabel = new Label("Brush Size:");
        sizeLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: %s;
            -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
        """.formatted(TEXT_SECOND));

        styleToggleButton(eraserBtn);

        Button clearBtn = createGhostButton("Clear Canvas");
        clearBtn.setOnAction(e -> {
            animateButtonPress(clearBtn);
            clearCanvas();
            events.clear();
        });

        HBox colorSection = new HBox(8, colorLabel, colorPicker);
        colorSection.setAlignment(Pos.CENTER_LEFT);

        HBox sizeSection = new HBox(8, sizeLabel, sizeSlider, sizeValue);
        sizeSection.setAlignment(Pos.CENTER_LEFT);

        FlowPane toolsRow = new FlowPane(24, 12, colorSection, sizeSection, eraserBtn, clearBtn);
        toolsRow.setAlignment(Pos.CENTER_LEFT);
        toolsRow.setPrefWrapLength(820);
        toolsRow.setPadding(new Insets(14));
        toolsRow.setStyle("""
            -fx-background-color: #F6F7FF;
            -fx-background-radius: 10px;
            -fx-border-color: %s;
            -fx-border-width: 1px;
            -fx-border-radius: 10px;
        """.formatted(BORDER));

        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setStyle("""
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-border-width: 2px;
            -fx-border-radius: 10px;
            -fx-background-radius: 10px;
        """.formatted(SURFACE, BORDER));
        canvasContainer.setPadding(new Insets(10));

        VBox drawingArea = new VBox(14, toolsLabel, toolsRow, canvasContainer);
        drawingArea.setPadding(new Insets(16));

        setupCanvasEvents();

        // ---- Responsive canvas (keeps draw model intact) ----
        drawingArea.widthProperty().addListener((obs, oldW, newW) -> {
            double target = Math.min(1000, Math.max(360, newW.doubleValue() - 56));
            canvas.setWidth(target);
            clearCanvas();
            redrawFromEvents();
        });
        drawingArea.heightProperty().addListener((obs, oldH, newH) -> {
            double targetH = Math.min(700, Math.max(240, newH.doubleValue() - 220));
            canvas.setHeight(targetH);
            clearCanvas();
            redrawFromEvents();
        });

        return drawingArea;
    }

    // ---------- Canvas logic ----------
    private void setupCanvasEvents() {
        gc = canvas.getGraphicsContext2D();
        clearCanvas();

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
        if (events.isEmpty()) return "";
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

        textToggle.selectedProperty().addListener((obs, oldVal, newVal) -> { if (newVal) switchMode("text"); });
        drawToggle.selectedProperty().addListener((obs, oldVal, newVal) -> { if (newVal) switchMode("draw"); });
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

        // Load tag
        if (existing.tagName() != null) tagNameField.setText(existing.tagName());
        if (existing.tagColor() != null && !existing.tagColor().isBlank()) {
            try { tagColorPicker.setValue(Color.web(existing.tagColor())); } catch (Exception ignored) {}
        }

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

            String tagName = tagNameField.getText();
            String tagColor = (tagColorPicker.getValue() == null) ? null : toHex(tagColorPicker.getValue());

            if (noteId == null) {
                var n = NoteApi.create(title, textHtml, drawingJson, tagName, tagColor);
                noteId = n.id();
                showModernInfo("Note Created", "Your note has been successfully created!");
            } else {
                NoteApi.update(noteId, title, textHtml, drawingJson, tagName, tagColor);
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

    // ---------- Export ----------
    private MenuButton createExportMenu() {
        MenuItem exportHtml = new MenuItem("Export text as HTML");
        exportHtml.setOnAction(e -> exportTextHtml());

        MenuItem exportPng = new MenuItem("Export drawing as PNG");
        exportPng.setOnAction(e -> exportCanvasPng());

        MenuItem exportPdf = new MenuItem("Export both as PDF");
        exportPdf.setOnAction(e -> exportAsPdf());

        // Compact button with icon only
        MenuButton mb = new MenuButton("\uD83D\uDCE4", null, exportHtml, exportPng, exportPdf);
        mb.setPrefWidth(40);
        mb.setPrefHeight(32);

        mb.setStyle("""
        -fx-background-color: rgba(79,81,140,0.10);
        -fx-text-fill: %s;
        -fx-background-radius: 6px;
        -fx-font-size: 14px;
        -fx-font-weight: 700;
        -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
        -fx-cursor: hand;
        -fx-border-color: %s;
        -fx-border-radius: 6px;
        -fx-padding: 0;
        -fx-effect: dropshadow(gaussian, rgba(79,81,140,0.18), 6, 0, 0, 2);
    """.formatted(TEXT_PRIMARY, BORDER));

        // Hover effect
        mb.setOnMouseEntered(e -> mb.setStyle(mb.getStyle().replace("0.10", "0.22")));
        mb.setOnMouseExited(e -> mb.setStyle(mb.getStyle().replace("0.22", "0.10")));

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

            BufferedImage bi = SwingFXUtils.fromFXImage(canvas.snapshot(null, null), null);
            ImageIO.write(bi, "png", file);
            showModernInfo("Exported", "Saved: " + file.getName());
        } catch (Exception ex) {
            showModernError("Export Failed", ex.getMessage());
        }
    }

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

            BufferedImage textImg = snapshotNodeToImage(getTextSnapshotNode(), 2.0);
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
    private Button createPrimaryButton() {
        Button button = new Button("Save");
        button.setStyle("""
        -fx-background-color: linear-gradient(from 0%% 0%% to 100%% 100%%, #2E3A59 0%%, #1E2A45 100%%);
        -fx-text-fill: white;
        -fx-padding: 8px 14px;
        -fx-background-radius: 10px;
        -fx-font-size: 13px;
        -fx-font-weight: 700;
        -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
        -fx-cursor: hand;
        -fx-effect: dropshadow(gaussian, rgba(30,42,69,0.25), 10, 0.3, 0, 3);
    """);
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle().replace("0.25", "0.4")));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("0.4", "0.25")));
        return button;
    }

    private Button createGhostButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("""
        -fx-background-color: rgba(46,58,89,0.08);
        -fx-text-fill: #2E3A59;
        -fx-padding: 6px 12px;
        -fx-background-radius: 8px;
        -fx-font-size: 13px;
        -fx-font-weight: 600;
        -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
        -fx-cursor: hand;
        -fx-border-color: #C4C8D4;
        -fx-border-radius: 8px;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);
    """);
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace("0.08", "0.16")));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("0.16", "0.08")));
        return btn;
    }

    private Button createDangerGhostButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("""
        -fx-background-color: rgba(203,68,53,0.08);
        -fx-text-fill: #CB4435;
        -fx-padding: 6px 12px;
        -fx-background-radius: 8px;
        -fx-font-size: 13px;
        -fx-font-weight: 600;
        -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
        -fx-cursor: hand;
        -fx-border-color: #C4C8D4;
        -fx-border-radius: 8px;
        -fx-effect: dropshadow(gaussian, rgba(203,68,53,0.2), 8, 0, 0, 2);
    """);
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace("0.08", "0.16")));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("0.16", "0.08")));
        return btn;
    }

    private void styleRadioButton(RadioButton btn) {
        btn.setStyle("""
        -fx-background-color: #F6F7FA;
        -fx-border-color: #C4C8D4;
        -fx-border-radius: 8px;
        -fx-background-radius: 8px;
        -fx-padding: 4px 10px;
        -fx-font-size: 13px;
        -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
        -fx-text-fill: #2E3A59;
    """);
        btn.setPrefHeight(30);
    }

    private void styleToggleButton(ToggleButton toggle) {
        toggle.setStyle("""
        -fx-background-color: #F3F4F8;
        -fx-text-fill: #2E3A59;
        -fx-padding: 6px 12px;
        -fx-background-radius: 8px;
        -fx-font-size: 13px;
        -fx-font-weight: 600;
        -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
        -fx-cursor: hand;
        -fx-border-color: #C4C8D4;
        -fx-border-width: 1px;
        -fx-border-radius: 8px;
    """);

        toggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                toggle.setStyle("""
                -fx-background-color: linear-gradient(from 0%% 0%% to 100%% 100%%, #2E3A59 0%%, #1E2A45 100%%);
                -fx-text-fill: white;
                -fx-padding: 6px 12px;
                -fx-background-radius: 8px;
                -fx-font-size: 13px;
                -fx-font-weight: 700;
                -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
                -fx-cursor: hand;
                -fx-border-color: transparent;
                -fx-border-radius: 8px;
            """);
            } else {
                toggle.setStyle("""
                -fx-background-color: #F3F4F8;
                -fx-text-fill: #2E3A59;
                -fx-padding: 6px 12px;
                -fx-background-radius: 8px;
                -fx-font-size: 13px;
                -fx-font-weight: 600;
                -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
                -fx-cursor: hand;
                -fx-border-color: #C4C8D4;
                -fx-border-width: 1px;
                -fx-border-radius: 8px;
            """);
            }
        });
    }

    private void animateButtonPress(Button btn) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(100), btn);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(0.96);
        scale.setToY(0.96);
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
            -fx-background-color: %s;
            -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
        """.formatted(SURFACE));
        alert.showAndWait();
    }

    private void showModernInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("""
            -fx-background-color: %s;
            -fx-font-family: 'SF Pro Text','Segoe UI',system-ui;
        """.formatted(SURFACE));
        alert.showAndWait();
    }
}
