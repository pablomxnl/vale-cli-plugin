package org.ideplugins.vale_cli_plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import org.ideplugins.vale_cli_plugin.settings.ValePluginProjectSettingsConfigurable;
import org.jetbrains.annotations.NotNull;

public class ProjectSettingsToolbarAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtil.getInstance()
                .showSettingsDialog(e.getProject(), ValePluginProjectSettingsConfigurable.class);
    }

}

