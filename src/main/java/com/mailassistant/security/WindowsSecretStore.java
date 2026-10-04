package com.mailassistant.security;

import com.sun.jna.platform.win32.Crypt32Util;
import com.mailassistant.util.FileUtil;
import java.nio.file.*;

/** DPAPI current-user protection: moving the application to another Windows account cannot unlock it. */
public final class WindowsSecretStore implements SecretStore {
    private final Path directory;
    public WindowsSecretStore(Path directory) {
        if (!System.getProperty("os.name").startsWith("Windows")) throw new IllegalStateException("当前版本的安全凭据存储仅支持 Windows");
        this.directory = directory;
    }
    private Path path(String name) {
        if (!name.matches("[a-zA-Z0-9-]+")) throw new IllegalArgumentException("非法凭据名称");
        return directory.resolve(name + ".dpapi");
    }
    public void save(String name, byte[] value) throws Exception { FileUtil.atomicWrite(path(name), Crypt32Util.cryptProtectData(value)); }
    public byte[] load(String name) throws Exception { return Files.exists(path(name)) ? Crypt32Util.cryptUnprotectData(Files.readAllBytes(path(name))) : null; }
    public void delete(String name) throws Exception { Files.deleteIfExists(path(name)); }
}
