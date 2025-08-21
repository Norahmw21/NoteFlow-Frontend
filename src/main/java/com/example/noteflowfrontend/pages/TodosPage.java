package com.example.noteflowfrontend.pages;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class TodosPage extends BorderPane {
    public TodosPage() {
        setPadding(new Insets(16));
        setCenter(new Label("To-Do page here"));
    }
}