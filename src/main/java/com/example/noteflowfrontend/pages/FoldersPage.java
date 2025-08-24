package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.NoteApi;
import com.example.noteflowfrontend.core.dto.NoteDto;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class FoldersPage extends BorderPane {
    private final VBox listBox = new VBox(8);
    private final TextField search = new TextField();
    private final Button newTextBtn = new Button("New Text Note");
    private final Button newDrawBtn = new Button("New Drawing");

    private List<NoteDto> notes = new ArrayList<>();

    public FoldersPage() {
        setPadding(new Insets(16));

        var top = new HBox(10, new Label("My Notes"), new Region(), search, newTextBtn, newDrawBtn);
        HBox.setHgrow(top.getChildren().get(1), Priority.ALWAYS);
        search.setPromptText("Search notes...");
        setTop(top);

        var scroller = new ScrollPane(listBox);
        scroller.setFitToWidth(true);
        setCenter(scroller);

        newTextBtn.setOnAction(e -> openEditor(null, "text"));
        newDrawBtn.setOnAction(e -> openEditor(null, "draw"));

        search.textProperty().addListener((o, a, b) -> render());

        reload();
    }

    private void reload() {
        listBox.getChildren().clear();
        new Thread(() -> {
            try {
                List<NoteDto> res = NoteApi.list(); // active (not trashed)
                Platform.runLater(() -> {
                    notes = res;
                    render();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Load failed: " + ex.getMessage()).show());
            }
        }).start();
    }

    private void render() {
        listBox.getChildren().clear();
        String q = search.getText() == null ? "" : search.getText().trim().toLowerCase();
        for (var n : notes) {
            if (!q.isEmpty()) {
                if ((n.title() == null || !n.title().toLowerCase().contains(q)) &&
                        (n.textHtml() == null || !n.textHtml().toLowerCase().contains(q))) {
                    continue;
                }
            }
            listBox.getChildren().add(noteRow(n));
        }
    }

    private HBox noteRow(NoteDto n) {
        var title = new Label(n.title() == null ? "(Untitled)" : n.title());
        var open = new Button("Open");
        var fav = new Button(n.favorite() ? "Unfavorite" : "Favorite");
        var trash = new Button("Trash");

        open.setOnAction(e -> openEditor(n, n.drawingJson() != null ? "draw" : "text"));
        fav.setOnAction(e -> {
            new Thread(() -> {
                try { NoteApi.setFavorite(n.id(), !n.favorite()); reload(); } catch (Exception ex) { showErr(ex); }
            }).start();
        });
        trash.setOnAction(e -> {
            new Thread(() -> {
                try { NoteApi.setTrashed(n.id(), true); reload(); } catch (Exception ex) { showErr(ex); }
            }).start();
        });

        var row = new HBox(10, title, new Region(), open, fav, trash);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
        row.setPadding(new Insets(8, 0, 8, 0));
        row.setStyle("-fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
        return row;
    }

    private void openEditor(NoteDto existing, String startMode) {
        var dialog = new Dialog<Void>();
        dialog.setTitle(existing == null ? "New Note" : "Edit Note");
        var editor = new NoteEditorPage(existing, startMode);
        dialog.getDialogPane().setContent(editor);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefSize(980, 740);
        dialog.showAndWait();
        reload();
    }

    private void showErr(Exception ex) { Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).show()); }
}
