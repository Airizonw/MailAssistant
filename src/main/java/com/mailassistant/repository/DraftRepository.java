package com.mailassistant.repository;

import com.mailassistant.domain.model.Submission;
import java.util.List;

public interface DraftRepository {
    record DraftInfo(String id, String title, String modifiedAt) { @Override public String toString() { return title + "  ·  " + modifiedAt; } }
    List<DraftInfo> list() throws Exception;
    Submission load(String id) throws Exception;
    void save(Submission submission) throws Exception;
    void delete(String id) throws Exception;
}
