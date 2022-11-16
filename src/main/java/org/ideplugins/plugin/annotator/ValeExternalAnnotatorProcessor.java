package org.ideplugins.plugin.annotator;

import com.google.gson.JsonArray;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.ideplugins.plugin.service.ValeIssuesReporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;


public class ValeExternalAnnotatorProcessor extends ExternalAnnotator<InitialAnnotatorInfo, AnnotatorResult> {

    private static final Logger LOG = Logger.getInstance(ValeExternalAnnotatorProcessor.class);

    @Override
    public @Nullable InitialAnnotatorInfo collectInformation(@NotNull PsiFile psiFile) {
        ValeIssuesReporter reporter = psiFile.getProject().getService(ValeIssuesReporter.class);
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(psiFile.getProject());
        Document document = documentManager.getDocument(psiFile);
        String filePath = psiFile.getVirtualFile().getPath();
        if (SystemInfo.isWindows && !filePath.contains(File.separator) ){
            filePath = filePath.replace('/', File.separatorChar);
        }
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
        annotationResult.getValeResults().forEach(jsonObject -> {
            JsonArray span = jsonObject.getAsJsonArray("Span");
            int line = jsonObject.get("Line").getAsInt();
            int initialColumn = span.get(0).getAsInt();
            int finalColumn = span.get(1).getAsInt();
            TextRange range = annotationResult.getRange(line, initialColumn, finalColumn);
            if (annotationResult.isValidRangeForAnnotation(range)) {
                String valeSeverity = jsonObject.get("Severity").getAsString();
                HighlightSeverity severity = getSeverity(valeSeverity);
                holder.newAnnotation(severity, jsonObject.get("Message").getAsString())
                        .tooltip(jsonObject.get("Message").getAsString()).range(range).create();
            }
        });

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
