package com.mailassistant.domain.model;

import java.util.ArrayList;
import java.util.List;

public class SubmissionItem {
    public Exercise exercise;
    public String answer = "";
    public List<JavaSource> sources = new ArrayList<>();
    public List<RunResultCase> resultCases = new ArrayList<>();
    public List<ImageResource> umlImages = new ArrayList<>();
    public SubmissionItem() {}
    public SubmissionItem(Exercise exercise) { this.exercise = exercise; }
    /** Read old single-source drafts without inventing a name for pasted code. */
    @com.fasterxml.jackson.annotation.JsonSetter("sourceCode")
    public void readLegacySource(String code) {
        if (code != null && !code.isBlank()) sources.add(new JavaSource("", code));
    }
    @Override public String toString() { return exercise.toString(); }
}
