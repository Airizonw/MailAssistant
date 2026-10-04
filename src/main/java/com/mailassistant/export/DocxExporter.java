package com.mailassistant.export;

import com.mailassistant.preview.PreviewDocument;
import java.nio.file.Path;

public interface DocxExporter { void export(PreviewDocument document, Path output) throws Exception; }
