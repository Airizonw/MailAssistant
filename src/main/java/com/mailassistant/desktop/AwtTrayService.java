package com.mailassistant.desktop;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BaseMultiResolutionImage;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/** Owns the native tray icon; all AWT operations run on the event dispatch thread. */
public final class AwtTrayService implements TrayService {
    private volatile TrayIcon icon;
    private final javafx.stage.Stage owner;
    private TrayMenu menu;

    public AwtTrayService(javafx.stage.Stage owner) { this.owner = owner; }

    @Override public boolean available() { return icon != null; }

    @Override public void install(Runnable showWindow, Runnable showProfile, Runnable showDrafts, Runnable quit) {
        onAwt(() -> {
            if (icon != null || !SystemTray.isSupported()) return;
            try {
                var images = new ArrayList<Image>();
                for (int size : new int[]{16, 32, 48, 128, 256}) {
                    try (var stream = getClass().getResourceAsStream("/images/icons/app-" + size + ".png")) {
                        if (stream != null) {
                            Image image = ImageIO.read(stream);
                            if (image != null) images.add(image);
                        }
                    }
                }
                if (images.isEmpty()) return;
                TrayIcon candidate = new TrayIcon(new BaseMultiResolutionImage(images.toArray(Image[]::new)),
                        "MailAssistant · 课程作业邮件助手");
                candidate.setImageAutoSize(true);
                candidate.addMouseListener(new MouseAdapter() {
                    @Override public void mouseReleased(MouseEvent event) {
                        if (event.getButton() == MouseEvent.BUTTON3) javafx.application.Platform.runLater(() -> {
                            if (!available()) return;
                            if (menu == null) menu = new TrayMenu(owner, showWindow, showProfile, showDrafts, quit);
                            // JavaFX mouse coordinates match JavaFX screen bounds even with display scaling.
                            menu.showAt(new javafx.scene.robot.Robot().getMousePosition());
                        });
                    }
                    @Override public void mouseClicked(MouseEvent event) {
                        if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) showWindow.run();
                    }
                });
                SystemTray.getSystemTray().add(candidate);
                icon = candidate;
            } catch (Exception e) {
                System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, "无法安装系统托盘，保留正常窗口退出行为", e);
            }
        });
    }

    @Override public void close() {
        Runnable hideMenu = () -> { if (menu != null) { menu.close(); menu = null; } };
        if (javafx.application.Platform.isFxApplicationThread()) hideMenu.run();
        else javafx.application.Platform.runLater(hideMenu);
        onAwt(() -> {
            if (icon != null) { SystemTray.getSystemTray().remove(icon); icon = null; }
        });
    }

    private static void onAwt(Runnable action) {
        try {
            if (EventQueue.isDispatchThread()) action.run();
            else EventQueue.invokeAndWait(action);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (java.lang.reflect.InvocationTargetException e) {
            System.getLogger(AwtTrayService.class.getName()).log(System.Logger.Level.WARNING, "系统托盘操作失败", e.getCause());
        }
    }
}
