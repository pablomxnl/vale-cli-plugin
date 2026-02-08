package org.ideplugins.vale_cli_plugin.activity

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import org.ideplugins.vale_cli_plugin.settings.ValePluginProjectSettingsState

class ValeStartupActivityTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
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