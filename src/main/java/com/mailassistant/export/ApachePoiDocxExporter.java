package com.mailassistant.export;

import com.mailassistant.domain.model.*;
import com.mailassistant.preview.PreviewDocument;
import com.mailassistant.util.FileUtil;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;
import java.io.*;
import java.nio.file.Path;
import java.util.List;

public final class ApachePoiDocxExporter implements DocxExporter {
    public void export(PreviewDocument document, Path output) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            for (ContentBlock block : document.blocks) {
                if (block.type.equals("table")) {
                    if (!block.caption.isBlank()) text(doc.createParagraph(), block.caption, false, 11);
                    if (block.rows.isEmpty()) continue;
                    XWPFTable table = doc.createTable(block.rows.size(), block.rows.getFirst().size()); table.setWidth("100%");
                    for (int r=0;r<block.rows.size();r++) for (int c=0;c<block.rows.get(r).size();c++) {
                        var cell = table.getRow(r).getCell(c); if (r==0) cell.setColor("EDF2F8");
                        inlines(cell.getParagraphs().getFirst(), block.rows.get(r).get(c), document);
                    }
                    continue;
                }
                XWPFParagraph p = doc.createParagraph(); p.setSpacingAfter(160);
                switch (block.type) {
                    case "image" -> { picture(p.createRun(), document.assets.get(block.assetId), false); if (!block.caption.isBlank()) { p.createRun().addBreak(); text(p, block.caption, false, 10); } }
                    case "heading" -> { p.setKeepNext(true); text(p, block.text, true, 16); }
                    case "subheading" -> { p.setKeepNext(true); text(p, block.text, true, 12); }
                    case "code" -> { p.setIndentationLeft(180); XWPFRun run = p.createRun(); run.setFontFamily("Consolas"); run.setFontSize(10); multiline(run, block.text); }
                    default -> { if (block.inlines.isEmpty()) text(p, block.text, block.role.equals("identity"), 11); else inlines(p, block.inlines, document); }
                }
            }
            doc.getProperties().getCoreProperties().setTitle(document.subject);
            doc.write(bytes); FileUtil.atomicWrite(output.toAbsolutePath(), bytes.toByteArray());
        }
    }
    private void inlines(XWPFParagraph p, List<ContentBlock.Inline> inlines, PreviewDocument doc) throws Exception {
        for (var i : inlines) {
            if (i.assetId()!=null && !i.assetId().isBlank()) picture(p.createRun(), doc.assets.get(i.assetId()), true);
            else text(p, i.text(), false, 11);
        }
    }
    private void picture(XWPFRun run, ImageResource image, boolean inline) throws Exception {
        if (image == null) throw new IllegalArgumentException("导出图片缺失");
        double scale = Math.min(1, Math.min((inline ? 135.0 : 450.0) / image.width(), (inline ? 24.0 : 600.0) / image.height()));
        try (var input = new ByteArrayInputStream(image.data())) {
            run.addPicture(input, image.mimeType().equals("image/png") ? Document.PICTURE_TYPE_PNG : Document.PICTURE_TYPE_JPEG, image.name(), Units.toEMU(image.width()*scale), Units.toEMU(image.height()*scale));
        }
    }
    private void text(XWPFParagraph p, String text, boolean bold, int size) {
        var run = p.createRun(); run.setFontFamily("Microsoft YaHei"); run.setFontSize(size); run.setBold(bold); multiline(run, text);
    }
    private void multiline(XWPFRun run, String text) {
        String[] lines = (text == null ? "" : text).split("\\R", -1);
        for (int i=0;i<lines.length;i++) { if (i>0) run.addBreak(); run.setText(lines[i]); }
    }
}
