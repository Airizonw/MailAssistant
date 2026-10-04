package com.mailassistant.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private JsonUtil() {}
    public static <T> T copy(T value, Class<T> type) {
        return MAPPER.convertValue(value, type);
    }
}
