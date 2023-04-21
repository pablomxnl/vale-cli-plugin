package org.ideplugins.vale_cli_plugin.actions;

import com.google.gson.JsonObject;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.FileContentUtil;
import com.jetbrains.rd.util.AtomicReference;
import org.ideplugins.vale_cli_plugin.exception.ValeCliExecutionException;
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor;
import org.ideplugins.vale_cli_plugin.service.ValeIssuesReporter;
import org.jetbrains.annotations.NotNull;
import org.zeroturnaround.exec.StartedProcess;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.intellij.execution.ui.ConsoleViewContentType.LOG_ERROR_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.LOG_INFO_OUTPUT;
import static java.util.Map.Entry;
import static org.ideplugins.vale_cli_plugin.actions.ActionHelper.*;

public class ValeToolsMenuAction extends AnAction {

    private static final Logger LOGGER = Logger.getInstance(ValeToolsMenuAction.class);

    private static void executeValeInBackground(ValeCliExecutor cliExecutor, ValeIssuesReporter reporter,
                                                @NotNull ProgressIndicator indicator, AtomicReference<StartedProcess> processReference)
            throws ValeCliExecutionException {
        processReference.getAndSet(cliExecutor.executeValeCliOnProject());
        Map<String, List<JsonObject>> results = cliExecutor.parseValeJsonResponse(processReference.get().getFuture(), indicator);
        reporter.populateIssuesFromValeResponse(results);
    }

    private static void killProcess(AtomicReference<StartedProcess> processReference) {
        processReference.get().getFuture().cancel(true);
    }

    private static void reparseAndDisplaySuccessMessage(ValeIssuesReporter reporter, Project project, ValeCliExecutor cliExecutor) {
        ApplicationManager.getApplication().invokeAndWait(FileContentUtil::reparseOpenedFiles);
        ApplicationManager.getApplication().invokeLater(() -> {
            String message = reporter.getTotalIssues();
            writeTextToConsole(project,
                    message + "\n" + "See the results in the problemview of a supported file\n" +
                            String.format("Execution time: %s seconds", cliExecutor.getExecutionTime()),
                    LOG_INFO_OUTPUT);
        });
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Optional.ofNullable(event.getProject()).ifPresent(project -> {
            ValeCliExecutor cliExecutor = ValeCliExecutor.getInstance(project);
            event.getPresentation().setEnabled(!cliExecutor.isTaskRunning());
        });
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Project project = actionEvent.getProject();
        if (project != null) {
            Entry<Boolean, String> validation = getSettings().areSettingsValid();
            if (validation.getKey()) {
                FileDocumentManager.getInstance().saveAllDocuments();
                ValeCliExecutor cliExecutor = ValeCliExecutor.getInstance(project);
                ValeIssuesReporter reporter = project.getService(ValeIssuesReporter.class);
                AtomicReference<StartedProcess> processReference = new AtomicReference<>(null);

                ApplicationManager.getApplication().runReadAction(() -> ProgressManager.getInstance().run(new Task.Backgroundable(project,
                        String.format("Executing vale cli on project scope for %s files",
                                cliExecutor.getNumberOfFiles())) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        try {
                            executeValeInBackground(cliExecutor, reporter, indicator, processReference);
                        } catch (ValeCliExecutionException exception) {
                            LOGGER.info("Error executing Vale CLI for project\n" + exception.getMessage());
                            handleError(project, exception);
                        }
                    }

                    @Override
                    public void onSuccess() {
                        if (processReference.get().getProcess().exitValue() == 0){
                            reparseAndDisplaySuccessMessage(reporter, project, cliExecutor);
                        }
                    }

                    @Override
                    public void onCancel() {
                        killProcess(processReference);
                    }
                }));
            } else {
                displayNotification(NotificationType.WARNING, "Invalid Vale CLI plugin configuration");
                writeTextToConsole(project, validation.getValue(), LOG_ERROR_OUTPUT);
            }
        }
    }

}
