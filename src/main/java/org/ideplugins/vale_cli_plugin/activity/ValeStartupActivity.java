package org.ideplugins.vale_cli_plugin.activity;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang3.StringUtils;
import org.ideplugins.vale_cli_plugin.listener.FileSavedListener;
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor;
import org.ideplugins.vale_cli_plugin.settings.ValeCliPluginConfigurationState;
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState;
import org.ideplugins.settings.SettingsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

import static org.ideplugins.vale_cli_plugin.Constants.*;


public class ValeStartupActivity implements StartupActivity {

    private static void getValeFilesCount(Project project) {
        if (project.isDisposed()) {
            return;
        }
        ValePluginSettingsState settingsState = ValePluginSettingsState.getInstance();

        if (StringUtils.isNotBlank(settingsState.extensions)) {
            ValeCliExecutor executor = project.getService(ValeCliExecutor.class);
            GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
            ApplicationManager.getApplication().runReadAction(() -> {
                int numberOfFiles = Arrays.stream(settingsState.extensions.split(",")).map(extension ->
                        FilenameIndex.getAllFilesByExt(project, extension, scope).size()).reduce(Integer::sum).orElse(0);
                executor.setNumberOfFiles(numberOfFiles);
            });
        }
    }

    private static void showUpdateNotification(Project project, IdeaPluginDescriptor pluginDescriptor,
                                               ValeCliPluginConfigurationState pluginSettings) {
        ApplicationManager.getApplication().invokeLater(() -> Optional.ofNullable(NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP)).ifPresent(group -> {
            NotificationAction action =
                    NotificationAction.createSimple(UPDATE_NOTIFICATION_BODY, () -> BrowserUtil.browse(JB_MARKETPLACE_URL));
            Notification notification = group.createNotification(
                    UPDATE_NOTIFICATION_TITLE,
                    "",
                    NotificationType.INFORMATION).addAction(action);
            Notifications.Bus.notify(notification, project);
            pluginSettings.setLastVersion(pluginDescriptor.getVersion());
        }));
    }

    @Override
    public void runActivity(@NotNull Project project) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> getValeFilesCount(project));
        FileSavedListener listener = FileSavedListener.getInstance(project);
        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(listener, listener);
        listener.activate();
        PluginId id = PluginId.getId(PLUGIN_ID);
        IdeaPluginDescriptor pluginDescriptor = PluginManagerCore.getPlugin(id);
        if (pluginDescriptor != null) {
            ValeCliPluginConfigurationState pluginSettings = ApplicationManager.getApplication().getService(ValeCliPluginConfigurationState.class);
            String lastKnownVersion = pluginSettings.getLastVersion();

            pluginSettings.setSentryDsn(SettingsProvider.getInstance().getSentryUrl(id.getIdString()));

            if (!lastKnownVersion.isEmpty() && !lastKnownVersion.equals(pluginDescriptor.getVersion())) {
                showUpdateNotification(project, pluginDescriptor, pluginSettings);
            }
        }
    }
}

