package com.mailassistant.mail;

import com.mailassistant.domain.model.MailAccount;
import com.mailassistant.preview.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.activation.DataHandler;
import java.util.*;

public final class JakartaMailClient implements MailClient {
    public Properties properties(MailAccount account) {
        Properties p = new Properties();
        p.setProperty("mail.smtp.auth", "true");
        p.setProperty("mail.smtp.ssl.enable", Boolean.toString(account.tls.equals("SSL")));
        p.setProperty("mail.smtp.starttls.enable", Boolean.toString(account.tls.equals("STARTTLS")));
        p.setProperty("mail.smtp.starttls.required", Boolean.toString(account.tls.equals("STARTTLS")));
        p.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        p.setProperty("mail.smtp.connectiontimeout", "15000"); p.setProperty("mail.smtp.timeout", "30000"); p.setProperty("mail.smtp.writetimeout", "30000");
        if (account.auth.equals("OAuth2 Token")) p.setProperty("mail.smtp.auth.mechanisms", "XOAUTH2");
        return p;
    }
    /** Pure MIME construction: no socket opened here. */
    public MimeMessage compose(Session session, MailAccount account, PreviewDocument document) throws Exception {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(account.address)); message.setRecipient(Message.RecipientType.TO, new InternetAddress(document.to));
        for (String cc : document.cc) message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
        message.setSubject(document.subject, "UTF-8"); message.setSentDate(new Date());
        MimeMultipart related = new MimeMultipart("related"); MimeBodyPart html = new MimeBodyPart();
        html.setContent(new HtmlSubmissionRenderer().html(document, false), "text/html; charset=UTF-8"); related.addBodyPart(html);
        for (String id : HtmlSubmissionRenderer.referencedAssets(document)) {
            var image = document.assets.get(id); MimeBodyPart part = new MimeBodyPart();
            part.setDataHandler(new DataHandler(new ByteArrayDataSource(image.data(), image.mimeType())));
            part.setHeader("Content-ID", "<" + id + ">"); part.setDisposition(Part.INLINE); related.addBodyPart(part);
        }
        message.setContent(related); message.saveChanges(); return message;
    }
    public void send(MailAccount account, String secret, PreviewDocument document) throws Exception {
        Session session = Session.getInstance(properties(account)); session.setDebug(false);
        MimeMessage message = compose(session, account, document);
        try (Transport transport = session.getTransport("smtp")) {
            transport.connect(account.host, account.port, account.address, secret);
            transport.sendMessage(message, message.getAllRecipients());
        }
    }
}
