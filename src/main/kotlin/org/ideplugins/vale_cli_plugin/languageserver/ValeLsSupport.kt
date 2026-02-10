package org.ideplugins.vale_cli_plugin.languageserver

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dev.j_a.ide.lsp.api.BaseLanguageServerSupport
import dev.j_a.ide.lsp.api.LanguageServerSupport
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState

object ValeLsSupport : BaseLanguageServerSupport (
    "org.ideplugins.vale_cli_plugin.languageserver",
    "Vale LS Support"
) {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LanguageServerSupport.LanguageServerStarter
    ) {
        val settings = ValePluginSettingsState.getInstance()
        if ( settings.extensionsAsList().contains(file.extension) || "vale.ini" == file.name ||
            ".vale.ini" == file.name){
            serverStarter.ensureStarted(ValeLsServerDescriptor(project))
        }
    }
}