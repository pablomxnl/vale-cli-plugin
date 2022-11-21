package org.ideplugins.plugin.actions;

import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.ideplugins.plugin.service.ValeCliExecutor;
import org.ideplugins.plugin.service.ValeIssuesReporter;
import org.ideplugins.plugin.settings.ValePluginSettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.ideplugins.plugin.actions.ActionHelper.areSettingsValid;
import static org.ideplugins.plugin.actions.ActionHelper.handleError;

public class ValePopupAction extends AnAction {

    private ValeCliExecutor cliExecutor;

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        if (areSettingsValid(actionEvent)) {
            PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
            cliExecutor = ValeCliExecutor.getInstance(Objects.requireNonNull(actionEvent.getProject()));
            FileDocumentManager.getInstance().saveAllDocuments();

            ApplicationManager.getApplication().invokeLater(() -> {
                ValeIssuesReporter reporter = actionEvent.getProject().getService(ValeIssuesReporter.class);
                try {
                    if (psiFile != null) {
                        Map<String, List<JsonObject>> results = cliExecutor.executeValeCliOnFile(psiFile);
                        reporter.updateIssuesForFile(psiFile.getVirtualFile().getPath(),
                                results.get(psiFile.getVirtualFile().getPath()));
                    } else {
                        VirtualFile[] virtualFiles = actionEvent.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
                        if (virtualFiles != null && virtualFiles.length > 1) {
                            List<String> filesToCheck = Arrays.stream(virtualFiles).map(VirtualFile::getPath)
                                    .collect(Collectors.toList());
                            Map<String, List<JsonObject>> results = cliExecutor.executeValeCliOnFiles(filesToCheck);
                            for (String file : filesToCheck) {
                                reporter.updateIssuesForFile(file, results.get(file));
                            }
                        }
                    }
                } catch (Exception exception) {
                    handleError(actionEvent.getProject(), exception);
                }
            });
        }
    }


    @Override
    public void update(@NotNull AnActionEvent actionEvent) {
        ValePluginSettingsState settings = ValePluginSettingsState.getInstance();
        List<String> extensions = Arrays.stream(settings.extensions.split(",")).collect(Collectors.toList());
        AtomicReference<Boolean> shouldBeEnabled = new AtomicReference<>();
        shouldBeEnabled.set(false);
        PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
        VirtualFile[] files = actionEvent.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (psiFile != null) {
            String fileName = psiFile.getName();
            extensions.forEach(extension -> shouldBeEnabled.set(shouldBeEnabled.get() || fileName.toLowerCase().endsWith(extension)));
        } else if (files != null) {
            boolean allFiles = true;
            for (VirtualFile file : files) {
                allFiles = extensions.contains(file.getExtension()) && allFiles;
            }
            shouldBeEnabled.set(allFiles);
        }
        actionEvent.getPresentation().setEnabledAndVisible(shouldBeEnabled.get());
    }

}
