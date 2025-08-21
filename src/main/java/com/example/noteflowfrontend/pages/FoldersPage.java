package com.example.noteflowfrontend.pages;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class FoldersPage extends BorderPane {
    public FoldersPage() {
        setPadding(new Insets(16));
        setCenter(new Label("Folders grid/list goes here"));
    }
}
