package com.example.noteflowfrontend;


import com.example.noteflowfrontend.pages.FoldersPage;
import com.example.noteflowfrontend.pages.TodosPage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.example.noteflowfrontend.pages.ProfilePage;
import com.example.noteflowfrontend.shell.AppShell;

public class App extends Application {
    @Override public void start(Stage stage) {
        AppShell shell = new AppShell();

        shell.router.mount("folders", () -> new FoldersPage());
        shell.router.mount("favorites", () -> new FoldersPage());
        shell.router.mount("todos", () -> new TodosPage());
        shell.router.mount("profile", () -> new ProfilePage());   // profile editor

        shell.router.navigate("folders");

        stage.setTitle("NoteFlow");
        stage.setScene(new Scene(shell));
        stage.show();
    }
    public static void main(String[] args) { launch(); }
}
