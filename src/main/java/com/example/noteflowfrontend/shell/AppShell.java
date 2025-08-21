package com.example.noteflowfrontend.shell;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class AppShell extends BorderPane {

    // palette
    private static final String C1 = "#DABFFF"; // lavender
    private static final String C2 = "#907AD6"; // purple
    private static final String C3 = "#4F518C"; // dark purple-blue
    private static final String C4 = "#2C2A4A"; // deep navy
    private static final String C5 = "#7FDEFF"; // cyan

    private final StackPane outlet = new StackPane();
    public final Router router = new Router(outlet);

    public AppShell() {
        setPrefSize(1100, 720);

        // sidebar (fixed)
        VBox side = new VBox(12);
        side.setPadding(new Insets(24));
        side.setPrefWidth(220);
        side.setStyle("-fx-background-color: white; -fx-border-color: rgba(0,0,0,0.06); -fx-border-width: 0 1 0 0;");

        Image logo = new Image(getClass().getResourceAsStream("/logo.png"));
        ImageView imageView = new ImageView(logo);
        imageView.setFitWidth(150);
        imageView.setFitHeight(100);
        Button btnNotes   = navBtn("My Notes");
        Button btnFav     = navBtn("Favorite");
        Button btnProfile = navBtn("Profile");
        Button btnTodos   = navBtn("To-Do");

        side.getChildren().addAll(imageView, spacer(10), btnNotes, btnFav, btnTodos, spacer(20), btnProfile);

        // top bar (fixed)
        HBox top = new HBox(12);
        top.setPadding(new Insets(16, 24, 16, 24));
        top.setAlignment(Pos.CENTER_LEFT);
        Label hello = new Label("Hello 👋");
        hello.setStyle("-fx-text-fill: " + C4 + "; -fx-font-size: 18; -fx-font-weight: bold;");
        Region grow = new Region(); HBox.setHgrow(grow, Priority.ALWAYS);
        TextField search = new TextField(); search.setPromptText("Search");
        search.setPrefWidth(260);

        top.getChildren().addAll(hello, grow, search);
        top.setStyle("-fx-background-color: linear-gradient(to bottom, #FFFFFF, #F7F8FF);");

        // center outlet (content only)
        outlet.setPadding(new Insets(24));
        outlet.setStyle("-fx-background-color: #FFFFFF;");

        setLeft(side);
        setTop(top);
        setCenter(outlet);

        btnNotes.setOnAction(e -> router.navigate("folders"));
        btnFav.setOnAction(e -> router.navigate("favorites"));
        btnTodos.setOnAction(e -> router.navigate("todos"));
        btnProfile.setOnAction(e -> router.navigate("profile"));
    }

    public StackPane outlet() { return outlet; }

    private Button navBtn(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle("""
            -fx-background-radius: 12;
            -fx-background-color: transparent;
            -fx-text-fill: %s;
            -fx-font-size: 14;
            """.formatted(C3));
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color:" + C1 + "; -fx-background-radius:12; -fx-text-fill:" + C4 + "; -fx-font-size:14;"));
        b.setOnMouseExited (e -> b.setStyle("-fx-background-color: transparent; -fx-text-fill:" + C3 + "; -fx-font-size:14;"));
        return b;
    }
    private Node spacer(double h) { Region r = new Region(); r.setMinHeight(h); return r; }
}
