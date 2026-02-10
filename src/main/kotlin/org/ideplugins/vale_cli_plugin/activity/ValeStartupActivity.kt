package org.ideplugins.vale_cli_plugin.activity

import com.intellij.ide.BrowserUtil
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore.getPlugin
import com.intellij.notification.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.ideplugins.vale_cli_plugin.Constants
import org.ideplugins.vale_cli_plugin.settings.ValeCliPluginConfigurationState
import java.util.*

class ValeStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
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

private fun showUpdateNotification(
    project: Project, pluginDescriptor: IdeaPluginDescriptor, pluginSettings: ValeCliPluginConfigurationState
) {
    ApplicationManager.getApplication().invokeLater {
        Optional.ofNullable(
            NotificationGroupManager.getInstance().getNotificationGroup(Constants.NOTIFICATION_GROUP)
        ).ifPresent { group: NotificationGroup ->
            val action = NotificationAction.createSimple(
                Constants.UPDATE_NOTIFICATION_BODY
            ) { BrowserUtil.browse(Constants.JB_MARKETPLACE_URL) }
            val notification = group.createNotification(
                Constants.UPDATE_NOTIFICATION_TITLE, "", NotificationType.INFORMATION
            ).addAction(action)
            Notifications.Bus.notify(notification, project)
            pluginSettings.lastVersion = pluginDescriptor.version
        }
    }
}