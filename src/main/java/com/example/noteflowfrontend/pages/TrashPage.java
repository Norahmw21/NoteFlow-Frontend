package com.example.noteflowfrontend.pages;

import com.example.noteflowfrontend.core.NoteApi;
import com.example.noteflowfrontend.core.dto.NoteDto;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class TrashPage extends BorderPane {
    private final VBox list = new VBox(8);

    public TrashPage() {
        setPadding(new Insets(16));
        setTop(new Label("Trash"));

        var scroller = new ScrollPane(list);
        scroller.setFitToWidth(true);
        setCenter(scroller);

        reload();
    }

    private void reload() {
        list.getChildren().clear();
        new Thread(() -> {
            try {
                List<NoteDto> notes = NoteApi.listTrash();
                Platform.runLater(() -> notes.forEach(n -> list.getChildren().add(row(n))));
            } catch (Exception ex) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).show());
            }
        }).start();
    }

    private HBox row(NoteDto n) {
        var title = new Label(n.title() == null ? "(Untitled)" : n.title());
        var restore = new Button("Restore");
        var deleteForever = new Button("Delete Forever");

        restore.setOnAction(e -> new Thread(() -> {
            try { NoteApi.setTrashed(n.id(), false); reload(); } catch (Exception ex) { showErr(ex); }
        }).start());

        deleteForever.setOnAction(e -> new Thread(() -> {
            try { NoteApi.deletePermanent(n.id()); reload(); } catch (Exception ex) { showErr(ex); }
        }).start());

        var row = new HBox(10, title, new Region(), restore, deleteForever);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
        row.setPadding(new Insets(8, 0, 8, 0));
        row.setStyle("-fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
        return row;
    }

    private void showErr(Exception ex) { Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).show()); }
}
