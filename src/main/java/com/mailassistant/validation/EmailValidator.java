package com.mailassistant.validation;

import jakarta.mail.internet.InternetAddress;
import java.util.*;

public final class EmailValidator {
    private EmailValidator() {}
    public static boolean valid(String value) {
        if (value == null || value.isBlank() || value.contains("\r") || value.contains("\n")) return false;
        try {
            InternetAddress address = new InternetAddress(value, true); address.validate();
            return value.equals(address.getAddress()) && value.matches("[^\\s<>@,;]+@[^\\s<>@,;]+\\.[^\\s<>@,;]+");
        } catch (Exception e) { return false; }
    }
    public static List<String> recipients(String to, List<String> cc) {
        List<String> errors = new ArrayList<>();
        if (cc.size() > 3) errors.add("助教邮箱最多填写三个");
        if (!valid(to)) errors.add("教师邮箱格式不正确");
        Set<String> seen = new HashSet<>(); seen.add(to.toLowerCase(Locale.ROOT));
        for (String address : cc) {
            if (!valid(address)) errors.add("抄送邮箱格式不正确：" + address);
            if (!seen.add(address.toLowerCase(Locale.ROOT))) errors.add("To / CC 中有重复邮箱：" + address);
        }
        return errors;
    }
}
