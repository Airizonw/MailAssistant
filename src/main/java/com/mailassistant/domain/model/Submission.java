package com.mailassistant.domain.model;

import com.mailassistant.preview.PreviewDocument;
import java.util.*;

public class Submission {
    public String id = UUID.randomUUID().toString();
    public String draftName = "";
    public String createdAt = java.time.Instant.now().toString(), modifiedAt = createdAt;
    public Chapter chapter;
    public UserProfile profile = new UserProfile();
    public MailAccount account = new MailAccount();
    public List<SubmissionItem> items = new ArrayList<>();
    public PreviewDocument preview;
}
