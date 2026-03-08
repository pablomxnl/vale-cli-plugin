package org.ideplugins.vale_cli_plugin.activity

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.ide.plugins.PluginManagerCore.getPlugin
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.ideplugins.vale_cli_plugin.Constants
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor
import org.ideplugins.vale_cli_plugin.service.ValeLsConfigService
import org.ideplugins.vale_cli_plugin.settings.*
import org.ideplugins.vale_cli_plugin.utils.NotificationHelper

class ValeStartupActivity : ProjectActivity {

    private val logger = Logger.getInstance(ValeStartupActivity::class.java)

    override suspend fun execute(project: Project) {
        val notificationHelper = NotificationHelper(project)

        configureValePath()
        checkValeConfig(project)
        runSyncIfNeeded(project, notificationHelper)
        checkIfPluginWasUpdated(notificationHelper)
    }

    private fun checkValeConfig(project: Project) {
        val settings = ValePluginSettingsState.getInstance()
        if (settings.valePath.isNullOrEmpty() ) {
            return
        }
        val projectSettings = ValePluginProjectSettingsState.getInstance(project)
        if (!ApplicationManager.getApplication().isUnitTestMode) {
            if (projectSettings.valeSettingsPath.isEmpty() && projectSettings.rootIni.isEmpty()){
                logger.info("Running vale-ls config")
                val lsConfigService = ValeLsConfigService.getInstance(project)
                val configuration = lsConfigService.loadConfigurationPaths()
                projectSettings.rootIni = configuration.rootIni()
            } else {
                logger.info("Not running vale-ls config again")
            }
        } else {
            projectSettings.rootIni = ".vale.ini"
        }
    }

    private fun checkIfPluginWasUpdated(notificationHelper: NotificationHelper) {
        val id = PluginId.getId(Constants.PLUGIN_ID)
        val pluginDescriptor = getPlugin(id) ?: return

        val settings = ApplicationManager.getApplication()
            .getService(ValeCliPluginConfigurationState::class.java)

        if (
            settings.lastVersion.isNotEmpty() &&
            settings.lastVersion != pluginDescriptor.version
        ) {
            invokeLater {
                notificationHelper.showPluginWasUpdatedNotification()
                settings.lastVersion = pluginDescriptor.version
            }
        }
    }

    private fun configureValePath() {
        val settings = ValePluginSettingsState.getInstance()
        if (settings.valePath.isBlank()) {
            logger.info("Finding vale path")
            settings.valePath = OSUtils.findValeBinaryPath()
        }
    }

    private fun runSyncIfNeeded(project: Project, notificationHelper: NotificationHelper) {
        val projectSettings = ValePluginProjectSettingsState.getInstance(project)
        val settings = ValePluginSettingsState.getInstance()

        if (settings.valePath.isNullOrEmpty() || !projectSettings.runSyncOnStartup || projectSettings.rootIni.isEmpty()) {
            return
        }

        val cliExecutor = ValeCliExecutor.getInstance(project)

        try {
            cliExecutor.runSyncCommand(object : CapturingProcessAdapter() {
                override fun processTerminated(event: ProcessEvent) {
                    if (event.exitCode == 0) {
                        notificationHelper.notifySyncSuccess()
                    } else {
                        notificationHelper.notifySyncFailure(
                            cliExecutor.parseErrorProcessOutput(output).text
                        )
                    }
                }
            })
        } catch (e: ExecutionException) {
            logger.debug(e)
            notificationHelper.notifySyncFailure(e.message)
        }
    }
}