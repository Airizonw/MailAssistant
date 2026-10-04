package com.mailassistant.config;

import java.nio.file.*;
import java.net.URISyntaxException;

public record PathConfig(Path root) {
    public Path data() { return root.resolve("data"); }
    public Path config() { return data().resolve("config"); }
    public Path drafts() { return data().resolve("drafts"); }
    public Path database() { return data().resolve("database/exercise.db"); }
    public Path exports() { return data().resolve("export"); }
    public void initialize() throws java.io.IOException {
        for (Path p : new Path[]{config(), drafts(), database().getParent(), exports(), data().resolve("cache"), data().resolve("logs")}) Files.createDirectories(p);
        Path probe = Files.createTempFile(data(), ".writable-", ".tmp"); Files.delete(probe);
    }
    public static PathConfig discover() throws URISyntaxException {
        String override = System.getProperty("mailassistant.home");
        if (override != null) {
            Path path = Path.of(override);
            if (!path.isAbsolute()) throw new IllegalArgumentException("mailassistant.home 必须为绝对路径");
            return new PathConfig(path.normalize());
        }
        String launcher = System.getProperty("jpackage.app-path");
        if (launcher != null) return new PathConfig(Path.of(launcher).toAbsolutePath().getParent());
        Path location = Path.of(PathConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (Files.isDirectory(location) && location.endsWith(Path.of("target", "classes"))) return new PathConfig(location.getParent().getParent());
        return new PathConfig(Files.isDirectory(location) ? location : location.getParent());
    }
}
