package org.ideplugins.vale_cli_plugin.actions;

import com.google.gson.JsonObject;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.FileContentUtil;
import com.jetbrains.rd.util.AtomicReference;
import org.ideplugins.vale_cli_plugin.exception.ValeCliExecutionException;
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor;
import org.ideplugins.vale_cli_plugin.service.ValeIssuesReporter;
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeroturnaround.exec.StartedProcess;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.intellij.execution.ui.ConsoleViewContentType.LOG_ERROR_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.LOG_INFO_OUTPUT;
import static java.util.Map.Entry;
import static org.ideplugins.vale_cli_plugin.actions.ActionHelper.*;

public class ValeToolsMenuAction extends AnAction {

    private static final Logger LOGGER = Logger.getInstance(ValeToolsMenuAction.class);
    protected static final ResourceBundle BUNDLE = ResourceBundle.getBundle("ValePlugin");

    protected static void executeValeInBackground(ValeCliExecutor cliExecutor, ValeIssuesReporter reporter,
                                                  @NotNull ProgressIndicator indicator,
                                                  AtomicReference<StartedProcess> processReference, int filesNumber)
            throws ValeCliExecutionException {
        Map<String, List<JsonObject>> results =
                cliExecutor.parseValeJsonResponse(processReference.get().getFuture(), indicator, filesNumber);
        reporter.populateIssuesFromValeResponse(results);
    }

    protected static void killProcess(AtomicReference<StartedProcess> processReference) {
        Optional.ofNullable(processReference.get()).ifPresent(reference -> reference.getFuture().cancel(true));
    }

    protected static void reparseAndDisplaySuccessMessage(ValeIssuesReporter reporter, Project project, ValeCliExecutor cliExecutor) {
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
                createBackgroundValeProcess(project, null);
            } else {
                displayNotification(NotificationType.WARNING, BUNDLE.getString("invalid.notification.body"));
                writeTextToConsole(project, validation.getValue(), LOG_ERROR_OUTPUT);
            }
        }
    }

    protected void createBackgroundValeProcess(@NotNull Project project,
                                               @Nullable VirtualFile dir) {
        ValePluginSettingsState settings = ValePluginSettingsState.getInstance();
        ValeCliExecutor cliExecutor = ValeCliExecutor.getInstance(project);
        ValeIssuesReporter reporter = project.getService(ValeIssuesReporter.class);
        String path = (dir == null)? project.getBasePath() : dir.getPath();
        int numberOfFiles = (dir == null)? countFilesToLint(project, settings.extensionsAsList()) :
                countFilesToLint(dir, settings.extensionsAsList());
        String title = (dir == null)?
                "Executing vale cli on project scope for %s files".formatted(numberOfFiles) :
                "Executing lint on directory %s, for %s files ".formatted(path, numberOfFiles);
        AtomicReference<StartedProcess> processReference = new AtomicReference<>(null);
        //TODO: migrate to TasksKt.withBackgroundProgress
        ApplicationManager.getApplication().runReadAction(() ->
                ProgressManager.getInstance().run(new Task.Backgroundable(project, title) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    processReference.getAndSet(cliExecutor.executeValeCliOnPath(path));
                    executeValeInBackground(cliExecutor, reporter, indicator, processReference, numberOfFiles);
                } catch (ValeCliExecutionException exception) {
                    LOGGER.info("Error executing Vale CLI\n" + exception.getMessage());
                    handleError(project, exception);
                }
            }

            @Override
            public void onSuccess() {
                Optional.ofNullable(processReference.get()).ifPresent(reference -> {
                    if (reference.getProcess().exitValue() == 0) {
                        reparseAndDisplaySuccessMessage(reporter, project, cliExecutor);
                    }
                });
            }

            @Override
            public void onCancel() {
                killProcess(processReference);
            }
        }));

    }

    protected int countFilesToLint(VirtualFile directory, List<String> extensions) {
        AtomicInteger count = new AtomicInteger(0);
        VfsUtilCore.processFilesRecursively(directory, virtualFile -> {
            if (!virtualFile.isDirectory() && extensions.contains(virtualFile.getExtension())){
                count.incrementAndGet();
            }
            return true;
        });
        return count.get();
    }

    private int countFilesToLint(Project project, List<String> extensions){
        int result = 0;
        String path = project.getBasePath();
        if (path != null) {
            result = countFilesToLint(VirtualFileManager.getInstance().findFileByNioPath(Path.of(path)), extensions);
        }
        return result;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
