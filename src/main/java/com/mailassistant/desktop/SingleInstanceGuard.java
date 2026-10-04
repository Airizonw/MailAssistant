package com.mailassistant.desktop;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** OS-owned lock: released even if the process crashes. Never delete the lock file. */
public final class SingleInstanceGuard implements AutoCloseable {
    private final FileChannel channel;
    private final FileLock lock;

    private SingleInstanceGuard(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    public static SingleInstanceGuard tryAcquire(Path file) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            FileLock lock;
            try { lock = channel.tryLock(); }
            catch (OverlappingFileLockException alreadyRunning) { lock = null; }
            if (lock != null) return new SingleInstanceGuard(channel, lock);
            channel.close();
            return null;
        } catch (IOException | RuntimeException e) {
            channel.close();
            throw e;
        }
    }

    @Override public void close() throws IOException {
        try { if (lock.isValid()) lock.release(); }
        finally { channel.close(); }
    }
}
