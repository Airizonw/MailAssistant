package com.mailassistant.storage.encrypted;

import com.mailassistant.domain.model.Submission;
import com.mailassistant.repository.DraftRepository;
import com.mailassistant.security.CryptoService;
import com.mailassistant.util.*;
import java.nio.file.*;
import java.util.*;

/** Snapshots include image bytes inside the encrypted payload: no plaintext screenshot copies. */
public final class EncryptedDraftRepository implements DraftRepository {
    private final Path directory;
    private final CryptoService crypto;
    public EncryptedDraftRepository(Path directory, CryptoService crypto) { this.directory = directory; this.crypto = crypto; }
    private Path path(String id) { UUID.fromString(id); return directory.resolve(id).resolve("draft.enc"); }
    public Submission load(String id) throws Exception { return JsonUtil.MAPPER.readValue(crypto.decrypt(Files.readAllBytes(path(id))), Submission.class); }
    public void save(Submission submission) throws Exception {
        submission.modifiedAt = java.time.Instant.now().toString();
        if (submission.draftName == null || submission.draftName.isBlank())
            submission.draftName = "草稿 " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        FileUtil.atomicWrite(path(submission.id), crypto.encrypt(JsonUtil.MAPPER.writeValueAsBytes(submission)));
        List<DraftInfo> drafts = list();
        for (int i = 30; i < drafts.size(); i++) delete(drafts.get(i).id());
    }
    public void delete(String id) throws Exception {
        Path file = path(id);
        Files.deleteIfExists(file);
        // Only remove the empty draft folder; never recursively delete other files.
        try { Files.deleteIfExists(file.getParent()); }
        catch (DirectoryNotEmptyException ignored) { }
    }
    public List<DraftInfo> list() throws Exception {
        List<DraftInfo> result = new ArrayList<>();
        if (!Files.exists(directory)) return result;
        try (var paths = Files.list(directory)) {
            for (Path p : paths.filter(Files::isDirectory).toList()) {
                if (!Files.exists(p.resolve("draft.enc"))) continue;
                try {
                    Submission s = load(p.getFileName().toString());
                    String title = s.draftName == null || s.draftName.isBlank()
                            ? (s.chapter == null ? "未选章节" : s.chapter.title()) + " · " + s.items.size() + " 道题" : s.draftName;
                    result.add(new DraftInfo(s.id, title, s.modifiedAt));
                } catch (Exception e) { result.add(new DraftInfo(p.getFileName().toString(), "无法读取的草稿（可选择查看错误）", "")); }
            }
        }
        result.sort(Comparator.comparing((DraftInfo info) -> {
            try { return java.time.Instant.parse(info.modifiedAt()); }
            catch (Exception e) { return java.time.Instant.MIN; }
        }).reversed()); return result;
    }
}
