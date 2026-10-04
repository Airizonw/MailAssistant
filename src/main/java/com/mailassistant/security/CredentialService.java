package com.mailassistant.security;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

public final class CredentialService {
    private final SecretStore store;
    public CredentialService(SecretStore store) { this.store = store; }
    public CryptoService crypto() throws Exception {
        byte[] key = store.load("master-key");
        if (key == null) { key = new byte[32]; new SecureRandom().nextBytes(key); store.save("master-key", key); }
        try { return new AesGcmCryptoService(key); } finally { Arrays.fill(key, (byte) 0); }
    }
    private String name(String address) throws Exception {
        return "smtp-" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(address.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8)));
    }
    public void save(String address, String secret) throws Exception { store.save(name(address), secret.getBytes(StandardCharsets.UTF_8)); }
    public String load(String address) throws Exception {
        byte[] value = store.load(name(address));
        if (value == null) return "";
        try { return new String(value, StandardCharsets.UTF_8); } finally { Arrays.fill(value, (byte) 0); }
    }
    public void delete(String address) throws Exception { store.delete(name(address)); }
}
