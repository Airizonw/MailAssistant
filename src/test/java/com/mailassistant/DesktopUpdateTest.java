package com.mailassistant;

import com.mailassistant.desktop.SingleInstanceGuard;
import com.mailassistant.storage.sqlite.DatabaseManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.sql.DriverManager;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class DesktopUpdateTest {
    @TempDir Path temp;

    @Test void duplicateInstanceIsRejectedAndCrashReleasesLock() throws Exception {
        Path lock = temp.resolve("app.lock");
        try (var first = SingleInstanceGuard.tryAcquire(lock)) {
            assertNotNull(first);
            assertNull(SingleInstanceGuard.tryAcquire(lock));
            assertEquals(2, probe(lock).waitFor());
        }
        Process owner = probe(lock, "hold");
        try {
            assertEquals("locked", owner.inputReader().readLine());
            assertNull(SingleInstanceGuard.tryAcquire(lock));
        } finally { owner.destroyForcibly(); assertTrue(owner.waitFor(10, TimeUnit.SECONDS)); }
        // Windows may signal process termination before finishing handle cleanup.
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(5), () -> {
            while (true) {
                try (var restarted = SingleInstanceGuard.tryAcquire(lock)) {
                    if (restarted != null) break;
                }
                Thread.sleep(25);
            }
        });
    }

    private Process probe(Path lock, String... extra) throws Exception {
        var args = new java.util.ArrayList<String>();
        args.add(Path.of(System.getProperty("java.home"), "bin", "java.exe").toString());
        args.add("-cp"); args.add(System.getProperty("java.class.path"));
        args.add(LockProbe.class.getName()); args.add(lock.toString());
        args.addAll(java.util.List.of(extra));
        return new ProcessBuilder(args).redirectErrorStream(true).start();
    }

    public static class LockProbe {
        public static void main(String[] args) throws Exception {
            try (var guard = SingleInstanceGuard.tryAcquire(Path.of(args[0]))) {
                if (guard == null) { System.exit(2); return; }
                System.out.println("locked"); System.out.flush();
                if (args.length > 1) Thread.sleep(30000);
            }
        }
    }

    @Test void oldBankIsUpgradedAndBackedUpWithoutTouchingUserData() throws Exception {
        Path db = temp.resolve("database/exercise.db");
        new DatabaseManager(db);
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + db); var st = c.createStatement()) {
            st.executeUpdate("UPDATE metadata SET value='2026.10.02.1' WHERE key='content_version'");
            st.executeUpdate("DELETE FROM exercise_asset WHERE exercise_id>=5000");
            st.executeUpdate("DELETE FROM exercise WHERE chapter_id>=5");
            st.executeUpdate("DELETE FROM chapter WHERE id>=5");
        }
        byte[] before = Files.readAllBytes(db);
        Path draft = temp.resolve("draft.enc"); Files.writeString(draft, "synthetic encrypted draft");
        var manager = new DatabaseManager(db);
        try (var c = manager.connect(); var st = c.createStatement(); var rows = st.executeQuery("SELECT count(*) FROM exercise")) {
            assertTrue(rows.next()); assertEquals(173, rows.getInt(1));
        }
        try (var backups = Files.list(db.getParent().resolve("backups"))) {
            var files = backups.toList(); assertEquals(1, files.size());
            assertArrayEquals(before, Files.readAllBytes(files.getFirst()));
        }
        assertEquals("synthetic encrypted draft", Files.readString(draft));
        byte[] updated = Files.readAllBytes(db);
        new DatabaseManager(db);
        assertArrayEquals(updated, Files.readAllBytes(db));
    }

    @Test void newerAndCustomBanksAreNotOverwritten() throws Exception {
        Path db = temp.resolve("exercise.db"); new DatabaseManager(db);
        for (String version : new String[]{"2099.1.1.1", "custom-bank"}) {
            try (var c = DriverManager.getConnection("jdbc:sqlite:" + db); var st = c.prepareStatement("UPDATE metadata SET value=? WHERE key='content_version'")) {
                st.setString(1, version); st.executeUpdate();
            }
            byte[] before = Files.readAllBytes(db);
            new DatabaseManager(db);
            assertArrayEquals(before, Files.readAllBytes(db));
        }
    }
}
