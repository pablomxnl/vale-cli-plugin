package org.ideplugins.plugin.annotator;

import com.google.gson.JsonObject;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.DocumentUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AnnotatorResult {

    private final InitialAnnotatorInfo info;

    public AnnotatorResult(InitialAnnotatorInfo info) {
        this.info = info;
    }

    public List<JsonObject> getValeResults() {
        return info.results;
    }

    @Nullable
    public TextRange getRange(int line, int begin, int end) {
        if (line < info.document.getLineCount()-1 ){
            int startOffset = info.document.getLineStartOffset(line - 1);
            return TextRange.from(startOffset + begin - 1, (end - begin) + 1);
        }
        return null;
    }

    public boolean isValidRangeForAnnotation(TextRange range) {
        return DocumentUtil.isValidOffset(range.getStartOffset(), info.document) &&
                DocumentUtil.isValidOffset(range.getEndOffset(), info.document);
    }


}
