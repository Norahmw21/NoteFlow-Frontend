package com.example.noteflowfrontend.shell;


import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Router {
    private final StackPane outlet;
    private final Map<String, Supplier<Node>> routes = new HashMap<>();

    public Router(StackPane outlet) { this.outlet = outlet; }

    public void mount(String name, Supplier<Node> factory) {
        routes.put(name, factory);
    }

    public void navigate(String name) {
        Supplier<Node> f = routes.get(name);
        if (f == null) throw new IllegalArgumentException("Route not found: " + name);
        outlet.getChildren().setAll(f.get());
    }
}
