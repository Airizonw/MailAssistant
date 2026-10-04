package com.mailassistant.preview;

import com.mailassistant.domain.model.*;
import java.util.*;

public class PreviewDocument {
    public String subject = "", to = "";
    public List<String> cc = new ArrayList<>();
    public List<ContentBlock> blocks = new ArrayList<>();
    public Map<String, ImageResource> assets = new LinkedHashMap<>();
    public int exerciseCount;
}
