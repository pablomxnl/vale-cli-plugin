package org.ideplugins.vale_cli_plugin.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.CommonProcessors;
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor;
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static com.intellij.execution.ui.ConsoleViewContentType.LOG_ERROR_OUTPUT;
import static java.util.Map.Entry;
import static org.ideplugins.vale_cli_plugin.actions.ActionHelper.displayNotification;
import static org.ideplugins.vale_cli_plugin.actions.ActionHelper.writeTextToConsole;

public class ValePopupDirectoryAction extends ValeToolsMenuAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Project project = actionEvent.getProject();
        VirtualFile dir = actionEvent.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project != null) {
            Entry<Boolean, String> validation = ValePluginSettingsState.getInstance().areSettingsValid();
            if (validation.getKey() && dir != null) {
                createBackgroundValeProcess(project, dir);
            } else {
                displayNotification(NotificationType.WARNING, BUNDLE.getString("invalid.notification.body"));
                writeTextToConsole(project, validation.getValue(), LOG_ERROR_OUTPUT);
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Optional.ofNullable(event.getProject()).ifPresent(project -> {
            ValeCliExecutor cliExecutor = ValeCliExecutor.getInstance(project);
            ValePluginSettingsState settings = ValePluginSettingsState.getInstance();
            if (cliExecutor.isTaskRunning()) {
                event.getPresentation().setEnabled(false);
            } else {
                Optional.ofNullable(event.getData(CommonDataKeys.VIRTUAL_FILE)).ifPresent(dir -> {
                    event.getPresentation().setEnabled(dirContainsFilesToLint(dir, settings.extensionsAsList()));
                });
            }
        });
    }

    private boolean dirContainsFilesToLint(VirtualFile directory, List<String> extensions) {
        if (!directory.isDirectory()) {
            return false;
        }

        CommonProcessors.FindFirstProcessor<VirtualFile> processor = new CommonProcessors.FindFirstProcessor<>() {
            @Override
            protected boolean accept(@NotNull VirtualFile file) {
                if (!file.isDirectory()) {
                    String extension = file.getExtension();
                    return extension != null && extensions.contains(extension);
                }
                return false;
            }
        };

        VfsUtilCore.processFilesRecursively(directory, processor);
        return processor.isFound();
    }
}
