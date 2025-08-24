// App.java
package com.example.noteflowfrontend;

import com.example.noteflowfrontend.core.Auth;
import com.example.noteflowfrontend.pages.*;
import com.example.noteflowfrontend.shell.AppShell;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        AppShell shell = new AppShell();

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
            return new FoldersPage();
        });
        shell.router.mount("favorites", () -> {
            shell.setChromeVisible(true);
            return new FoldersPage();
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

        // first screen (choose one)
        shell.router.navigate("login");     // or "signup"

        stage.setTitle("NoteFlow");
        stage.setScene(new Scene(shell, 1120, 720));
        stage.show();
    }

    public static void main(String[] args) { launch(); }
}
