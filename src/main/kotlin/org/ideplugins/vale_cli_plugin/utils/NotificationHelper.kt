package org.ideplugins.vale_cli_plugin.utils

import com.intellij.ide.BrowserUtil
import com.intellij.ide.IdeBundle
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import org.ideplugins.vale_cli_plugin.Constants
import org.ideplugins.vale_cli_plugin.Constants.PLUGIN_BUNDLE
import org.ideplugins.vale_cli_plugin.settings.ValePluginProjectSettingsConfigurable
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsConfigurable
import java.util.ResourceBundle

class NotificationHelper(
    private val project: Project
) {

    private val bundle: ResourceBundle = ResourceBundle.getBundle(PLUGIN_BUNDLE)
    private val valeGroup =
        NotificationGroupManager.getInstance()
            .getNotificationGroup(Constants.VALE_NOTIFICATION_GROUP)

    private val pluginUpdatedNotificationGroup =
        NotificationGroupManager.getInstance()
            .getNotificationGroup(Constants.UPDATE_NOTIFICATION_GROUP)

    fun notifySyncSuccess() {
        valeGroup?.let {
            Notifications.Bus.notify(
                it.createNotification("✅ ${bundle.getString("vale.cli.plugin.syncsuccess.notification")}",
                    "",
                    NotificationType.INFORMATION))
        }

    }

    fun notifySyncFailure(details: String?) {
        val notification = create(
            valeGroup,
            "❌ ${bundle.getString("vale.cli.plugin.syncfail.notification")}",
            details ?: "",
            NotificationType.ERROR
        )
            .addGlobalSettingsAction()
            .addProjectSettingsAction()


        Notifications.Bus.notify(notification, project)
    }

    fun showPluginWasUpdatedNotification() {
        val notification = create(
            pluginUpdatedNotificationGroup,
            Constants.UPDATE_NOTIFICATION_TITLE,
            "",
            NotificationType.INFORMATION
        ).addAction(
            NotificationAction.createSimple(Constants.UPDATE_NOTIFICATION_BODY) {
                BrowserUtil.browse(Constants.JB_MARKETPLACE_URL)
            }
        )
        Notifications.Bus.notify(notification, project)
    }

    fun showNotificationWithConfigurationActions(content: String, type: NotificationType){
        val notification = create(valeGroup, "Vale CLI", content, type)
            .addGlobalSettingsAction()
            .addProjectSettingsAction()
        Notifications.Bus.notify(notification, project)
    }

    fun showNotificationWithGoToPluginsAction(title: String, content: String, type: NotificationType) {
        val notification = create(valeGroup, title, "", type)
            .addGoToPluginsAction(content)
        Notifications.Bus.notify(notification, project)
    }


    private fun create(
        group: NotificationGroup?,
        title: String,
        content: String,
        type: NotificationType
    ): Notification =
        requireNotNull(group).createNotification(title, content, type)

    private fun Notification.addProjectSettingsAction(): Notification =
        addAction(
            NotificationAction.createSimple("Vale project settings") {
                ShowSettingsUtil.getInstance()
                    .showSettingsDialog(project, ValePluginProjectSettingsConfigurable::class.java)
            }
        )

    private fun Notification.addGlobalSettingsAction(): Notification =
        addAction(
            NotificationAction.createSimple("Configure vale path") {
                ShowSettingsUtil.getInstance()
                    .showSettingsDialog(project, ValePluginSettingsConfigurable::class.java)
            }
        )

    private fun Notification.addGoToPluginsAction(text:String): Notification =
        addAction(
            NotificationAction.createSimple(text) {
                ShowSettingsUtil.getInstance()
                    .showSettingsDialog(project, IdeBundle.message("title.plugins"))
            }
        )


}