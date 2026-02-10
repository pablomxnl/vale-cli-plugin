package org.ideplugins.vale_cli_plugin.activity

import dev.j_a.ide.lsp.api.registry.RegisterLanguageServerSupportActivity
import org.ideplugins.vale_cli_plugin.languageserver.ValeLsSupport

class ValeLsRegisterLanguageServerActivity :
    RegisterLanguageServerSupportActivity(ValeLsSupport)