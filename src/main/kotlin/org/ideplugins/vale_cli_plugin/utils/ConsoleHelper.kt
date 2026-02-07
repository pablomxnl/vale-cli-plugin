package org.ideplugins.vale_cli_plugin.utils

import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import org.ideplugins.vale_cli_plugin.toolwindow.ValeToolWindow

class ConsoleHelper {
    companion object {
        @JvmStatic fun writeTextToConsole(project: Project, text: String, level: ConsoleViewContentType) {
            val consoleView = getConsoleView(project)
            consoleView?.let { console ->
                console.clear()
                console.print(text, level)
                ToolWindowManager.getInstance(project).getToolWindow("Vale CLI")?.show(null)
            }
        }

        @JvmStatic fun clearConsole(project: Project) {
            val consoleView = getConsoleView(project)
            consoleView?.clear()
        }

        private fun getConsoleView(project: Project): ConsoleView? {
            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Vale CLI") ?: return null
            val contentManager = toolWindow.contentManager

            val content = contentManager.findContent("Vale Results") ?: run {
                ValeToolWindow().createToolWindowContent(project, toolWindow)
                contentManager.findContent("Vale Results")
            }

            return content?.let { c ->
                val container = c.component as? java.awt.Container
                if (container != null && container.componentCount > 0) {
                    container.getComponent(0) as? ConsoleView
                } else null
            }
        }
    }
}