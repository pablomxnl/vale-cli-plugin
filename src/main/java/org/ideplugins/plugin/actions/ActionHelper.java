package org.ideplugins.plugin.actions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.intellij.execution.ui.ConsoleViewContentType.*;

public class ActionHelper {

    private static final String GROUP_ID = "org.ideplugins.vale-cli-plugin";
    private static final ValePluginSettingsState settingsState = ValePluginSettingsState.getInstance();

    public static void displayNotification(final NotificationType notificationType, final String notificationBody) {
        Notification notification = new Notification(GROUP_ID, "Vale CLI", notificationBody, notificationType);
        Notifications.Bus.notify(notification);
    }

    public static void writeTextToConsole(@NotNull Project project, String text, ConsoleViewContentType level) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Vale CLI");
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager.findContent("Vale Results");
        ConsoleView consoleView = (ConsoleView) content.getComponent();
        consoleView.clear();
        consoleView.print(text, level);
        toolWindow.show(null);
    }

    public static void showResultsInConsole(Project project, String text) {
        if (StringUtils.isBlank(text)) return;
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Vale CLI");
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager.findContent("Vale Results");
        ConsoleView consoleView = (ConsoleView) content.getComponent();
        consoleView.clear();

        Map<String, Integer> resultsPerSeverity = new HashMap<>();
        Set<String> files = new HashSet<>();

        JsonElement element = JsonParser.parseString(text);
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            jsonObject.keySet().forEach(key -> {
                JsonArray jsonArray = jsonObject.getAsJsonArray(key);
                jsonArray.forEach(jsonElement -> {
                    JsonObject object = jsonElement.getAsJsonObject();
                    String severity = object.get("Severity").getAsString();
                    resultsPerSeverity.merge(severity, 1, Integer::sum);
                    ConsoleViewContentType level = getSeverity(severity);
                    String fileName = key.substring(project.getBasePath().length() + 1);
                    files.add(fileName);
                    String pattern = "suggestion".equalsIgnoreCase(severity) ? "{0}\t\t{1}:{2}\t\t{3}\n" :
                            "{0}\t\t\t{1}:{2}\t\t{3}\n";
                    String message =
                            MessageFormat.format(pattern, severity, fileName, object.get("Line").getAsString(),
                                    object.get("Message").getAsString());
                    consoleView.print(message, level);
                });
            });
        }
        consoleView.print("\n Vale found ", LOG_INFO_OUTPUT);
        resultsPerSeverity.forEach((key, value) -> {
            ConsoleViewContentType level = getSeverity(key);
            String message = MessageFormat.format("{0} {1}s, ", value, key);
            consoleView.print(message, level);
        });
        consoleView.print(String.format(" in %s files", files.size()), LOG_INFO_OUTPUT);
        toolWindow.show(null);
    }

    private static ConsoleViewContentType getSeverity(String severity) {
        switch (severity) {
            case "warning":
                return LOG_WARNING_OUTPUT;
            case "error":
                return LOG_ERROR_OUTPUT;
            case "suggestion":
            default:
                return LOG_INFO_OUTPUT;
        }
    }

    public static boolean areSettingsValid(AnActionEvent event) {
        boolean result = true;
        StringBuilder errors = new StringBuilder();
        if (StringUtils.isBlank(settingsState.valePath)) {
            errors.append("Vale path is not set.");
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

}
