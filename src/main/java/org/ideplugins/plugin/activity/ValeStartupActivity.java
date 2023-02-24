package org.ideplugins.plugin.activity;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang.StringUtils;
import org.ideplugins.plugin.service.ValeCliExecutor;
import org.ideplugins.plugin.settings.ValePluginSettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;


public class ValeStartupActivity implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> getValeFilesCount(project));
    }


    private static void getValeFilesCount(Project project) {
        if (project.isDisposed()){
            return;
        }
        ValePluginSettingsState settingsState = ValePluginSettingsState.getInstance();

        if (StringUtils.isNotBlank(settingsState.extensions)) {
            ValeCliExecutor executor = project.getService(ValeCliExecutor.class);
            GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
            ApplicationManager.getApplication().runReadAction(() -> {
                int numberOfFiles = Arrays.stream(settingsState.extensions.split(",")).map(extension ->
                        FilenameIndex.getAllFilesByExt(project, extension, scope).size()).reduce(Integer::sum).orElse(0);
                executor.setNumberOfFiles(numberOfFiles);
            });
        }
    }
}

