package org.ideplugins.plugin.actions;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.ideplugins.plugin.settings.ValePluginSettingsConfigurable;
import org.ideplugins.plugin.settings.ValePluginSettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.intellij.execution.ui.ConsoleViewContentType.LOG_ERROR_OUTPUT;

public class ActionHelper {

    private static final String GROUP_ID = "org.ideplugins.vale-cli-plugin";
    private static final ValePluginSettingsState settingsState = ValePluginSettingsState.getInstance();

    public static void displayNotification(final NotificationType notificationType, final String notificationBody) {
        Notification notification = new Notification(GROUP_ID, "Vale CLI", notificationBody, notificationType);
        notification.addAction(new NotificationAction("Click here to configure Vale CLI") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent, @NotNull Notification notification) {
                ShowSettingsUtil.getInstance().showSettingsDialog(null, ValePluginSettingsConfigurable.class);
            }
        });
        Notifications.Bus.notify(notification);
    }

    public static ValePluginSettingsState getSettings(){
        return settingsState;
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

    public static void handleError(Project project, Exception exception) {
        ApplicationManager.getApplication().invokeLater(()->{
            writeTextToConsole(project, "There was an error executing vale \n" +
                            "Please check paths for vale cli binary on Settings -> Tools -> Vale CLI\nError output:\n\t" +
                            exception.getMessage(),
                    LOG_ERROR_OUTPUT);
        });
    }
}