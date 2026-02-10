package org.ideplugins.vale_cli_plugin.languageserver


import com.intellij.icons.AllIcons
import dev.j_a.ide.lsp.api.statusBar.LanguageServerStatusBarWidgetFactory
import org.ideplugins.vale_cli_plugin.Constants

class ValeLsStatusBarWidgetFactory : LanguageServerStatusBarWidgetFactory(Constants.PLUGIN_ID,
    AllIcons.Webreferences.WebSocket) {
}