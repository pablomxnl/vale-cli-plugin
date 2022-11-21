package org.ideplugins.plugin.actions;

import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import org.ideplugins.plugin.service.ValeCliExecutor;
import org.ideplugins.plugin.service.ValeIssuesReporter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.intellij.execution.ui.ConsoleViewContentType.LOG_INFO_OUTPUT;
import static org.ideplugins.plugin.actions.ActionHelper.*;

public class ValeToolsMenuAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        if (areSettingsValid(actionEvent)) {
            FileDocumentManager.getInstance().saveAllDocuments();
            ApplicationManager.getApplication().invokeLater(() -> {
                ValeIssuesReporter reporter =
                        Objects.requireNonNull(actionEvent.getProject()).getService(ValeIssuesReporter.class);
                Map<String, List<JsonObject>> results = new HashMap<>();
                try {
                    results = ValeCliExecutor.getInstance(actionEvent.getProject()).executeValeCliOnProject();
                    reporter.populateIssuesFromValeResponse(results);
                    String message = reporter.getTotalIssues();
                    writeTextToConsole(actionEvent.getProject(),
                            message + "\n" + "See the results in the problemview of a supported file",
                            LOG_INFO_OUTPUT);
                } catch (Exception exception) {
                    handleError(actionEvent.getProject(), exception);
                }
            });
        }
    }

}
