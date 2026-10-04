package com.mailassistant.domain.model;

import java.util.ArrayList;
import java.util.List;

public class UserProfile {
    public String college = "", className = "", studentId = "", name = "", teacherEmail = "";
    public List<String> assistantEmails = new ArrayList<>();
    public String heading() { return college + "-" + className + "班-" + studentId + "-" + name; }
}
