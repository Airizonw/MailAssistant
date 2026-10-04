package com.mailassistant.security;

public interface CryptoService {
    byte[] encrypt(byte[] plaintext) throws java.security.GeneralSecurityException;
    byte[] decrypt(byte[] ciphertext) throws java.security.GeneralSecurityException;
}
