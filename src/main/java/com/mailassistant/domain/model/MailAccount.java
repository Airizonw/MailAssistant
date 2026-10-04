package com.mailassistant.domain.model;

/** Authentication secrets are deliberately excluded from this serializable model. */
public class MailAccount {
    public String provider = "QQ", address = "", host = "smtp.qq.com";
    public int port = 465;
    public String tls = "SSL", auth = "授权码";
}
