package com.mailassistant;

import com.mailassistant.application.AppServices;
import com.mailassistant.config.PathConfig;
import com.mailassistant.desktop.*;
import com.mailassistant.ui.controller.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.concurrent.Task;

public final class MailAssistantApplication extends Application {
    private MainController controller;
    private DesktopLifecycle lifecycle;
    private SingleInstanceGuard instance;
    @Override public void start(Stage stage) {
        final PathConfig paths;
        try {
            paths = PathConfig.discover();
            instance = SingleInstanceGuard.tryAcquire(paths.data().resolve("app.lock"));
            if (instance == null) { javafx.application.Platform.exit(); return; }
        } catch (Exception e) { startupError(stage); return; }
        stage.setTitle("MailAssistant · 课程作业邮件助手");
        stage.setMinWidth(1040); stage.setMinHeight(720);
        stage.setScene(new Scene(new StackPane(new Label("正在准备离线题库与安全存储…")), 1280, 860));
        new AppIconProvider().apply(stage); stage.show();
        Task<AppServices> boot = new Task<>() { protected AppServices call() throws Exception { return new AppServices(paths); } };
        boot.setOnSucceeded(event -> {
            try {
                controller = new MainController(stage, boot.getValue());
                Scene scene = new Scene(controller.root(), 1280, 860);
                scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
                stage.setScene(scene);
                lifecycle = new DesktopLifecycle(stage, new AwtTrayService(stage), controller::mayExit,
                        controller::showProfile, controller::showDrafts, controller::minimizeToTray);
                controller.start();
            } catch (Exception e) { startupError(stage); }
        });
        boot.setOnFailed(event -> startupError(stage));
        Thread thread = new Thread(boot, "app-startup"); thread.setDaemon(true); thread.start();
    }
    private void startupError(Stage stage) {
        Alert error = new Alert(Alert.AlertType.ERROR, "无法初始化本地数据。请确认程序位于可写目录、题库完整，并使用原 Windows 账户打开已有加密数据。\n本版本的安全存储需要 Windows。", ButtonType.CLOSE);
        error.initOwner(stage); error.setHeaderText("启动失败"); error.showAndWait();
        if (lifecycle != null) lifecycle.close();
        javafx.application.Platform.exit();
    }
    @Override public void stop() throws Exception {
        try { if (controller != null) controller.close(); }
        finally {
            try { if (lifecycle != null) lifecycle.close(); }
            finally { if (instance != null) instance.close(); }
        }
    }
    public static void main(String[] args) { launch(args); }
}
