package org.ideplugins.vale_cli_plugin.annotator;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.execution.ui.ConsoleViewContentType.LOG_ERROR_OUTPUT;
import static org.ideplugins.vale_cli_plugin.utils.ConsoleHelper.clearConsole;
import static org.ideplugins.vale_cli_plugin.utils.ConsoleHelper.writeTextToConsole;

public class ValeExternalAnnotatorProcessor extends ExternalAnnotator<ValeExternalAnnotatorProcessor.InitialInfo,
ValeExternalAnnotatorProcessor.AnalysisResult> implements DumbAware {

    private static final Logger LOGGER = Logger.getInstance(ValeExternalAnnotatorProcessor.class);

    public record InitialInfo(PsiFile file, Document document) {

    }
    public record AnalysisResult(List<ValeProblem> alerts, Document document) {

    }

    @Override
    public @Nullable ValeExternalAnnotatorProcessor.InitialInfo collectInformation(@NotNull PsiFile file) {
        Project project = file.getProject();
        ValeCliExecutor cliExecutor = ValeCliExecutor.getInstance(project);
        VirtualFile virtualFile = file.getViewProvider().getVirtualFile();
        if (cliExecutor.extensionsAsList().contains(virtualFile.getExtension())) {
            return new InitialInfo(file, file.getViewProvider().getDocument());
        }
        return null;
    }

    @Override
    public @Nullable ValeExternalAnnotatorProcessor.AnalysisResult doAnnotate(InitialInfo collectedInfo) {
        if (collectedInfo == null)
            return null;
        Project project = collectedInfo.file.getProject();
        VirtualFile virtualFile = collectedInfo.file.getViewProvider().getVirtualFile();
        ValeCliExecutor cliExecutor = ValeCliExecutor.getInstance(project);
        String errors = cliExecutor.checkConfiguration();
        if (!errors.isEmpty()) {
            ApplicationManager.getApplication().invokeLater(() -> writeTextToConsole(project, errors, LOG_ERROR_OUTPUT));
            return null;
        }
        if (!cliExecutor.extensionsAsList().contains(virtualFile.getExtension())) {
            return null;
        }

        return new AnalysisResult(getValeProblems(collectedInfo, virtualFile, cliExecutor, project), collectedInfo.document);
    }

    @Override
    public void apply(@NotNull PsiFile file, AnalysisResult annotationResult, @NotNull AnnotationHolder holder) {
        if (annotationResult == null) {
            return;
        }
        if (!annotationResult.alerts.isEmpty()) {
            for (ValeProblem problem : annotationResult.alerts()) {
                TextRange textRange = problem.getRange(annotationResult.document());
                if (textRange != null) {
                    createAnnotation(holder, problem, textRange, file);
                }
            }
            ApplicationManager.getApplication().invokeLater(() -> clearConsole(file.getProject()));
        }
    }

    private static List<ValeProblem> parseProcessOutput(ProcessOutput processOutput, Project project, ValeCliExecutor cliExecutor)
            throws ExecutionException {
        List<ValeProblem> result = new ArrayList<>();
        if (processOutput.getExitCode() == 0) {
            result = cliExecutor.parseSuccessProcessOutput(processOutput);
        } else {
            showErrors(cliExecutor.parseErrorProcessOutput(processOutput), project);
        }

        return result;
    }

    private static void showErrors(ValeRuntimeError valeRuntimeError, Project project) {
        String errorMessage = "❌ Vale lint failed\n Code: " + valeRuntimeError.code() +
                "\n Message: " + valeRuntimeError.text();
        ApplicationManager.getApplication().invokeLater(() -> writeTextToConsole(project,errorMessage,LOG_ERROR_OUTPUT ));
    }

    private void createAnnotation(@NotNull AnnotationHolder holder, @NotNull ValeProblem problem, TextRange range, PsiFile file) {
        AnnotationBuilder annotationBuilder = holder.newAnnotation(problem.getHighlightSeverity(),
                problem.message()).highlightType(problem.getProblemHighlightType());
        if (problem.isValidRangeForAnnotation(range, file.getViewProvider().getDocument())) {
            annotationBuilder = annotationBuilder.range(range);
        }
        annotationBuilder = addQuickFix(problem, range, annotationBuilder);
        if ("md".equalsIgnoreCase(file.getViewProvider().getVirtualFile().getExtension())) {
            // Avoid double annotations for markdown files that are also treated as HTML
            if (!"HTML".equalsIgnoreCase(file.getFileType().getDisplayName())) {
                annotationBuilder.create();
            }
        } else {
            annotationBuilder.create();
        }

    }

    private static AnnotationBuilder addQuickFix(@NotNull ValeProblem problem, TextRange range, AnnotationBuilder annotationBuilder) {
        String term = problem.match();
        switch (problem.action().name()) {
            case "replace":
                if (problem.action().parameters().isPresent()) {
                    String replacement = problem.action().parameters().get().getFirst();
                    annotationBuilder = annotationBuilder.withFix(new ValeReplaceQuickFix(term, replacement, range));
                }
                break;
            case "remove":
                annotationBuilder = annotationBuilder.withFix(new ValeRemoveQuickFix(term, range));
                break;
            case "ignore":
                LOGGER.info("Not implemented yet");
                break;
            case null:
            default:
                break;
        }
        return annotationBuilder;
    }


    private static List<ValeProblem> getValeProblems(InitialInfo collectedInfo, VirtualFile virtualFile,
                                              ValeCliExecutor cliExecutor,  Project project) {
        List<ValeProblem> result = new ArrayList<>();
        try {
            LOGGER.debug("Getting alerts via stdin for file" + virtualFile.getPath());
            ProcessOutput output = cliExecutor.runLintStdinCommand(
                    collectedInfo.document.getImmutableCharSequence(), virtualFile.getExtension());
            result = parseProcessOutput(output, project, cliExecutor);
        } catch (ExecutionException e) {
            LOGGER.debug("Vale execution exception: " + e.getMessage());
        }
        return result;
    }
}
