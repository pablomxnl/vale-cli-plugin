package org.ideplugins.vale_cli_plugin.annotator;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor;
import org.ideplugins.vale_cli_plugin.settings.ValePluginProjectSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        ValePluginProjectSettingsState projectSettings = ValePluginProjectSettingsState.getInstance(project);
        ValeCliExecutor cliExecutor = ValeCliExecutor.getInstance(project);
        VirtualFile virtualFile = file.getViewProvider().getVirtualFile();
        Document document = file.getViewProvider().getDocument();
        boolean valeConfigurationFound = !projectSettings.getValeSettingsPath().isEmpty() ||
                !projectSettings.getRootIni().isEmpty();
        if  (   valeConfigurationFound &&
                cliExecutor.isAllowedByConfiguredExtensions(virtualFile.getExtension()) &&
                virtualFile.isInLocalFileSystem() && document !=null) {
            return new InitialInfo(file, document);
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
        if (!cliExecutor.isAllowedByConfiguredExtensions(virtualFile.getExtension())) {
            return null;
        }
        List<ValeProblem> results = new ArrayList<>();
        try {
            results.addAll(getValeProblems(collectedInfo, virtualFile, cliExecutor, project));
        } catch (Exception e){
            LOGGER.debug("Vale lint failed" + e);
            String errorMessage = "❌ Vale lint failed:\n" + e.getMessage();
            ApplicationManager.getApplication().invokeLater(
                    () -> writeTextToConsole(project, errorMessage, LOG_ERROR_OUTPUT),
                    ModalityState.defaultModalityState(),
                    project.getDisposed());
        }
        return new AnalysisResult(results, collectedInfo.document);
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
        ApplicationManager.getApplication().invokeLater(
                () -> writeTextToConsole(project, errorMessage, LOG_ERROR_OUTPUT),
                ModalityState.defaultModalityState(),
                project.getDisposed()
        );

    }

    private void createAnnotation(@NotNull AnnotationHolder holder, @NotNull ValeProblem problem, TextRange range, PsiFile file) {
        AnnotationBuilder annotationBuilder = holder.newAnnotation(problem.getHighlightSeverity(),
                formatMessage(problem));
        if (problem.isValidRangeForAnnotation(range, file.getViewProvider().getDocument())) {
            annotationBuilder = annotationBuilder.range(range);
        }
        String tooltip = ValeTooltipHelper.buildTooltip(problem);
        if (tooltip != null && !tooltip.isBlank()) {
            annotationBuilder = annotationBuilder.tooltip(tooltip);
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
        if (problem.action() == null || problem.action().name() == null || problem.action().name().isBlank()) {
            return annotationBuilder;
        }
        String actionName = problem.action().name();

        // We do not want to show the chooser for "replace" and "remove" if the result is unique anyway.
        // In those cases, it is enough to just apply the change without a second confirmation from the user.
        // For other actions like "suggest" and "edit", the replacement gets computed dynamically and is not readily
        // presented to the user beforehand. Therefore, we want a second confirmation.
        boolean alwaysShowChooser = !"replace".equalsIgnoreCase(actionName)
                && !"remove".equalsIgnoreCase(actionName);

        annotationBuilder = annotationBuilder.withFix(new ValeLazyFixQuickFix(problem, range, alwaysShowChooser));
        return annotationBuilder;
    }

    /**
     * Formats the annotation text shown in the editor by combining the problem message and rule name.
     */
    private static @NotNull String formatMessage(@NotNull ValeProblem problem) {
        String message = problem.message() == null ? "" : problem.message();
        String check = problem.check() == null ? "" : problem.check();
        if (message.isBlank()) {
            return check.isBlank() ? "Vale issue" : check;
        }
        if (check.isBlank()) {
            return message;
        }
        return message + " (" + check + ")";
    }

    private static List<ValeProblem> getValeProblems(InitialInfo collectedInfo, VirtualFile virtualFile,
                                              ValeCliExecutor cliExecutor,  Project project) throws IOException, ExecutionException {
        List<ValeProblem> result = new ArrayList<>();
        try {
            LOGGER.debug("Getting alerts via stdin for file" + virtualFile.getPath());
            ProcessOutput output = cliExecutor.runLintStdinCommand(
                    collectedInfo.document.getImmutableCharSequence(),
                    virtualFile.getExtension(),
                    virtualFile.getPath());
            result.addAll(parseProcessOutput(output, project, cliExecutor));
        } catch (ExecutionException | IOException e) {
            LOGGER.debug("Vale execution exception", e);
            throw e;
        }
        return result;
    }
}
