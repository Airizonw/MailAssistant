package com.mailassistant.storage.encrypted;

import com.mailassistant.repository.UserProfileRepository;
import com.mailassistant.domain.model.*;
import com.mailassistant.security.CryptoService;
import com.mailassistant.util.*;
import java.nio.file.*;

public final class EncryptedProfileRepository implements UserProfileRepository {
    private final Path path;
    private final CryptoService crypto;
    public EncryptedProfileRepository(Path path, CryptoService crypto) { this.path = path; this.crypto = crypto; }
    public Settings load() throws Exception {
        return Files.exists(path) ? JsonUtil.MAPPER.readValue(crypto.decrypt(Files.readAllBytes(path)), Settings.class) : new Settings(new UserProfile(), new MailAccount());
    }
    public void save(Settings settings) throws Exception { FileUtil.atomicWrite(path, crypto.encrypt(JsonUtil.MAPPER.writeValueAsBytes(settings))); }
    public void delete() throws Exception { Files.deleteIfExists(path); }
}
