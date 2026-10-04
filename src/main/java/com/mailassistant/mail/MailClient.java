package com.mailassistant.mail;

import com.mailassistant.domain.model.MailAccount;
import com.mailassistant.preview.PreviewDocument;

public interface MailClient { void send(MailAccount account, String secret, PreviewDocument document) throws Exception; }
