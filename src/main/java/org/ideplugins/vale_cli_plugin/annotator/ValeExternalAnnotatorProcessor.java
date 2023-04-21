package org.ideplugins.vale_cli_plugin.annotator;

import com.google.gson.JsonArray;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.ideplugins.vale_cli_plugin.service.ValeIssuesReporter;
import org.ideplugins.vale_cli_plugin.settings.OSUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;


public class ValeExternalAnnotatorProcessor extends ExternalAnnotator<InitialAnnotatorInfo, AnnotatorResult> {


    @Override
    public @Nullable InitialAnnotatorInfo collectInformation(@NotNull PsiFile psiFile) {
        ValeIssuesReporter reporter = psiFile.getProject().getService(ValeIssuesReporter.class);
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(psiFile.getProject());
        Document document = documentManager.getDocument(psiFile);
        String filePath = OSUtils.normalizeFilePath(psiFile.getVirtualFile().getPath());
        if (!reporter.hasIssuesForFile(filePath))
            return null;
        return new InitialAnnotatorInfo(document, psiFile, reporter.getIssues(filePath));
    }

    @Override
    public @Nullable AnnotatorResult doAnnotate(InitialAnnotatorInfo collectedInfo) {
        return new AnnotatorResult(collectedInfo);
    }


    @Override
    public void apply(@NotNull PsiFile psiFile, AnnotatorResult annotationResult, @NotNull AnnotationHolder holder) {
        if (annotationResult.getValeResults() !=null ) {
            annotationResult.getValeResults().forEach(jsonObject -> {
                JsonArray span = jsonObject.getAsJsonArray("Span");
                int line = jsonObject.get("Line").getAsInt();
                int initialColumn = span.get(0).getAsInt();
                int finalColumn = span.get(1).getAsInt();
                Optional.ofNullable(annotationResult.getRange(line, initialColumn, finalColumn)).ifPresent(range -> {
                    if (annotationResult.isValidRangeForAnnotation(range)) {
                        String valeSeverity = jsonObject.get("Severity").getAsString();
                        HighlightSeverity severity = getSeverity(valeSeverity);
                        holder.newAnnotation(severity, jsonObject.get("Message").getAsString())
                                .tooltip(jsonObject.get("Message").getAsString()).range(range).create();
                    }
                });
            });
        }
    }

    private HighlightSeverity getSeverity(@NotNull String valeSeverity) {
        HighlightSeverity severity;
        switch (valeSeverity) {
            case "warning":
                severity = HighlightSeverity.WARNING;
                break;
            case "error":
                severity = HighlightSeverity.ERROR;
                break;
            case "suggestion":
            default:
                severity = HighlightSeverity.WEAK_WARNING;
        }
        return severity;
    }

}
