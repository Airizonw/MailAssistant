package com.mailassistant.application;

import com.mailassistant.domain.model.*;
import com.mailassistant.mail.MailClient;
import com.mailassistant.security.CredentialService;
import com.mailassistant.util.JsonUtil;
import com.mailassistant.validation.EmailValidator;

public final class MailService {
    private final MailClient client;
    private final CredentialService credentials;
    private final SubmissionService submissions;
    /** Immutable-by-copy snapshot created BEFORE showing the confirmation dialog. */
    public static final class PreparedMail {
        private final Submission snapshot;
        private PreparedMail(Submission snapshot) { this.snapshot = snapshot; }
        public String summary() {
            var s = snapshot; return "发件人：" + s.account.address + "\n收件人：" + s.preview.to + "\n抄送：" + String.join("；", s.preview.cc) + "\n主题：" + s.preview.subject + "\n题目数量：" + s.preview.exerciseCount;
        }
    }
    public MailService(MailClient client, CredentialService credentials, SubmissionService submissions) { this.client=client; this.credentials=credentials; this.submissions=submissions; }
    public PreparedMail prepare(Submission submission) throws Exception {
        submissions.validateFinal(submission); var a = submission.account;
        SubmissionService.requireValid(EmailValidator.recipients(submission.preview.to, submission.preview.cc));
        if (submission.preview.cc.isEmpty()) throw new IllegalArgumentException("请填写至少一个助教邮箱");
        if (!EmailValidator.valid(a.address)) throw new IllegalArgumentException("请填写有效的发件邮箱");
        if (a.host.isBlank() || !a.host.matches("[A-Za-z0-9.-]+") || a.port<1 || a.port>65535) throw new IllegalArgumentException("请配置有效的 SMTP 主机和端口");
        if (!java.util.Set.of("SSL", "STARTTLS").contains(a.tls)) throw new IllegalArgumentException("SMTP 必须使用 SSL 或 STARTTLS");
        if (credentials.load(a.address).isBlank()) throw new IllegalArgumentException("请先在个人中心中保存授权码或 Token");
        return new PreparedMail(JsonUtil.copy(submission, Submission.class));
    }
    /** Called only by the UI's affirmative second-confirmation handler; never on preview/export. */
    public void sendConfirmed(PreparedMail prepared) throws Exception {
        var s = prepared.snapshot; String secret = credentials.load(s.account.address);
        if (secret.isBlank()) throw new IllegalArgumentException("邮箱凭据已被移除，请重新设置");
        try { client.send(s.account, secret, s.preview); }
        catch (Exception e) { throw new IllegalStateException("未能确认发送成功。请检查网络、SMTP 配置与授权码，并先查看已发送邮件后再决定是否重试。"); }
    }
}
