package org.ideplugins.vale_cli_plugin.annotator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.DocumentUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record ValeProblem(
    String description,
    String link,
    String message,
    String match,
    String check,
    String severity,
    Integer line,
    List<Integer> span,
    ValeAction action

) {
        public HighlightSeverity getHighlightSeverity(){
                return switch (severity) {
                        case "warning","suggestion" -> HighlightSeverity.WARNING;
                        case "error" -> HighlightSeverity.ERROR;
                        default -> HighlightSeverity.WEAK_WARNING;
                };
        }

        public ProblemHighlightType getProblemHighlightType(){
                return switch (severity) {
                        case "warning" -> ProblemHighlightType.WARNING;
                        case "error" -> ProblemHighlightType.ERROR;
                        case "suggestion" -> ProblemHighlightType.INFORMATION;
                        default -> ProblemHighlightType.WEAK_WARNING;
                };
        }

        @Nullable
        public TextRange getRange(final Document document){
                if (line() <= document.getLineCount()){
                        int startOffset = document.getLineStartOffset(line() - 1 );
                        return TextRange.from(startOffset + span().getFirst() - 1, ( span().getLast() - span().getFirst())+1 );
                }
                return null;
        }

        public boolean isValidRangeForAnnotation(final TextRange range, final Document document) {
                return range != null && DocumentUtil.isValidOffset(range.getStartOffset(), document) &&
                        DocumentUtil.isValidOffset(range.getEndOffset(), document);
        }


}

record ValeAction(
        String name,
        @JsonProperty(value = "Params")
        Optional<List<String>> parameters
){

}