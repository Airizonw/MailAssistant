package com.mailassistant.validation;

import com.mailassistant.domain.model.*;
import com.mailassistant.preview.*;
import java.util.*;

public final class PreviewValidator {
    public List<String> validate(Submission submission) {
        List<String> errors = new ArrayList<>(); PreviewDocument doc = submission.preview;
        if (doc == null) return List.of("请先生成正文");
        if (submission.chapter != null) {
            String expected = "Java-" + submission.profile.studentId + "-" + submission.profile.name + "-" + submission.profile.college + "-第" + submission.chapter.number() + "章作业";
            if (!doc.subject.equals(expected)) errors.add("主题与当前身份或章节不符，请重新生成正文");
        }
        if (doc.exerciseCount != submission.items.size()) errors.add("预览题目数量与当前作业不符");
        if (doc.subject.isBlank() || doc.subject.contains("\r") || doc.subject.contains("\n")) errors.add("邮件主题无效");
        if (doc.blocks.isEmpty() || !doc.blocks.getFirst().role.equals("identity") || !doc.blocks.getFirst().text.equals(submission.profile.heading())) errors.add("正文首行必须保留完整个人信息");
        long owner = 0; String currentCase = ""; int phase = 0;
        for (var block : doc.blocks) {
            if (block.role.equals("exercise")) { owner = block.exerciseId; currentCase = ""; phase = 0; }
            else if (block.exerciseId != 0 && block.exerciseId != owner) errors.add("正文内容已移出所属题目，请在原始编辑页调整整题顺序");
            int nextPhase = switch (block.role) { case "question" -> 0; case "answer", "answer-heading", "source", "source-heading" -> 1; case "uml", "uml-heading" -> 2; case "result-heading" -> 3; default -> block.role.startsWith("case:") || block.role.startsWith("result:") ? 3 : phase; };
            if (nextPhase < phase) errors.add("请保持题目 → 解答/代码 → UML → 运行结果的顺序");
            phase = nextPhase;
            if (block.role.equals("result-heading")) currentCase = submission.items.stream().anyMatch(item -> item.exercise.id() == block.exerciseId && item.resultCases.size() == 1) ? "0" : "";
            if (block.role.startsWith("case:")) currentCase = block.role.substring(5);
            if (block.role.startsWith("result:") && !block.role.substring(7).equals(currentCase)) errors.add("截图已移出对应运行情况，请在原始编辑页调整情况顺序");
        }
        for (SubmissionItem item : submission.items) {
            var blocks = doc.blocks.stream().filter(b -> b.exerciseId == item.exercise.id()).toList();
            String prefix = "预览中第 " + item.exercise.number() + " 题：";
            if (blocks.stream().noneMatch(b -> b.role.equals("exercise") && !b.text.isBlank())) errors.add(prefix + "缺少题号标题");
            if (blocks.stream().noneMatch(b -> b.role.equals("question") && (!b.text.isBlank() || !b.inlines.isEmpty() || b.type.equals("image") || b.type.equals("table")))) errors.add(prefix + "缺少题目内容");
            if (item.exercise.programming() && blocks.stream().noneMatch(b -> b.role.equals("source") && !b.text.isBlank())) errors.add(prefix + "缺少代码");
            if (blocks.stream().filter(b -> b.role.equals("source") && !b.text.isBlank()).count() < item.sources.size()) errors.add(prefix + "请保留每份 Java 代码");
            if (blocks.stream().filter(b -> b.role.equals("source-heading") && !b.text.isBlank()).count() < item.sources.size()) errors.add(prefix + "请保留每份代码的名称");
            for (int i=0;i<item.resultCases.size();i++) {
                String role = "result:" + i;
                if (blocks.stream().noneMatch(b -> b.role.equals(role) && b.type.equals("image"))) errors.add(prefix + "运行情况 " + (i+1) + " 缺少截图");
            }
            if (item.exercise.requireUml() && blocks.stream().noneMatch(b -> b.role.equals("uml") && b.type.equals("image"))) errors.add(prefix + "缺少必需 UML 图");
        }
        for (String id : HtmlSubmissionRenderer.referencedAssets(doc)) {
            if (!doc.assets.containsKey(id) || !new ImageValidator().valid(doc.assets.get(id))) errors.add("预览中存在无效图片");
        }
        return errors.stream().distinct().toList();
    }
}
