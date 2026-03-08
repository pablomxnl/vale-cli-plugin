package org.ideplugins.vale_cli_plugin.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.options.ShowSettingsUtil;
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsConfigurable;
import org.jetbrains.annotations.NotNull;

public class SettingsToolbarAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var application = ApplicationManager.getApplication();
        if (application.isHeadlessEnvironment()) {
            return;
        }
        application.invokeLater(() ->
                        ShowSettingsUtil.getInstance()
                                .showSettingsDialog(e.getProject(), ValePluginSettingsConfigurable.class),
                ModalityState.defaultModalityState());
    }


}
