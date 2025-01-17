package org.ideplugins.vale_cli_plugin.activity

import com.intellij.ide.BrowserUtil
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore.getPlugin
import com.intellij.notification.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.apache.commons.lang3.StringUtils
import org.ideplugins.vale_cli_plugin.Constants
import org.ideplugins.vale_cli_plugin.listener.FileSavedListener
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor
import org.ideplugins.vale_cli_plugin.settings.ValeCliPluginConfigurationState
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState
import java.util.*

class ValeStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        ApplicationManager.getApplication().executeOnPooledThread {
            getValeFilesCount(
                project
            )
        }
        val listener = FileSavedListener.getInstance(project)
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(listener, listener)
        listener.activate()
        val id = PluginId.getId(Constants.PLUGIN_ID)
        val pluginDescriptor = getPlugin(id)
        if (pluginDescriptor != null) {
            val pluginSettings = ApplicationManager.getApplication().getService(
                ValeCliPluginConfigurationState::class.java
            )
            val lastKnownVersion = pluginSettings.lastVersion

            if (lastKnownVersion.isNotEmpty() && lastKnownVersion != pluginDescriptor.version) {
                showUpdateNotification(project, pluginDescriptor, pluginSettings)
            }
        }
    }

}

private fun getValeFilesCount(project: Project) {
    if (project.isDisposed) {
        return
    }
    val settingsState = ValePluginSettingsState.getInstance()

    if (StringUtils.isNotBlank(settingsState.extensions)) {
        val executor = ValeCliExecutor.getInstance(project)
        val scope = GlobalSearchScope.projectScope(project)
        ApplicationManager.getApplication().runReadAction {
            val numberOfFiles = Arrays.stream(
                settingsState.extensions.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            )
                .map { extension: String? ->
                    FilenameIndex.getAllFilesByExt(
                        project,
                        extension!!, scope
                    ).size
                }.reduce { a: Int, b: Int -> Integer.sum(a, b) }
                .orElse(0)
            executor.numberOfFiles = numberOfFiles
        }
    }
}

private fun showUpdateNotification(
    project: Project, pluginDescriptor: IdeaPluginDescriptor,
    pluginSettings: ValeCliPluginConfigurationState
) {
    ApplicationManager.getApplication().invokeLater {
        Optional.ofNullable(
            NotificationGroupManager.getInstance()
                .getNotificationGroup(Constants.NOTIFICATION_GROUP)
        ).ifPresent { group: NotificationGroup ->
            val action =
                NotificationAction.createSimple(
                    Constants.UPDATE_NOTIFICATION_BODY
                ) { BrowserUtil.browse(Constants.JB_MARKETPLACE_URL) }
            val notification = group.createNotification(
                Constants.UPDATE_NOTIFICATION_TITLE,
                "",
                NotificationType.INFORMATION
            ).addAction(action)
            Notifications.Bus.notify(notification, project)
            pluginSettings.lastVersion = pluginDescriptor.version
        }
    }
}