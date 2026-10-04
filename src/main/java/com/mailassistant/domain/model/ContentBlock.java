package com.mailassistant.domain.model;

import java.util.ArrayList;
import java.util.List;

/** Shared, editable document format. Images are referenced by opaque identifiers, never paths/URLs. */
public class ContentBlock {
    public long exerciseId;
    public String role = "";
    public String latex = "", number = "", language = "";
    public String type = "paragraph", text = "", assetId = "", caption = "";
    public List<Inline> inlines = new ArrayList<>();
    public List<List<List<Inline>>> rows = new ArrayList<>();
    public record Inline(String text, String assetId, String alt, String latex) {
        public Inline(String text, String assetId, String alt) { this(text, assetId, alt, ""); }
        public static Inline text(String text) { return new Inline(text, "", ""); }
    }
    public static ContentBlock text(String type, String text) {
        ContentBlock b = new ContentBlock(); b.type = type; b.text = text; return b;
    }
    public static ContentBlock image(String id, String caption) {
        ContentBlock b = text("image", ""); b.assetId = id; b.caption = caption; return b;
    }
    public String searchableText() {
        StringBuilder out = new StringBuilder(text).append(' ').append(caption).append(' ').append(latex);
        for (var i : inlines) out.append(' ').append(i.text() == null ? i.alt() + " " + i.latex() : i.text());
        for (var row : rows) for (var cell : row) for (var i : cell) out.append(' ').append(i.text() == null ? i.alt() + " " + i.latex() : i.text());
        return out.toString();
    }
    @Override public String toString() {
        String label = switch (type) { case "image" -> "图片 · " + caption; case "code" -> "代码 · " + text;
            case "table" -> "表格 · " + caption; default -> text.isEmpty() ? inlines.stream().map(i -> i.text() == null ? i.alt() : i.text()).reduce("", String::concat) : text; };
        return label.replace('\n', ' ').substring(0, Math.min(65, label.length()));
    }
}
