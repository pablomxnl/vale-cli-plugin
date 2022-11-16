package org.ideplugins.plugin.annotator;

import com.google.gson.JsonObject;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.DocumentUtil;

import java.util.List;

public class AnnotatorResult {

    private final InitialAnnotatorInfo info;

    public AnnotatorResult(InitialAnnotatorInfo info) {
        this.info = info;
    }

    public List<JsonObject> getValeResults() {
        return info.results;
    }

    public TextRange getRange(int line, int begin, int end) {
        int startOffset = info.document.getLineStartOffset(line - 1);
        return TextRange.from(startOffset + begin - 1, (end - begin) + 1);
    }

    public boolean isValidRangeForAnnotation(TextRange range) {
        return DocumentUtil.isValidOffset(range.getStartOffset(), info.document) &&
                DocumentUtil.isValidOffset(range.getEndOffset(), info.document);
    }


}
