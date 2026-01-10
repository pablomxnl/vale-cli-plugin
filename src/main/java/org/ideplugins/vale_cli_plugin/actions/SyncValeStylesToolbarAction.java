package org.ideplugins.vale_cli_plugin.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.CapturingProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.ResourceBundle;

import static com.intellij.execution.ui.ConsoleViewContentType.LOG_ERROR_OUTPUT;
import static com.intellij.execution.ui.ConsoleViewContentType.LOG_INFO_OUTPUT;
import static org.ideplugins.vale_cli_plugin.actions.ActionHelper.displayNotification;
import static org.ideplugins.vale_cli_plugin.actions.ActionHelper.writeTextToConsole;


public class SyncValeStylesToolbarAction extends AnAction {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("ValePlugin");

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project != null) {
            ValeCliExecutor cliExecutor = ValeCliExecutor.getInstance(project);
            String errors = cliExecutor.checkConfiguration();
            if (!errors.isEmpty()){
                displayNotification(NotificationType.WARNING, BUNDLE.getString("invalid.notification.body"));
                writeTextToConsole(project, errors, LOG_ERROR_OUTPUT);
                return;
            }
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Vale CLI");
            if (toolWindow != null) {
                Optional<ConsoleView> optional = ActionHelper.getConsoleView(project, toolWindow);
                if (optional.isPresent()) {
                    ConsoleView consoleView = optional.get();
                    ProcessListener listener = new SyncProcessListener(consoleView, toolWindow);
                    consoleView.clear();
                    try {
                        cliExecutor.runSyncCommand(listener);
                    } catch (ExecutionException e) {
                        consoleView.print("❌ Failed to execute vale sync: " + e.getMessage() + "\n",
                                LOG_ERROR_OUTPUT);
                    }
                }
            }
        }

    }

    private static class SyncProcessListener extends CapturingProcessAdapter {
        private final @NotNull ConsoleView consoleView;
        private final ToolWindow toolWindow;

        SyncProcessListener(@NotNull ConsoleView consoleView, ToolWindow toolWindow) {
            this.consoleView = consoleView;
            this.toolWindow = toolWindow;
        }

        @Override
        public void startNotified(@NotNull ProcessEvent event) {
            consoleView.print("🔄 Starting Vale sync...\n", LOG_INFO_OUTPUT);
        }

        @Override
        public void processTerminated(@NotNull ProcessEvent event) {
            if (event.getExitCode() == 0) {
                consoleView.print("✅ Vale sync completed successfully.\n",
                        LOG_INFO_OUTPUT);
            } else {
                consoleView.print("❌ Vale sync failed with exit code: "
                                + event.getExitCode() + "\n" + getOutput().getStderr(),
                        LOG_ERROR_OUTPUT);
            }
            toolWindow.activate(null);
        }
    }
}
