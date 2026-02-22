package org.ideplugins.vale_cli_plugin.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.ideplugins.vale_cli_plugin.settings.ValePluginProjectSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

@Service(Service.Level.PROJECT)
public final class ValeLsConfigService {

    private static final Logger LOGGER = Logger.getInstance(ValeLsConfigService.class);
    private final Project project;
    private final ObjectMapper mapper = JsonMapper.builder()
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES,
                    MapperFeature.ACCEPT_CASE_INSENSITIVE_VALUES,
                    MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS
            )
            .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
            .configure(JsonReadFeature.ALLOW_MISSING_VALUES, true)
            .build();

    public ValeLsConfigService(Project project) {
        this.project = project;
    }

    public static @NotNull ValeLsConfigService getInstance(@NotNull Project project) {
        return project.getService(ValeLsConfigService.class);
    }

    public void requestConfigurationPaths(@NotNull Consumer<ValeConfigurationPaths> consumer) {
        if (project.isDisposed()) {
            return;
        }
        if (ApplicationManager.getApplication().isDispatchThread()) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                ValeConfigurationPaths paths = loadConfigurationPaths();
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (project.isDisposed()) {
                        return;
                    }
                    consumer.accept(paths);
                });
            });
            return;
        }
        consumer.accept(loadConfigurationPaths());
    }

    private @NotNull ValeConfigurationPaths loadConfigurationPaths() {
        Path configPath = configuredConfigPath();
        ValeCliExecutor cliExecutor = ValeCliExecutor.getInstance(project);
        try {
            ProcessOutput output = cliExecutor.runLsConfigCommand(configPath);
            if (output.getExitCode() != 0) {
                LOGGER.warn("Vale ls-config failed with exit code " + output.getExitCode()
                        + ": " + output.getStderr());
                return ValeConfigurationPaths.empty();
            }
            String stdout = output.getStdout();
            if (stdout.isBlank()) {
                LOGGER.warn("Vale ls-config returned empty output.");
                return ValeConfigurationPaths.empty();
            }
            LsConfigResponse response = mapper.readValue(stdout, LsConfigResponse.class);
            List<String> configFiles = response.configFiles == null ? List.of() : List.copyOf(response.configFiles);
            List<String> paths = response.paths == null ? List.of() : List.copyOf(response.paths);
            return new ValeConfigurationPaths(configFiles, paths);
        } catch (Exception e) {
            LOGGER.warn("Failed to run vale ls-config", e);
            return ValeConfigurationPaths.empty();
        }
    }

    private @Nullable Path configuredConfigPath() {
        ValePluginProjectSettingsState settings = ValePluginProjectSettingsState.getInstance(project);
        String configuredPath = settings.getValeSettingsPath();
        if (configuredPath != null && !configuredPath.isBlank()) {
            return Paths.get(configuredPath);
        }
        return null;
    }

    private static final class LsConfigResponse {
        @JsonProperty("ConfigFiles")
        private List<String> configFiles;
        @JsonProperty("Paths")
        private List<String> paths;
    }
}
