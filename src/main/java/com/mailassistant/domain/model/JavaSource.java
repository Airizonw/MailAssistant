package com.mailassistant.domain.model;

public class JavaSource {
    public String name = "", code = "";
    public JavaSource() {}
    public JavaSource(String name, String code) { this.name = name; this.code = code; }
    public String fileName() {
        String value = name.strip();
        return value.isEmpty() || value.toLowerCase(java.util.Locale.ROOT).endsWith(".java") ? value : value + ".java";
    }
}
