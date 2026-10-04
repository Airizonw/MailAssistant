package com.mailassistant.storage.sqlite;

import com.mailassistant.domain.model.*;
import com.mailassistant.repository.ExerciseRepository;
import com.mailassistant.validation.ImageValidator;
import com.mailassistant.util.JsonUtil;
import java.util.*;
import java.security.MessageDigest;

public final class SQLiteExerciseRepository implements ExerciseRepository {
    private final DatabaseManager database;
    public SQLiteExerciseRepository(DatabaseManager database) { this.database = database; }
    public List<Chapter> chapters() throws Exception {
        List<Chapter> result = new ArrayList<>();
        try (var c = database.connect(); var st = c.createStatement(); var rs = st.executeQuery("SELECT * FROM chapter ORDER BY sort_order")) {
            while (rs.next()) result.add(new Chapter(rs.getLong("id"), rs.getInt("chapter_no"), rs.getString("title")));
        }
        return result;
    }
    public List<Exercise> findByChapter(long chapterId) throws Exception {
        List<Exercise> result = new ArrayList<>();
        try (var c = database.connect()) {
            Set<Long> requireUml = new HashSet<>();
            try (var st = c.createStatement(); var rs = st.executeQuery("SELECT value FROM metadata WHERE key='uml_required_exercise_ids'")) {
                if (rs.next()) for (var n : JsonUtil.MAPPER.readTree(rs.getString(1))) requireUml.add(n.asLong());
            }
            try (var query = c.prepareStatement("SELECT * FROM exercise WHERE chapter_id=? ORDER BY sort_order")) {
                query.setLong(1, chapterId);
                try (var rs = query.executeQuery()) {
                    while (rs.next()) {
                        long id = rs.getLong("id"); Map<String, ImageResource> assets = new LinkedHashMap<>();
                        try (var aq = c.prepareStatement("SELECT a.* FROM asset a JOIN exercise_asset ea ON a.id=ea.asset_id WHERE ea.exercise_id=?")) {
                            aq.setLong(1, id);
                            try (var ar = aq.executeQuery()) {
                                while (ar.next()) {
                                    byte[] data = ar.getBytes("data");
                                    if (!HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data)).equals(ar.getString("sha256"))) throw new IllegalArgumentException("题库图片校验失败");
                                    var image = new ImageValidator().decode(ar.getString("id"), "题目插图", data);
                                    if (image.width() != ar.getInt("width_px") || image.height() != ar.getInt("height_px") || data.length != ar.getInt("byte_size") || !image.mimeType().equals(ar.getString("mime_type"))) throw new IllegalArgumentException("题库图片元数据无效");
                                    assets.put(image.id(), image);
                                }
                            }
                        }
                        result.add(new Exercise(id, chapterId, rs.getString("exercise_no"), rs.getString("title"), rs.getString("exercise_type"), rs.getBoolean("require_result"), rs.getBoolean("allow_uml"), requireUml.contains(id), new StructuredContentReader().read(rs.getString("content"), assets), assets));
                    }
                }
            }
        }
        return result;
    }
}
