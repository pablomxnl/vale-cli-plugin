package org.ideplugins.vale_cli_plugin.languageserver

import com.intellij.openapi.application.PathManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists


class ValeLsDownloaderTest {

    @ParameterizedTest
    @Disabled
    @CsvSource(
        "linux, x86_64",
        "windows, x86_64",
        "linux, aarch64",
        "windows, aarch64",
        "macos, x86_64",
        "macos, aarch64"
    )
    fun testDownloadAndExtractValeLs( operatingSystem: String, cpuArch: String){
        val tempPath = Path.of(PathManager.getTempPath())
        createDirectoriesIfNeeded(tempPath,tempPath.resolve(operatingSystem),
            tempPath.resolve(operatingSystem).resolve(cpuArch))

        val osPath = tempPath.resolve(operatingSystem).resolve(cpuArch)

        val binaryPath =
            if ("windows" == operatingSystem)
                osPath.resolve("languageserver").resolve("vale-ls.exe")
            else osPath.resolve("languageserver").resolve("vale-ls")
        ValeLsDownloader.downloadAndExtractLanguageServer(operatingSystem, cpuArch, osPath, binaryPath)
        assertTrue(Files.exists(binaryPath), "Binary for language server should have been downloaded and extracted")
    }

    private fun createDirectoriesIfNeeded(vararg paths : Path){
        paths.forEach {
            if (!Files.exists(it)){
                Files.createDirectory(it)
            }
        }
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            val path = Path.of(PathManager.getTempPath())
            deleteRecursively(path.resolve("linux"),
                path.resolve("windows"),
                path.resolve("macos"))
        }

        private fun deleteRecursively(vararg pathList : Path) {
            pathList.forEach {
                path ->
                run {
                    if (path.exists())
                    Files.walk(path).use { paths ->
                        paths.sorted(Comparator.reverseOrder())
                            .map { obj: Path -> obj.toFile() }
                            .forEach(File::delete)
                    }
                }
            }
        }
    }


}
