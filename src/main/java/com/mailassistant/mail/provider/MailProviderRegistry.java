package com.mailassistant.mail.provider;

import com.mailassistant.domain.model.MailAccount;
import java.util.List;

public final class MailProviderRegistry {
    public static final List<String> PROVIDERS = List.of("QQ", "163");
    public static String suffix(String provider) { return "163".equals(provider) ? "@163.com" : "@qq.com"; }
    public static String localPart(String address) { return address.split("@", 2)[0]; }
    public static String address(String provider, String localPart) {
        String value = localPart.strip();
        return value.isEmpty() ? "" : value + suffix(provider);
    }
    public static void apply(String provider, MailAccount account) {
        account.provider = provider; account.port = 465; account.tls = "SSL";
        account.host = switch (provider) { case "QQ" -> "smtp.qq.com"; case "163" -> "smtp.163.com"; default -> ""; };
    }
}
