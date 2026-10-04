package com.mailassistant.validation;

import com.mailassistant.domain.model.ImageResource;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class ImageValidator {
    public static final int MAX_BYTES = 10 * 1024 * 1024;
    public ImageResource read(Path path) throws IOException {
        if (Files.size(path) > MAX_BYTES) throw new IOException("单张图片不能超过 10 MB");
        return decode(UUID.randomUUID().toString(), path.getFileName().toString(), Files.readAllBytes(path));
    }
    public ImageResource decode(String id, String name, byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_BYTES) throw new IOException("图片为空或超过 10 MB");
        try (var input = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException("无法识别图片，请使用 PNG / JPEG");
            var reader = readers.next();
            try {
                reader.setInput(input);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!Set.of("png", "jpeg", "jpg").contains(format)) throw new IOException("只支持 PNG / JPEG 图片");
                int w = reader.getWidth(0), h = reader.getHeight(0);
                if (w < 1 || h < 1 || (long) w * h > 25_000_000) throw new IOException("图片像素总数不得超过 2500 万");
                if (reader.read(0) == null) throw new IOException("图片数据损坏");
                return new ImageResource(id, name, format.equals("png") ? "image/png" : "image/jpeg", bytes, w, h);
            } finally { reader.dispose(); }
        }
    }
    public boolean valid(ImageResource image) {
        try { decode(image.id(), image.name(), image.data()); return true; } catch (Exception e) { return false; }
    }
}
