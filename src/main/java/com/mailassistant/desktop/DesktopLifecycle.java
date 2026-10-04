package com.mailassistant.desktop;

import javafx.application.Platform;
import javafx.stage.Stage;
import java.util.function.BooleanSupplier;

public final class DesktopLifecycle implements AutoCloseable {
    private final TrayService tray;
    private boolean closed;
    public DesktopLifecycle(Stage stage, TrayService tray, BooleanSupplier mayExit,
                            Runnable showProfile, Runnable showDrafts) {
        this(stage, tray, mayExit, showProfile, showDrafts, () -> true);
    }
    public DesktopLifecycle(Stage stage, TrayService tray, BooleanSupplier mayExit,
                            Runnable showProfile, Runnable showDrafts, BooleanSupplier minimizeToTray) {
        this.tray = tray;
        Runnable restore = () -> { stage.show(); stage.setIconified(false); stage.toFront(); stage.requestFocus(); };
        Runnable quit = () -> Platform.runLater(() -> {
            if (closed) return;
            if (mayExit.getAsBoolean()) { close(); Platform.exit(); }
        });
        tray.install(onFx(restore, () -> {}), onFx(restore, showProfile), onFx(restore, showDrafts), quit);
        Platform.setImplicitExit(!tray.available());
        stage.setOnCloseRequest(event -> {
            if (tray.available() && minimizeToTray.getAsBoolean()) { event.consume(); stage.hide(); }
            else if (!mayExit.getAsBoolean()) event.consume();
            else { close(); Platform.exit(); }
        });
    }
    private Runnable onFx(Runnable restore, Runnable action) {
        return () -> Platform.runLater(() -> { if (!closed) { restore.run(); action.run(); } });
    }
    public void close() { closed = true; tray.close(); }
}
