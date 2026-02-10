package org.ideplugins.vale_cli_plugin.languageserver

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.io.ZipUtil
import okhttp3.*
import org.ideplugins.vale_cli_plugin.settings.ValeCliPluginConfigurationState
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions

class ValeLsDownloader {

    companion object {
        private val LOGGER: Logger = Logger.getInstance(ValeLsDownloader::class.java)

        private fun downloadBinaryFile(url: String, outputFile: File) {
            val client = OkHttpClient()

            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Failed to download file: ${response.code}")
                }

                val body = response.body ?: throw IOException("Response body is null")

                body.byteStream().use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }


        fun downloadAndExtractLanguageServer(os: String, cpuArch: String, pluginPath: Path, binaryPath:Path){
            val baseUrl = "https://github.com/errata-ai/vale-ls/releases/latest/download"
            val winPostFix = if (cpuArch == "x86_64") "gnu" else "msvc"
            val osFile = when(os){
                "macos" -> "apple-darwin"
                "linux" -> "unknown-linux-gnu"
                "windows" -> "pc-windows-$winPostFix"
                else -> ""
            }
            val url = "$baseUrl/vale-ls-$cpuArch-$osFile.zip"
            val outputFile = downloadFile(pluginPath, binaryPath, url)
            LOGGER.info("extracting zip to: ${binaryPath.parent}")
            val pluginSettings = ApplicationManager.getApplication().getService(
                ValeCliPluginConfigurationState::class.java
            )
            ZipUtil.extract(pluginPath.resolve(outputFile), binaryPath.parent, null)
            chmodExecutable(binaryPath)
            pluginSettings.valeLsPath = binaryPath.toString()
        }

        private fun chmodExecutable(binaryPath: Path) {
            if (!SystemInfo.isWindows) {
                val perms = PosixFilePermissions.fromString("rwx------")
                LOGGER.info("Changing permissions for vale-ls executable $binaryPath ")
                Files.setPosixFilePermissions(binaryPath, perms)
            }
        }

        private fun downloadFile(
            pluginPath: Path,
            binaryPath: Path,
            url: String
        ): String {
            val outputFile = "languageserver.zip"
            LOGGER.info("plugin path: $pluginPath, binaryPath: $binaryPath")
            LOGGER.info("downloading $url file to: ${pluginPath.resolve(outputFile)}")
            downloadBinaryFile(url, pluginPath.resolve(outputFile).toFile())
            return outputFile
        }
    }


}