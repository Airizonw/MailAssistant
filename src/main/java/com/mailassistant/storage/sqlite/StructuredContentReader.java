package com.mailassistant.storage.sqlite;

import com.fasterxml.jackson.databind.JsonNode;
import com.mailassistant.domain.model.*;
import com.mailassistant.util.JsonUtil;
import java.util.*;

public final class StructuredContentReader {
    public List<ContentBlock> read(String json, Map<String, ImageResource> assets) throws Exception {
        JsonNode root = JsonUtil.MAPPER.readTree(json);
        if (root.path("schemaVersion").asInt() != 1 || !root.path("blocks").isArray()) throw new IllegalArgumentException("不支持的题目正文格式");
        List<ContentBlock> result = new ArrayList<>();
        for (JsonNode node : root.path("blocks")) {
            String type = node.path("type").asText(); ContentBlock block;
            switch (type) {
                case "paragraph" -> { block = ContentBlock.text(type, ""); block.inlines = inlines(node.path("inlines"), assets);
                    if (block.inlines.stream().allMatch(i -> i.assetId().isBlank())) {
                        block.text = block.inlines.stream().map(ContentBlock.Inline::text).reduce("", String::concat); block.inlines.clear();
                    } }
                case "code" -> { block = ContentBlock.text(type, node.path("text").asText()); block.language = node.path("language").asText("text"); }
                case "image", "formula" -> {
                    if (type.equals("formula") && node.path("latex").asText().isBlank()) throw new IllegalArgumentException("公式缺少 LaTeX");
                    String id = node.path("assetId").asText(); requireAsset(id, assets);
                    block = ContentBlock.image(id, node.path("caption").asText(node.path("alt").asText()));
                    block.latex = node.path("latex").asText(); block.number = node.path("number").asText();
                }
                case "table" -> {
                    block = ContentBlock.text(type, ""); block.caption = node.path("caption").asText();
                    var headers = row(node.path("headers"), assets); block.rows.add(headers);
                    if (headers.isEmpty()) throw new IllegalArgumentException("表格缺少表头");
                    for (JsonNode r : node.path("rows")) {
                        var cells = row(r, assets);
                        if (cells.size() != headers.size()) throw new IllegalArgumentException("表格列数不一致");
                        block.rows.add(cells);
                    }
                }
                default -> throw new IllegalArgumentException("未知正文块：" + type);
            }
            result.add(block);
        }
        return result;
    }
    private List<List<ContentBlock.Inline>> row(JsonNode row, Map<String, ImageResource> assets) {
        List<List<ContentBlock.Inline>> cells = new ArrayList<>();
        for (JsonNode cell : row) cells.add(inlines(cell.path("inlines"), assets));
        return cells;
    }
    private List<ContentBlock.Inline> inlines(JsonNode nodes, Map<String, ImageResource> assets) {
        List<ContentBlock.Inline> result = new ArrayList<>();
        if (!nodes.isArray()) throw new IllegalArgumentException("缺少 inlines");
        for (JsonNode node : nodes) {
            switch (node.path("type").asText()) {
                case "text" -> result.add(ContentBlock.Inline.text(node.path("text").asText()));
                case "formula" -> {
                    String id = node.path("assetId").asText(); requireAsset(id, assets);
                    if (node.path("latex").asText().isBlank() || node.path("alt").asText().isBlank()) throw new IllegalArgumentException("公式内容不完整");
                    result.add(new ContentBlock.Inline(null, id, node.path("alt").asText(), node.path("latex").asText()));
                }
                default -> throw new IllegalArgumentException("未知行内内容类型");
            }
        }
        return result;
    }
    private void requireAsset(String id, Map<String, ImageResource> assets) {
        if (!assets.containsKey(id)) throw new IllegalArgumentException("题目缺少图片资源：" + id);
    }
}
