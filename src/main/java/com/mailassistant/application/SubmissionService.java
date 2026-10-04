package com.mailassistant.application;

import com.mailassistant.domain.model.*;
import com.mailassistant.repository.*;
import com.mailassistant.preview.*;
import com.mailassistant.validation.*;
import java.nio.file.*;
import java.util.*;

public final class SubmissionService {
    private final DraftRepository drafts;
    public SubmissionService(DraftRepository drafts) { this.drafts = drafts; }
    public void save(Submission s) throws Exception { drafts.save(s); }
    public List<DraftRepository.DraftInfo> drafts() throws Exception { return drafts.list(); }
    public Submission load(String id) throws Exception { return drafts.load(id); }
    public void deleteDraft(String id) throws Exception { drafts.delete(id); }
    public PreviewDocument generate(Submission s) {
        requireValid(new SubmissionValidator().validate(s)); return new HtmlSubmissionRenderer().compose(s);
    }
    public void validateFinal(Submission s) {
        requireValid(new SubmissionValidator().validate(s)); requireValid(new PreviewValidator().validate(s));
    }
    public static void requireValid(List<String> errors) { if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("\n", errors)); }
    public JavaSource readJavaSource(Path path) throws Exception {
        return new JavaSource(path.getFileName().toString(), readSource(path));
    }
    public String readSource(Path path) throws Exception {
        if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".java")) throw new IllegalArgumentException("请选择 .java 源文件");
        if (Files.size(path)>2*1024*1024) throw new IllegalArgumentException("源文件不能超过 2 MB");
        return Files.readString(path, java.nio.charset.StandardCharsets.UTF_8).replace("\uFEFF", "");
    }
    public ImageResource readImage(Path path) throws Exception { return new ImageValidator().read(path); }
}
