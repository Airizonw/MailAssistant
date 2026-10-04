package com.mailassistant.ui.component;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class Ui {
    private Ui() {}
    public static Label label(String text, String style) { Label l = new Label(text); l.getStyleClass().add(style); l.setWrapText(true); return l; }
    public static Button button(String text, String style, Runnable action) { Button b = new Button(text); if (!style.isEmpty()) b.getStyleClass().add(style); b.setOnAction(e -> action.run()); return b; }
    public static VBox card(Node... children) { VBox box = new VBox(14, children); box.getStyleClass().add("card"); return box; }
    public static HBox row(Node... nodes) { HBox row = new HBox(10, nodes); row.setAlignment(javafx.geometry.Pos.CENTER_LEFT); return row; }
    public static Region spacer() { Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS); return r; }
    public static ScrollPane scroll(Node content) { ScrollPane s = new ScrollPane(content); s.setFitToWidth(true); s.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); return s; }
    public static VBox field(String title, Node input) { VBox v = new VBox(7, label(title, "field-label"), input); HBox.setHgrow(v, Priority.ALWAYS); return v; }
}
