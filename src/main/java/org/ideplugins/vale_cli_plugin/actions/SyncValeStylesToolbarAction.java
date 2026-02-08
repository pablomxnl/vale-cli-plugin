package org.ideplugins.vale_cli_plugin.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.CapturingProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.platform.ide.progress.TasksKt;
import org.ideplugins.vale_cli_plugin.Constants;
import org.ideplugins.vale_cli_plugin.utils.NotificationHelper;
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

import static com.intellij.execution.ui.ConsoleViewContentType.LOG_ERROR_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.LOG_INFO_OUTPUT;
import static org.ideplugins.vale_cli_plugin.utils.ConsoleHelper.writeTextToConsole;


public class SyncValeStylesToolbarAction extends AnAction {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(Constants.PLUGIN_BUNDLE);

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project != null) {
            ValeCliExecutor cliExecutor = ValeCliExecutor.getInstance(project);
            NotificationHelper helper = new NotificationHelper(project);
            String errors = cliExecutor.checkConfiguration();
            if (!errors.isEmpty()) {
                helper.showNotificationWithConfigurationActions(BUNDLE.getString("invalid.notification.body"), NotificationType.WARNING);
                writeTextToConsole(project, errors, LOG_ERROR_OUTPUT);
                return;
            }
            ProcessListener listener = new SyncProcessListener(project);
            TasksKt.runWithModalProgressBlocking(project, BUNDLE.getString("sync.modal.title"), (coroutineScope, continuation) -> {
                try {
                    return cliExecutor.runSyncCommand(listener);
                } catch (ExecutionException e) {
                    ApplicationManager.getApplication().invokeLater(() ->
                            writeTextToConsole(project, "❌ Failed to execute vale sync: " + e.getMessage(), LOG_ERROR_OUTPUT));
                }
                return null;
            });
        }
    }


    private static class SyncProcessListener extends CapturingProcessAdapter {
        private final Project project;

        SyncProcessListener(@NotNull Project project) {
            this.project = project;
        }

        @Override
        public void processTerminated(@NotNull ProcessEvent event) {
            ApplicationManager.getApplication().invokeLater(()-> {
                if (event.getExitCode() == 0) {
                    writeTextToConsole(project, "✅ " + BUNDLE.getString("vale.cli.plugin.syncsuccess.notification"), LOG_INFO_OUTPUT);
                } else {
                    writeTextToConsole(project, "❌ " + BUNDLE.getString("vale.cli.plugin.syncfailed.consolemessage")
                                    + event.getExitCode() + "\n" + getOutput().getStderr(),
                            LOG_ERROR_OUTPUT);
                }
            });
        }
    }
}
