package org.ideplugins.vale_cli_plugin.languageserver

import com.google.gson.JsonObject
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dev.j_a.ide.lsp.api.descriptor.CommandLineLanguageServerDescriptor
import org.eclipse.lsp4j.InitializeParams
import org.ideplugins.vale_cli_plugin.settings.ValeCliPluginConfigurationState
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState
import java.nio.file.Files
import java.nio.file.Path

class ValeLsServerDescriptor(project: Project) : CommandLineLanguageServerDescriptor(
    project, ValeLsSupport, true
) {
    override fun createCommandLine(): GeneralCommandLine {
        val pluginSettings = ApplicationManager.getApplication().getService(
            ValeCliPluginConfigurationState::class.java
        )
        require(
            pluginSettings.valeLsPath != null && pluginSettings.valeLsPath.isNotEmpty() && Files.exists(
                Path.of(pluginSettings.valeLsPath)
            )
        ) { "Language Server not ready yet" }
        return GeneralCommandLine(pluginSettings.valeLsPath)
    }


    override fun isSupported(file: VirtualFile): Boolean {
        val settings = ValePluginSettingsState.getInstance()
        return settings.extensionsAsList()
            .contains(file.extension) || "vale.ini" == file.name || ".vale.ini" == file.name
    }

    /**
    installVale	true	Automatically install and update Vale to a vale_bin folder in the same location as vale-ls. If false, the vale executable needs to be available on the user’s $PATH.
    filter	None	An output filter to apply when calling Vale.
    configPath	None	An absolute path to a .vale.ini file to be used as the default configuration.
    syncOnStartup	true	Runs vale sync upon starting the server.
     */
    override fun customize(initParams: InitializeParams) {
        var settings = ValePluginSettingsState.getInstance()
        initParams.initializationOptions = JsonObject().apply {
            addProperty("installVale", settings.installVale)
            addProperty("syncOnStartup", settings.syncVale)
            addProperty("configPath", settings.valeSettingsPath)
        }
    }

}