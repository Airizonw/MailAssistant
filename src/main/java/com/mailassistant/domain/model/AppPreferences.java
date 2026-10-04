package com.mailassistant.domain.model;

import java.nio.file.*;

/** Application preferences are global, not part of a coursework draft. */
public class AppPreferences {
    public boolean autoDeleteDraft = false;
    public String homeworkFolder = "";
    public boolean minimizeToTray = true;

    public Path existingHomeworkFolder() {
        try {
            if (homeworkFolder == null || homeworkFolder.isBlank()) return null;
            Path path = Path.of(homeworkFolder);
            return path.isAbsolute() && Files.isDirectory(path) ? path : null;
        } catch (InvalidPathException e) { return null; }
    }
}
