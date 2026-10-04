package com.mailassistant.util;

import java.nio.file.*;
import java.io.IOException;

public final class FileUtil {
    private FileUtil() {}
    public static void atomicWrite(Path target, byte[] bytes) throws IOException {
        Files.createDirectories(target.getParent());
        Path temp = Files.createTempFile(target.getParent(), ".write-", ".tmp");
        try {
            Files.write(temp, bytes);
            try { Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }
}
