package com.mailassistant.security;

public interface SecretStore {
    void save(String name, byte[] value) throws Exception;
    /** Returns a caller-owned plaintext buffer, or null; caller may erase the buffer. */
    byte[] load(String name) throws Exception;
    void delete(String name) throws Exception;
}
