package com.mailassistant.storage.sqlite;

import java.nio.file.*;
import java.sql.*;

public final class DatabaseManager {
    private final Path path;
    public DatabaseManager(Path path) throws Exception {
        this.path = path.toAbsolutePath();
        Files.createDirectories(this.path.getParent());
        try (var stream = getClass().getResourceAsStream("/database/exercise.db")) {
            if (stream == null) throw new java.io.IOException("缺少内置习题库");
            Path candidate = Files.createTempFile(this.path.getParent(), "exercise-", ".tmp");
            try {
                Files.copy(stream, candidate, StandardCopyOption.REPLACE_EXISTING);
                String bundledVersion = validate(candidate);
                boolean replace = !Files.exists(this.path);
                if (!replace) {
                    replace = newer(bundledVersion, validate(this.path));
                    if (replace) {
                        byte[] old = Files.readAllBytes(this.path);
                        String hash = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(old));
                        Path backups = this.path.getParent().resolve("backups");
                        Files.createDirectories(backups);
                        Path backup = backups.resolve("exercise-" + hash + ".db");
                        if (!Files.exists(backup)) Files.write(backup, old, StandardOpenOption.CREATE_NEW);
                        if (!java.util.Arrays.equals(old, Files.readAllBytes(backup)))
                            throw new java.io.IOException("旧题库备份校验失败");
                    }
                }
                // All SQLite connections are closed before replacing the runtime copy.
                if (replace) {
                    try { Files.move(candidate, this.path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
                    catch (AtomicMoveNotSupportedException e) { Files.move(candidate, this.path, StandardCopyOption.REPLACE_EXISTING); }
                }
            } finally { Files.deleteIfExists(candidate); }
        }
    }

    private static String validate(Path file) throws SQLException {
        try (var c = openReadOnly(file); var st = c.createStatement()) {
            try (var result = st.executeQuery("PRAGMA quick_check")) {
                if (!result.next() || !"ok".equals(result.getString(1))) throw new SQLException("习题库完整性检查失败");
            }
            try (var result = st.executeQuery("PRAGMA foreign_key_check")) {
                if (result.next()) throw new SQLException("习题库资源关联检查失败");
            }
            try (var result = st.executeQuery("SELECT value FROM metadata WHERE key='schema_version'")) {
                if (!result.next() || !"1".equals(result.getString(1))) throw new SQLException("不支持的题库结构版本");
            }
            try (var result = st.executeQuery("SELECT value FROM metadata WHERE key='content_version'")) {
                if (!result.next()) throw new SQLException("缺少题库内容版本");
                return result.getString(1);
            }
        }
    }

    private static boolean newer(String bundled, String installed) {
        // Preserve unknown/custom versions rather than silently overwriting them.
        if (bundled == null || installed == null || !bundled.matches("\\d+(\\.\\d+)*") || !installed.matches("\\d+(\\.\\d+)*")) return false;
        String[] a = bundled.split("\\."), b = installed.split("\\.");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            var x = new java.math.BigInteger(i < a.length ? a[i] : "0");
            var y = new java.math.BigInteger(i < b.length ? b[i] : "0");
            int compare = x.compareTo(y);
            if (compare != 0) return compare > 0;
        }
        return false;
    }

    public Connection connect() throws SQLException { return openReadOnly(path); }

    private static Connection openReadOnly(Path file) throws SQLException {
        var props = new java.util.Properties(); props.setProperty("open_mode", "1");
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file.toAbsolutePath(), props);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON"); statement.execute("PRAGMA query_only = ON");
        } catch (SQLException e) { connection.close(); throw e; }
        return connection;
    }
}
