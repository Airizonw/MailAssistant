package com.mailassistant.desktop;

import javafx.scene.image.Image;
import javafx.stage.Stage;

/** Optional assets: missing icons must never prevent startup. */
public final class AppIconProvider {
    public void apply(Stage stage) {
        for (int size : new int[]{16, 32, 48, 128, 256}) {
            var url = getClass().getResource("/images/icons/app-" + size + ".png");
            if (url != null) { Image image = new Image(url.toExternalForm()); if (!image.isError()) stage.getIcons().add(image); }
        }
    }
}
