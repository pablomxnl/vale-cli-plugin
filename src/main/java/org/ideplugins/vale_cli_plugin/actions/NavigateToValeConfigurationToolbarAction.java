package org.ideplugins.vale_cli_plugin.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.ideplugins.vale_cli_plugin.settings.ValePluginProjectSettingsState;
import org.jetbrains.annotations.NotNull;

public class NavigateToValeConfigurationToolbarAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var application = ApplicationManager.getApplication();
        Project project = e.getProject();
        if (application.isHeadlessEnvironment() && !application.isUnitTestMode() || project == null) {
            return;
        }

        doActionPerformed(
                project,
                application,
                ValePluginProjectSettingsState.getInstance(project),
                LocalFileSystem.getInstance(),
                FileEditorManager.getInstance(project)
        );
    }

    void doActionPerformed(Project project, com.intellij.openapi.application.Application application, ValePluginProjectSettingsState projectSettings, LocalFileSystem localFileSystem, FileEditorManager fileEditorManager) {
        String path = projectSettings.getValeSettingsPath();
        String rootIni = projectSettings.getRootIni();
        String configPath = path.isEmpty() ? (rootIni == null || rootIni.isEmpty() ? "" : rootIni) : path;
        
        if (!configPath.isEmpty()) {
            VirtualFile virtualFile = localFileSystem.refreshAndFindFileByPath(configPath);
            if (virtualFile != null) {
                application.invokeLater(() ->
                        fileEditorManager.openFile(virtualFile, true));
            }
        }
    }
}
