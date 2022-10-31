package org.ideplugins.plugin.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang.StringUtils;
import org.ideplugins.plugin.settings.ValePluginSettingsState;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.intellij.execution.ui.ConsoleViewContentType.LOG_ERROR_OUTPUT;
import static org.ideplugins.plugin.actions.ActionHelper.writeTextToConsole;

public class ValeCliExecutor implements ValeCli {

    private static ValeCliExecutor INSTANCE;
    private ValePluginSettingsState settingsState;
    private Project project;

    private ValeCliExecutor(Project project) {
        settingsState = ValePluginSettingsState.getInstance();
        this.project = project;
    }

    public static ValeCliExecutor getInstance(Project project) {
        if (INSTANCE == null) {
            INSTANCE = new ValeCliExecutor(project);
        }
        return INSTANCE;
    }

    @Override
    public String executeValeCliOnFile(PsiFile file) {
        try {
            String filePath = file.getVirtualFile().getPath();
            List<String> command = createValeCommand();
            command.add(filePath);
            return executeCommand(command, 10);
        } catch (IOException | TimeoutException | InterruptedException | ExecutionException exception) {
            handleError(exception);
        }
        return "";
    }

    @Override
    public String executeValeCliOnFiles(List<String> files) {
        try {
            List<String> command = createValeCommand();
            command.addAll(files);
            return executeCommand(command, 10);
        } catch (IOException | TimeoutException | InterruptedException | ExecutionException exception) {
            handleError(exception);
        }
        return "";
    }

    @Override
    public String executeValeCliOnProject() {
        try {
            List<String> command = createValeInProjectCommand();
            return executeCommand(command, 60);
        } catch (IOException | TimeoutException | InterruptedException | ExecutionException exception) {
            handleError(exception);
        }
        return "";
    }

    private String executeCommand(List<String> command, int timeout)
            throws IOException, ExecutionException, InterruptedException, TimeoutException {
        String projectPath =
                Arrays.stream(ProjectRootManager.getInstance(project).getContentRoots()).findFirst().get().getPath();
        Future<ProcessResult> future = new ProcessExecutor().directory(new File(projectPath))
                .command(command)
                .exitValueNormal()
                .readOutput(true)
                .start().getFuture();
        return future.get(timeout, TimeUnit.SECONDS).outputUTF8();
    }


    private List<String> createValeInProjectCommand() {
        List<String> command = createValeCommand();
        command.add(String.format("--glob=*.{%s}", settingsState.extensions));
        command.add(project.getBasePath());
        return command;
    }

    private List<String> createValeCommand() {
        List<String> command = new ArrayList<>();
        command.add(settingsState.valePath);
        if (StringUtils.isNotBlank(settingsState.valeSettingsPath)) {
            command.add("--config");
            command.add(settingsState.valeSettingsPath);
        }
        command.add("--no-exit");
        command.add("--no-wrap");
        command.add("--output=JSON");
        return command;
    }

    private void handleError(Exception exception) {
        writeTextToConsole(project, "There was an error executing vale \n" +
                        "Please check paths for vale cli binary on Settings -> Tools -> Vale CLI\nError output:\n\t" +
                        exception.getMessage(),
                LOG_ERROR_OUTPUT);
    }

}
