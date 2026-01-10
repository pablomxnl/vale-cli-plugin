package org.ideplugins.vale_cli_plugin.service;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import org.ideplugins.vale_cli_plugin.settings.ValePluginProjectSettingsState;
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;
import java.util.ResourceBundle;


@Service(Service.Level.PROJECT)
public final class ValeCliExecutor implements Disposable {

    private final Logger LOGGER = Logger.getInstance(ValeCliExecutor.class);
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("ValePlugin");
    private final Project project;
    private final ValePluginSettingsState settings;
    private final ValePluginProjectSettingsState projectSettings;

    public ValeCliExecutor(Project theProject) {
        project = theProject;
        settings = ValePluginSettingsState.getInstance();
        projectSettings = ValePluginProjectSettingsState.getInstance(project);
    }

    public static ValeCliExecutor getInstance(@NotNull Project theProject) {
        return theProject.getService(ValeCliExecutor.class);
    }

    private @NotNull GeneralCommandLine buildLintStdInCommand(String binaryPath, String configFilePath, String extension) {
        GeneralCommandLine command = new GeneralCommandLine().withExePath(binaryPath)
                .withWorkingDirectory(Objects.requireNonNull(ProjectUtil.guessProjectDir(project)).toNioPath());
        if (configFilePath != null && !configFilePath.isBlank()) {
            command = command.withParameters("--config=" + configFilePath);
        }
        command = command.withParameters("--no-exit", "--no-wrap", "--output=JSON", "--ext=" + extension);
        return command;
    }

    private GeneralCommandLine buildSyncCommand(String binaryPath, String configFilePath) {
        GeneralCommandLine command = new GeneralCommandLine().withExePath(binaryPath)
                .withWorkingDirectory(Objects.requireNonNull(ProjectUtil.guessProjectDir(project)).toNioPath());
        command = command.withParameters("sync", "--output=JSON");
        if (configFilePath != null && !configFilePath.isBlank()) {
            command = command.withParameters("--config=" + configFilePath);
        }
        return command;
    }


    @Override
    public void dispose() {

    }

    public void runSyncCommand(ProcessListener listener) throws ExecutionException {
        GeneralCommandLine sync = buildSyncCommand(settings.valePath, projectSettings.getValeSettingsPath());
        LOGGER.info("Running vale sync command: " + sync.getCommandLineString());
        CapturingProcessHandler handler = new CapturingProcessHandler(sync);
        handler.addProcessListener(listener);
        handler.startNotify();
    }

    public String checkConfiguration(){
        StringBuilder errors = new StringBuilder();
        File valeBinary = new File(settings.valePath);
        if ( settings.valePath.isEmpty() || !valeBinary.exists() || !valeBinary.canExecute()){
            errors.append(BUNDLE.getString("vale.cli.plugin.settings.invalidexe.message"));
            errors.append(":\t\t").append(settings.valePath);
            errors.append("\n");
        }
        if ( projectSettings.getExtensions().isBlank()){
            errors.append(BUNDLE.getString("vale.cli.plugin.project.settings.invalid_extensions.message"));
            errors.append("\n");
        }
        String configurationFile = projectSettings.getValeSettingsPath();
        if (!configurationFile.isEmpty()){
            File f = new File(configurationFile);
            if (!f.exists()){
                errors.append(BUNDLE.getString("vale.cli.plugin.project.settings.invalidfile.message"));
                errors.append(":\t").append(configurationFile);
                errors.append("\n");

            }
        }
        return errors.toString();
    }


}
