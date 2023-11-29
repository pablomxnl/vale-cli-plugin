package org.ideplugins.vale_cli_plugin.annotator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
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
        // https://youtrack.jetbrains.com/issue/IDEA-229905
        Application application = ApplicationManager.getApplication();
        if (application != null && application.isReadAccessAllowed() ) return null;
        return new AnnotatorResult(collectedInfo);
    }


    @Override
    public void apply(@NotNull PsiFile psiFile, AnnotatorResult annotationResult, @NotNull AnnotationHolder holder) {
        if ( annotationResult != null && annotationResult.getValeResults() !=null ) {
            annotationResult.getValeResults().forEach(jsonObject -> {
                JsonArray span = jsonObject.getAsJsonArray("Span");
                int line = jsonObject.get("Line").getAsInt();
                int initialColumn = span.get(0).getAsInt();
                int finalColumn = span.get(1).getAsInt();
                Optional.ofNullable(annotationResult.getRange(line, initialColumn, finalColumn)).ifPresent(range -> {
                    if (annotationResult.isValidRangeForAnnotation(range)) {
                        createAnnotation(holder, jsonObject, range);
                    }
                });
            });
        }
    }

    private void createAnnotation(AnnotationHolder holder, JsonObject jsonObject, TextRange range) {
        String valeSeverity = jsonObject.get("Severity").getAsString();
        HighlightSeverity severity = getSeverity(valeSeverity);
        JsonObject action = jsonObject.getAsJsonObject("Action");
        String actionName = action.get("Name").getAsString();
        JsonElement params = action.get("Params");
        AnnotationBuilder ab =
        holder.newAnnotation(severity, jsonObject.get("Message").getAsString())
                .tooltip(jsonObject.get("Message").getAsString()).range(range);
        if ("replace".equals(actionName) && params.isJsonArray()){
            String term = jsonObject.get("Match").getAsString();
            String replacement =  params.getAsJsonArray().get(0).getAsString();
            ab = ab.withFix(new ValeReplaceQuickFix(term, replacement, range));
        }
        ab.create();
    }

    private HighlightSeverity getSeverity(@NotNull String valeSeverity) {
        return switch (valeSeverity) {
            case "warning" -> HighlightSeverity.WARNING;
            case "error" -> HighlightSeverity.ERROR;
            default -> HighlightSeverity.INFORMATION;
        };
    }

}
