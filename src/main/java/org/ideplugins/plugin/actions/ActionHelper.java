package org.ideplugins.plugin.actions;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.apache.commons.lang.StringUtils;
import org.ideplugins.plugin.settings.ValePluginSettingsState;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

import static com.intellij.execution.ui.ConsoleViewContentType.LOG_ERROR_OUTPUT;

public class ActionHelper {

    private static final String GROUP_ID = "org.ideplugins.vale-cli-plugin";
    private static final ValePluginSettingsState settingsState = ValePluginSettingsState.getInstance();

    public static void displayNotification(final NotificationType notificationType, final String notificationBody) {
        Notification notification = new Notification(GROUP_ID, "Vale CLI", notificationBody, notificationType);
        Notifications.Bus.notify(notification);
    }

    public static void writeTextToConsole(@NotNull Project project, String text, ConsoleViewContentType level) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Vale CLI");
        ContentManager contentManager = Objects.requireNonNull(toolWindow).getContentManager();
        Content content = contentManager.findContent("Vale Results");
        ConsoleView consoleView = (ConsoleView) content.getComponent();
        consoleView.clear();
        consoleView.print(text, level);
        toolWindow.show(null);
    }

    public static boolean areSettingsValid(AnActionEvent event) {
        boolean result = true;
        StringBuilder errors = new StringBuilder();
        if (StringUtils.isBlank(settingsState.valePath)) {
            errors.append("Vale path couldn't be detected automatically, please set it up");
            result = false;
        }
        if (StringUtils.isNotBlank(settingsState.valeSettingsPath)) {
            File file = new File(settingsState.valeSettingsPath);
            if (!file.exists()) {
                errors.append("\nVale settings file doesn't exist");
                result = false;
            }
        }
        if (StringUtils.isBlank(settingsState.extensions)) {
            errors.append("\nVale extensions to check is not set.");
            result = false;
        }
        if (!result) {
            String message = "Please configure it on Settings -> Tools -> Vale CLI";
            displayNotification(NotificationType.WARNING,
                    "Vale configuration not set: " + message);
            writeTextToConsole(event.getProject(), errors + "\n" + message, LOG_ERROR_OUTPUT);
        }
        return result;
    }

    public static void handleError(Project project, Exception exception) {
        writeTextToConsole(project, "There was an error executing vale \n" +
                        "Please check paths for vale cli binary on Settings -> Tools -> Vale CLI\nError output:\n\t" +
                        exception.getMessage(),
                LOG_ERROR_OUTPUT);
    }
}
