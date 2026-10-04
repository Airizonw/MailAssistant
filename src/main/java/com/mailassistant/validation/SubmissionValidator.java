package com.mailassistant.validation;

import com.mailassistant.domain.model.*;
import java.util.*;

public final class SubmissionValidator {
    private final ImageValidator images = new ImageValidator();
    public List<String> validate(Submission s) {
        List<String> errors = new ArrayList<>();
        UserProfile p = s.profile;
        if (p.name.isBlank()) errors.add("请填写姓名");
        if (p.studentId.isBlank()) errors.add("请填写学号");
        else if (!p.studentId.matches("[0-9]+")) errors.add("学号只能填写数字");
        if (p.college.isBlank()) errors.add("请填写学院");
        if (p.className.isBlank()) errors.add("请填写班级");
        else if (!p.className.matches("[0-9]+")) errors.add("班级 / 班号只能填写数字");
        for (String value : List.of(p.name, p.studentId, p.college, p.className))
            if (value.contains("\n") || value.contains("\r")) errors.add("个人信息不得含换行");
        if (s.chapter == null) errors.add("请选择章节");
        if (s.items.isEmpty()) errors.add("请至少选择一道习题");
        Set<Long> ids = new HashSet<>();
        long total = 0;
        for (SubmissionItem item : s.items) {
            Exercise ex = item.exercise; String prefix = "第 " + ex.number() + " 题：";
            if (!ids.add(ex.id())) errors.add(prefix + "重复选题");
            if (s.chapter != null && ex.chapterId() != s.chapter.id()) errors.add(prefix + "不属于当前章节");
            if (ex.programming() && item.sources.isEmpty()) errors.add(prefix + "缺少 Java 源代码");
            for (int i = 0; i < item.sources.size(); i++) {
                JavaSource source = item.sources.get(i);
                String sourcePrefix = prefix + "第 " + (i + 1) + " 份 Java 代码：";
                if (source.name.isBlank()) errors.add(sourcePrefix + "请填写代码名称");
                if (source.code.isBlank()) errors.add(sourcePrefix + "缺少源代码，请填写或删除此份代码");
            }
            if (!ex.programming() && item.answer.isBlank()) errors.add(prefix + "缺少解答");
            if ((ex.programming() || ex.requireResult()) && item.resultCases.isEmpty()) errors.add(prefix + "至少添加一种运行情况及截图");
            for (int i = 0; i < item.resultCases.size(); i++) {
                var result = item.resultCases.get(i);
                if (result.images.isEmpty()) errors.add(prefix + "情况 " + (i + 1) + " 缺少截图");
                for (var image : result.images) {
                    if (!images.valid(image)) errors.add(prefix + "情况 " + (i + 1) + " 的截图无效");
                    total += image.data().length;
                }
            }
            if (ex.requireUml() && item.umlImages.isEmpty()) errors.add(prefix + "题目要求 UML 图，请导入");
            for (var image : item.umlImages) {
                if (!images.valid(image)) errors.add(prefix + "UML 图片无效");
                total += image.data().length;
            }
        }
        if (total > 20 * 1024 * 1024) errors.add("图片总量超过 20 MB，请压缩后再生成正文");
        return errors;
    }
}
