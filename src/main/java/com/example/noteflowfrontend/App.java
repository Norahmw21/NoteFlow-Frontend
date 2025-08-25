// App.java
package com.example.noteflowfrontend;

import com.example.noteflowfrontend.core.Auth;
import com.example.noteflowfrontend.pages.*;
import com.example.noteflowfrontend.shell.AppShell;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        AppShell shell = new AppShell();
        Image icon = new Image(getClass().getResourceAsStream("/icon.png"));

        // --- AUTH ROUTES (NO SIDEBAR/TOPBAR)
        shell.router.mount("login", () -> {
            shell.setChromeVisible(false);
            return new com.example.noteflowfrontend.pages.LoginPage(shell.router).getRoot();
        });
        shell.router.mount("signup", () -> {
            shell.setChromeVisible(false);
            return new com.example.noteflowfrontend.pages.SignupPage(shell.router).getRoot();
        });

        // --- APP ROUTES (WITH SIDEBAR/TOPBAR)
        shell.router.mount("folders", () -> {
            shell.setChromeVisible(true);
            return new FoldersPage();   // My Notes
        });
        shell.router.mount("favorites", () -> {
            shell.setChromeVisible(true);
            return new FavoritesPage();
        });
        shell.router.mount("trash", () -> {
            shell.setChromeVisible(true);
            return new TrashPage();
        });
        shell.router.mount("todos", () -> {
            shell.setChromeVisible(true);
            return new TodosPage();
        });
        shell.router.mount("profile", () -> {
            shell.setChromeVisible(true);
            return new ProfilePage();
        });
        shell.router.mount("gpa-calculator", () -> {
            shell.setChromeVisible(true);
            return new GpaCalculatorPage();
        });
        shell.router.mount("Ai", () -> {
            shell.setChromeVisible(true);
            return new ChatPage();
        });


        // first screen (choose one)
        shell.router.navigate("login");     // or "signup"

        stage.setTitle("NoteFlow");
        stage.getIcons().add(icon);
        stage.setScene(new Scene(shell, 1120, 720));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}