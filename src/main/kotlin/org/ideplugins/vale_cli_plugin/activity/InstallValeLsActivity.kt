package org.ideplugins.vale_cli_plugin.activity

import com.intellij.ide.util.RunOnceUtil
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.system.CpuArch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ideplugins.vale_cli_plugin.languageserver.ValeLsDownloader
import java.nio.file.Files
import java.nio.file.Path


class InstallValeLsActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        withContext(Dispatchers.IO) {
                RunOnceUtil.runOnceForApp("org.ideplugins.vale_cli_plugin.activity.InstallValeLsActivity"){
                    val pluginPath = Path.of(PathManager.getPluginsPath(), "vale-cli-plugin")
                    val binaryPath = if (SystemInfo.isWindows) pluginPath.resolve("languageserver").resolve("vale-ls.exe")
                    else pluginPath.resolve("languageserver").resolve("vale-ls")

                    ValeLsDownloader.downloadAndExtractLanguageServer(SystemInfo.getOsName().lowercase(),
                        CpuArch.CURRENT.name.lowercase(),pluginPath, binaryPath)
                }
        }
    }
}