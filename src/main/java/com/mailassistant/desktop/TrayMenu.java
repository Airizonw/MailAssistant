package com.mailassistant.desktop;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/** JavaFX renders Chinese text and rounded controls consistently with the main window. */
public final class TrayMenu implements AutoCloseable {
    private final Stage popup = new Stage(StageStyle.TRANSPARENT);

    public TrayMenu(Stage owner, Runnable showWindow, Runnable showProfile, Runnable showDrafts, Runnable quit) {
        popup.initOwner(owner);
        popup.setAlwaysOnTop(true);
        popup.setResizable(false);
        VBox card = new VBox(6);
        card.getStyleClass().add("tray-card");
        ImageView logo = new ImageView(new Image(getClass().getResource("/images/icons/app-48.png").toExternalForm()));
        logo.setFitWidth(32); logo.setFitHeight(32);
        Label title = new Label("MailAssistant"); title.getStyleClass().add("tray-title");
        Label subtitle = new Label("课程作业邮件助手"); subtitle.getStyleClass().add("tray-subtitle");
        HBox header = new HBox(10, logo, new VBox(2, title, subtitle));
        header.getStyleClass().add("tray-header");
        card.getChildren().addAll(header, item("主界面", showWindow, true),
                item("个人信息", showProfile, false), item("草稿箱", showDrafts, false),
                new Separator(), item("退出", quit, false));
        VBox outer = new VBox(card); outer.getStyleClass().add("tray-root");
        Scene scene = new Scene(outer); scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        scene.setOnKeyPressed(event -> { if (event.getCode() == KeyCode.ESCAPE) popup.hide(); });
        popup.setScene(scene);
        popup.focusedProperty().addListener((o, oldValue, focused) -> { if (!focused) popup.hide(); });
    }

    private Button item(String title, Runnable action, boolean primary) {
        Button button = new Button(title);
        button.getStyleClass().add("tray-item");
        if (primary) button.getStyleClass().add("primary");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> { popup.hide(); action.run(); });
        return button;
    }

    public void showAt(Point2D point) {
        // A never-shown Stage has NaN dimensions. Measure the styled content first.
        var root = popup.getScene().getRoot();
        root.applyCss();
        double width = Math.ceil(root.prefWidth(-1));
        double height = Math.ceil(root.prefHeight(width));
        root.resize(width, height);
        root.layout();
        popup.setWidth(width);
        popup.setHeight(height);
        position(point, width, height);
        popup.show();
        // Account for native-window sizing/DPI changes after its peer is created.
        position(point, popup.getWidth(), popup.getHeight());
        popup.toFront(); popup.requestFocus();
    }

    private void position(Point2D point, double width, double height) {
        Rectangle2D bounds = Screen.getScreensForRectangle(point.getX(), point.getY(), 1, 1)
                .stream().findFirst().orElse(Screen.getPrimary()).getVisualBounds();
        popup.setX(Math.max(bounds.getMinX(), Math.min(point.getX() - width, bounds.getMaxX() - width)));
        popup.setY(Math.max(bounds.getMinY(), Math.min(point.getY() - height, bounds.getMaxY() - height)));
    }

    @Override public void close() { popup.close(); }
}
