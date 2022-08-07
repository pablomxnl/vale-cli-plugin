package org.ideplugins.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

import static org.ideplugins.plugin.actions.ActionHelper.areSettingsValid;
import static org.ideplugins.plugin.actions.ActionHelper.showResultsInConsole;

public class ValeToolsMenuAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        if (areSettingsValid(actionEvent)){
            FileDocumentManager.getInstance().saveAllDocuments();
            ApplicationManager.getApplication().invokeLater(() -> {
                String output = ValeCliExecutor.getInstance(actionEvent.getProject()).executeValeCliOnProject();
                showResultsInConsole(actionEvent.getProject(), output);
            });
        }
    }

}
