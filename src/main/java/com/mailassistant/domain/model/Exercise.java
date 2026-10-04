package com.mailassistant.domain.model;

import java.util.List;
import java.util.Map;

public record Exercise(long id, long chapterId, String number, String title, String type,
                       boolean requireResult, boolean allowUml, boolean requireUml,
                       List<ContentBlock> content, Map<String, ImageResource> assets) {
    public boolean programming() { return "PROGRAMMING".equals(type); }
    @Override public String toString() { return "第 " + number + " 题  ·  " + title; }
}
