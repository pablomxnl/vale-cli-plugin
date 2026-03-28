package org.ideplugins.vale_cli_plugin.activity

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationsConfiguration
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import org.ideplugins.vale_cli_plugin.Constants
import org.ideplugins.vale_cli_plugin.settings.ValePluginProjectSettingsState

class ValeStartupActivityTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        val notificationsConfiguration = NotificationsConfiguration.getNotificationsConfiguration()
        notificationsConfiguration.register(Constants.VALE_NOTIFICATION_GROUP, NotificationDisplayType.BALLOON, false, false)
        notificationsConfiguration.register(Constants.UPDATE_NOTIFICATION_GROUP, NotificationDisplayType.BALLOON, false, false)
        val vpsp = ValePluginProjectSettingsState(project)
        val mystate: ValePluginProjectSettingsState.State? = vpsp.state
        mystate?.runSyncOnStartup = true
        mystate?.valeSettingsPath = ""
        project.getService(ValePluginProjectSettingsState::class.java)?.loadState(mystate!!)
    }

    fun testActivityRuns() = runBlocking {
        val activity = ValeStartupActivity()
        activity.execute(project)
    }

    fun testProjectActivityRunsWithBadFile() = runBlocking {
        project.getService(ValePluginProjectSettingsState::class.java)?.valeSettingsPath = "./tmp/doesnotexit.ini"
        val activity = ValeStartupActivity()
        activity.execute(project)
    }

}
