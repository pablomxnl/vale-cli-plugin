package org.ideplugins.vale_cli_plugin.actions;

import com.google.gson.JsonObject;
import com.intellij.analysis.problemsView.toolWindow.ProblemsView;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.FileContentUtil;
import org.ideplugins.vale_cli_plugin.exception.ValeCliExecutionException;
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor;
import org.ideplugins.vale_cli_plugin.service.ValeIssuesReporter;
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState;
import org.jetbrains.annotations.NotNull;
import org.zeroturnaround.exec.ProcessResult;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.intellij.execution.ui.ConsoleViewContentType.LOG_ERROR_OUTPUT;
import static java.util.Map.Entry;
import static org.ideplugins.vale_cli_plugin.actions.ActionHelper.*;

public class ValePopupAction extends AnAction {

    private static final Logger LOGGER = Logger.getInstance(ValePopupAction.class);


    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Project project = actionEvent.getProject();
        if (project != null) {
            Entry<Boolean, String> validation = getSettings().areSettingsValid();

            if (validation.getKey()) {
                PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
                ValeCliExecutor cliExecutor = project.getService(ValeCliExecutor.class);
                FileDocumentManager.getInstance().saveAllDocuments();

                ApplicationManager.getApplication().invokeLater(() -> {
                    ValeIssuesReporter reporter = project.getService(ValeIssuesReporter.class);
                    try {
                        if (psiFile != null) {
                            Future<ProcessResult> future = cliExecutor.executeValeCliOnFile(psiFile).getFuture();
                            Map<String, List<JsonObject>> results = cliExecutor.parseValeJsonResponse(future , 2);
                            updateResults(reporter, results);
                        } else {
                            VirtualFile[] virtualFiles = actionEvent.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
                            if (virtualFiles != null && virtualFiles.length > 1) {
                                List<String> filesToCheck = Arrays.stream(virtualFiles).map(VirtualFile::getPath).collect(Collectors.toList());
                                Future<ProcessResult> future = cliExecutor.executeValeCliOnFiles(filesToCheck).getFuture();
                                Map<String, List<JsonObject>> results = cliExecutor.parseValeJsonResponse(future, filesToCheck.size());
                                updateResults(reporter, results);
                            }
                        }
                        Optional.ofNullable(ToolWindowManager.getInstance(project).getToolWindow(ProblemsView.ID))
                                .ifPresent(toolWindow -> toolWindow.show(() -> ApplicationManager.getApplication()
                                        .invokeAndWait(FileContentUtil::reparseOpenedFiles)));

                    } catch (ValeCliExecutionException exception) {
                        LOGGER.info("Error executing Vale CLI for file or set of files\n" + exception.getMessage());
                        handleError(project, exception);
                    }
                });
            } else {
                displayNotification(NotificationType.WARNING, "Invalid Vale CLI plugin configuration");
                writeTextToConsole(project, validation.getValue(), LOG_ERROR_OUTPUT);
            }
        }
    }

    private static void updateResults(ValeIssuesReporter reporter, Map<String, List<JsonObject>> results) {
        results.forEach(reporter::updateIssuesForFile);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Optional.ofNullable(event.getProject()).ifPresent(project -> {
            ValeCliExecutor cliExecutor = ValeCliExecutor.getInstance(project);
            if (cliExecutor.isTaskRunning()) {
                event.getPresentation().setEnabled(false);
            } else {
                ValePluginSettingsState settings = getSettings();
                List<String> extensions = Arrays.stream(settings.extensions.split(",")).collect(Collectors.toList());
                AtomicBoolean shouldBeEnabled = new AtomicBoolean(false);

                PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
                VirtualFile[] files = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
                if (psiFile != null) {
                    String fileName = psiFile.getName();
                    extensions.forEach(extension -> shouldBeEnabled.set(shouldBeEnabled.get() || fileName.toLowerCase().endsWith(extension)));
                } else if (files != null) {
                    boolean allFiles = true;
                    for (VirtualFile file : files) {
                        allFiles = extensions.contains(file.getExtension()) && allFiles;
                    }
                    shouldBeEnabled.set(allFiles);
                }
                event.getPresentation().setEnabled(shouldBeEnabled.get());
            }
        });
    }

}
