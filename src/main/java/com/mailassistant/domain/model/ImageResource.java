package com.mailassistant.domain.model;

public record ImageResource(String id, String name, String mimeType, byte[] data, int width, int height) {}
