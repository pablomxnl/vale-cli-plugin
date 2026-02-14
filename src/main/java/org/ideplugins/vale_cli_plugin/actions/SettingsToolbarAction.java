package org.ideplugins.vale_cli_plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsConfigurable;
import org.jetbrains.annotations.NotNull;

public class SettingsToolbarAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ApplicationManager.getApplication().invokeLater(() ->
                ShowSettingsUtil.getInstance()
                        .showSettingsDialog(e.getProject(), ValePluginSettingsConfigurable.class));
    }


}

