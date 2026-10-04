package com.mailassistant.preview;

import com.mailassistant.domain.model.*;
import com.mailassistant.util.JsonUtil;
import java.util.*;

public final class HtmlSubmissionRenderer {
    public PreviewDocument compose(Submission s) {
        PreviewDocument doc = new PreviewDocument();
        doc.subject = "Java-" + s.profile.studentId + "-" + s.profile.name + "-" + s.profile.college + "-第" + s.chapter.number() + "章作业";
        doc.to = s.profile.teacherEmail; doc.cc.addAll(s.profile.assistantEmails); doc.exerciseCount = s.items.size();
        ContentBlock header = ContentBlock.text("paragraph", s.profile.heading()); header.role = "identity"; doc.blocks.add(header);
        for (SubmissionItem item : s.items) {
            int start = doc.blocks.size();
            ContentBlock heading = ContentBlock.text("heading", "第 " + item.exercise.number() + " 题 · " + item.exercise.title()); heading.role = "exercise"; doc.blocks.add(heading);
            for (var block : item.exercise.content()) { var copy = JsonUtil.copy(block, ContentBlock.class); copy.role = "question"; doc.blocks.add(copy); }
            doc.assets.putAll(item.exercise.assets());
            if (!item.answer.isBlank()) {
                var title = ContentBlock.text("subheading", "解答"); title.role = "answer-heading"; doc.blocks.add(title);
                var answer = ContentBlock.text("paragraph", item.answer); answer.role = "answer"; doc.blocks.add(answer);
            }
            for (JavaSource source : item.sources) {
                ContentBlock title = ContentBlock.text("subheading", source.fileName()); title.role = "source-heading"; doc.blocks.add(title);
                ContentBlock code = ContentBlock.text("code", source.code); code.role = "source"; doc.blocks.add(code);
            }
            if (!item.umlImages.isEmpty()) { var umlHeading = ContentBlock.text("subheading", "UML 图"); umlHeading.role = "uml-heading"; doc.blocks.add(umlHeading); }
            for (var image : item.umlImages) addImage(doc, image, "uml", "");
            if (!item.resultCases.isEmpty()) { var resultHeading = ContentBlock.text("subheading", "运行结果"); resultHeading.role = "result-heading"; doc.blocks.add(resultHeading); }
            for (int i = 0; i < item.resultCases.size(); i++) {
                RunResultCase c = item.resultCases.get(i);
                if (item.resultCases.size() > 1 || !c.description.isBlank()) {
                    var caseHeading = ContentBlock.text("subheading", item.resultCases.size() > 1 ? "运行情况 " + (i + 1) + (c.description.isBlank() ? "" : "：" + c.description) : c.description); caseHeading.role = "case:" + i; doc.blocks.add(caseHeading);
                }
                for (var image : c.images) addImage(doc, image, "result:" + i, "");
            }
            for (int i = start; i < doc.blocks.size(); i++) doc.blocks.get(i).exerciseId = item.exercise.id();
        }
        return doc;
    }
    private void addImage(PreviewDocument doc, ImageResource image, String role, String caption) {
        doc.assets.put(image.id(), image); ContentBlock block = ContentBlock.image(image.id(), caption); block.role = role; doc.blocks.add(block);
    }
    public String html(PreviewDocument doc, boolean preview) {
        StringBuilder out = new StringBuilder("<!doctype html><html><head><meta charset='UTF-8'></head><body style='margin:24px;color:#233047;font-family:Arial,Microsoft YaHei,微软雅黑,sans-serif;font-size:15px;line-height:1.75;background:white'>");
        for (int index = 0; index < doc.blocks.size(); index++) {
            var b = doc.blocks.get(index);
            if (preview) out.append("<div id='preview-block-").append(index).append("'>");
            switch (b.type) {
                case "heading" -> out.append("<h2 style='margin-top:30px;border-bottom:1px solid #dce3ed;padding-bottom:10px'>").append(escape(b.text)).append("</h2>");
                case "subheading" -> out.append("<h3 style='font-size:17px;font-weight:bold'>").append(escape(b.text)).append("</h3>");
                case "code" -> out.append("<pre style='padding:18px;background:#f2f5f9;border:1px solid #dde5ef;white-space:pre-wrap;font:13px Consolas,monospace'>").append(escape(b.text)).append("</pre>");
                case "image" -> {
                    out.append("<p>").append(image(doc, b.assetId, b.caption, preview, false));
                    if (!b.caption.isBlank()) out.append("<br><span style='color:#63718a;font-size:12px'>").append(escape(b.caption)).append("</span>");
                    out.append("</p>");
                }
                case "table" -> {
                    out.append("<p>").append(escape(b.caption)).append("</p><table style='border-collapse:collapse'>");
                    for (int r=0; r<b.rows.size(); r++) {
                        out.append("<tr>");
                        for (var cell : b.rows.get(r)) out.append("<td style='border:1px solid #c9d3e0;padding:8px;" + (r==0?"background:#eef3f9;font-weight:bold":"") + "'>").append(inlines(doc, cell, preview)).append("</td>");
                        out.append("</tr>");
                    }
                    out.append("</table>");
                }
                default -> out.append("<p style='white-space:pre-wrap;").append(b.role.equals("identity") ? "font-weight:bold" : "").append("'>").append(b.inlines.isEmpty() ? escape(b.text) : inlines(doc, b.inlines, preview)).append("</p>");
            }
            if (preview) out.append("</div>");
        }
        return out.append("</body></html>").toString();
    }
    private String inlines(PreviewDocument doc, List<ContentBlock.Inline> items, boolean preview) {
        StringBuilder out = new StringBuilder();
        for (var item : items) out.append(item.assetId() == null || item.assetId().isBlank() ? escape(item.text()) : image(doc, item.assetId(), item.alt(), preview, true));
        return out.toString();
    }
    private String image(PreviewDocument doc, String id, String alt, boolean preview, boolean inline) {
        ImageResource image = doc.assets.get(id);
        if (image == null) throw new IllegalArgumentException("正文图片丢失");
        String src = preview ? "data:" + image.mimeType() + ";base64," + Base64.getEncoder().encodeToString(image.data()) : "cid:" + id;
        int width = inline ? Math.min(image.width(), 180) : Math.min(image.width(), 680);
        return "<img src='" + escape(src) + "' alt='" + escape(alt) + "' width='" + width + "' style='max-width:100%;height:auto;vertical-align:middle" + (inline ? ";max-height:32px" : "") + "'>";
    }
    public static String escape(String text) { return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); }
    public static Set<String> referencedAssets(PreviewDocument doc) {
        Set<String> ids = new LinkedHashSet<>();
        for (var b : doc.blocks) {
            if (b.type.equals("image")) ids.add(b.assetId);
            for (var i : b.inlines) if (i.assetId()!=null && !i.assetId().isBlank()) ids.add(i.assetId());
            for (var r : b.rows) for (var c : r) for (var i : c) if (i.assetId()!=null && !i.assetId().isBlank()) ids.add(i.assetId());
        }
        return ids;
    }
}
