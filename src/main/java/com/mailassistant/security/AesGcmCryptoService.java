package com.mailassistant.security;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;

public final class AesGcmCryptoService implements CryptoService {
    private static final byte[] HEADER = {'M', 'A', 'E', 1};
    private final SecretKeySpec key;
    public AesGcmCryptoService(byte[] key) {
        if (key.length != 32) throw new IllegalArgumentException("需要 256 位主密钥");
        this.key = new SecretKeySpec(key, "AES");
    }
    public byte[] encrypt(byte[] plaintext) throws GeneralSecurityException {
        byte[] iv = new byte[12]; new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv)); cipher.updateAAD(HEADER);
        byte[] encrypted = cipher.doFinal(plaintext);
        return ByteBuffer.allocate(16 + encrypted.length).put(HEADER).put(iv).put(encrypted).array();
    }
    public byte[] decrypt(byte[] ciphertext) throws GeneralSecurityException {
        if (ciphertext.length < 32 || !Arrays.equals(HEADER, Arrays.copyOf(ciphertext, 4))) throw new GeneralSecurityException("加密文件格式无效");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, Arrays.copyOfRange(ciphertext, 4, 16))); cipher.updateAAD(HEADER);
        return cipher.doFinal(ciphertext, 16, ciphertext.length - 16);
    }
}
