package org.ideplugins.vale_cli_plugin.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.io.FileUtil;
import org.ideplugins.vale_cli_plugin.annotator.ValeAction;
import org.ideplugins.vale_cli_plugin.annotator.ValeFixOutputParser;
import org.ideplugins.vale_cli_plugin.annotator.ValeProblem;
import org.ideplugins.vale_cli_plugin.annotator.ValeRuntimeError;
import org.ideplugins.vale_cli_plugin.settings.ValePluginProjectSettingsState;
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState;
import org.ideplugins.vale_cli_plugin.settings.ValeVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType.CONSOLE;
import static org.ideplugins.vale_cli_plugin.Constants.PLUGIN_BUNDLE;

import java.nio.file.Path;


@Service(Service.Level.PROJECT)
public final class ValeCliExecutor {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(PLUGIN_BUNDLE);
    private static final ValeVersion PATH_ARG_MIN_VERSION = ValeVersion.parse("3.13.2");
    private final Logger LOGGER = Logger.getInstance(ValeCliExecutor.class);
    private final Project project;
    private final ValePluginSettingsState settings;
    private final ValePluginProjectSettingsState projectSettings;
    private final ObjectMapper mapper = JsonMapper.builder()
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES,
                    MapperFeature.ACCEPT_CASE_INSENSITIVE_VALUES,
                    MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS
            )
            .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
            .configure(JsonReadFeature.ALLOW_MISSING_VALUES, true)
            .build();


    public ValeCliExecutor(Project theProject) {
        this(theProject, ValePluginSettingsState.getInstance(),ValePluginProjectSettingsState.getInstance(theProject));
    }

    public ValeCliExecutor(Project theProject,ValePluginSettingsState pluginSettings,ValePluginProjectSettingsState projectSettings  ){
        project = theProject;
        settings = pluginSettings;
        this.projectSettings = projectSettings;
    }

    public static ValeCliExecutor getInstance(@NotNull Project theProject) {
        return theProject.getService(ValeCliExecutor.class);
    }

    private @NotNull GeneralCommandLine buildLintStdInCommand(String extension, String path) {
        String configFilePath = projectSettings.getValeSettingsPath();
        Path baseDir = Objects.requireNonNull(ProjectUtil.guessProjectDir(project)).toNioPath();
        GeneralCommandLine command = new GeneralCommandLine().withExePath(settings.valePath)
                .withWorkingDirectory(baseDir);
        if (!configFilePath.isBlank()) {
            command = command.withParameters("--config=" + configFilePath);
        }
        Path inputPath = Path.of(Objects.requireNonNull(path, "path"));
        Path relativePath = (inputPath.isAbsolute() && inputPath.startsWith(baseDir))
                ? baseDir.relativize(inputPath)
                : inputPath;
        String posixPath = FileUtil.toSystemIndependentName(relativePath.toString());
        if (settings.valeVersion.isAtLeast(PATH_ARG_MIN_VERSION)) {
            command = command.withParameters("--no-exit", "--output=JSON", "--path=" + posixPath);
        } else {
            command = command.withParameters("--no-exit", "--output=JSON", "--ext=." + extension);
        }
        return command;
    }

    private GeneralCommandLine buildSyncCommand() {
        String configFilePath = projectSettings.getValeSettingsPath();
        Path baseDir = Path.of(Objects.requireNonNull(project.getBasePath()));
        GeneralCommandLine command = new GeneralCommandLine().withExePath(settings.valePath)
                .withWorkingDirectory(baseDir)
                .withParentEnvironmentType(CONSOLE);
        command = command.withParameters("sync", "--output=JSON");
        if (!configFilePath.isBlank()) {
            command = command.withParameters("--config=" + configFilePath);
        }
        return command;
    }

    private GeneralCommandLine buildFixCommand(String alertJson) {
        String configFilePath = projectSettings.getValeSettingsPath();
        Path baseDir = Objects.requireNonNull(ProjectUtil.guessProjectDir(project)).toNioPath();
        GeneralCommandLine command = new GeneralCommandLine().withExePath(settings.valePath)
                .withWorkingDirectory(baseDir);
        command = command.withParameters("fix");
        if (!configFilePath.isBlank()) {
            command = command.withParameters("--config=" + configFilePath);
        }
        command = command.withParameters(alertJson);
        return command;
    }

    private GeneralCommandLine buildLsConfigCommand(@Nullable Path configPath) {
        String configFilePath = configPath != null ? configPath.toString() : projectSettings.getValeSettingsPath();
        Path baseDir = Path.of(Objects.requireNonNull(project.getBasePath()));
        GeneralCommandLine command = new GeneralCommandLine().withExePath(settings.valePath)
                .withWorkingDirectory(baseDir)
                .withParentEnvironmentType(CONSOLE);
        command = command.withParameters("ls-config");
        if (configFilePath != null && !configFilePath.isBlank()) {
            command = command.withParameters("--config=" + configFilePath);
        }
        return command;
    }


    public ProcessOutput runSyncCommand(ProcessListener listener) throws ExecutionException {
        GeneralCommandLine sync = buildSyncCommand();
        LOGGER.info("Running vale sync command: " + sync.getCommandLineString());
        CapturingProcessHandler handler = new CapturingProcessHandler(sync);
        handler.addProcessListener(listener);
        return handler.runProcess();
    }

    public ProcessOutput runLsConfigCommand(@Nullable Path configPath) throws ExecutionException {
        GeneralCommandLine lsConfig = buildLsConfigCommand(configPath);
        LOGGER.info("Running vale ls-config command: " + lsConfig.getCommandLineString());
        CapturingProcessHandler handler = new CapturingProcessHandler(lsConfig);
        return handler.runProcess();
    }

    public ProcessOutput runLintStdinCommand(CharSequence stdin, String extension, String path) throws ExecutionException, IOException {
        GeneralCommandLine lint = buildLintStdInCommand(extension, path);
        LOGGER.debug("PATH: " + System.getenv("PATH"));
        LOGGER.info("Running vale lint stdin command: " + lint.getCommandLineString());

        Process process = lint.createProcess();
        try (Writer writer = new OutputStreamWriter(process.getOutputStream(), lint.getCharset())) {
            writer.append(stdin);
            writer.flush();
        }
        CapturingProcessHandler handler = new CapturingProcessHandler(process, lint.getCharset(), lint.getCommandLineString());
        return handler.runProcess();
    }

    public @NotNull List<String> runFix(@NotNull ValeProblem problem) {
        ValeAction action = problem.action();
        String actionName = action == null || action.name() == null ? "" : action.name();
        if (actionName.isBlank()) {
            return List.of();
        }
        return runFixOnProblem(problem);
    }

    private @NotNull List<String> runFixOnProblem(@NotNull ValeProblem problem) {
        String alertJson = buildFixAlertPayload(problem);
        if (alertJson.isBlank()) {
            return List.of();
        }
        GeneralCommandLine fix = buildFixCommand(alertJson);
        LOGGER.info("Running vale fix command: " + fix.getCommandLineString());
        try {
            CapturingProcessHandler handler = new CapturingProcessHandler(fix);
            ProcessOutput output = handler.runProcess();
            String stdout = output.getStdout();
            ValeFixOutputParser.FixResult result = ValeFixOutputParser.parse(stdout);
            if (!result.error().isBlank()) {
                LOGGER.warn("Vale fix returned error: " + result.error());
            }
            if (!output.getStderr().isBlank()) {
                LOGGER.warn("Vale fix diagnostics: " + output.getStderr());
            }
            return result.suggestions();
        } catch (Exception e) {
            LOGGER.warn("Failed to run vale fix", e);
            return List.of();
        }
    }

    private @NotNull String buildFixAlertPayload(@NotNull ValeProblem problem) {
        ValeAction action = problem.action();
        String actionName = action == null || action.name() == null ? "" : action.name();
        if (actionName.isBlank()) {
            return "";
        }
        Optional<List<String>> paramsOptional = action.parameters().isEmpty() ? Optional.empty() : action.parameters();
        List<String> params = paramsOptional.orElseGet(List::of);
        String match = problem.match();
        if (match != null && match.isBlank()) {
            match = null;
        }
        String check = problem.check();
        if (check != null && check.isBlank()) {
            check = null;
        }
        FixAction fixAction = new FixAction(actionName, List.copyOf(params));
        FixAlertPayload alert = new FixAlertPayload(fixAction, match, check);
        try {
            return mapper.writeValueAsString(alert);
        } catch (Exception e) {
            LOGGER.warn("Failed to build vale fix payload", e);
            return "";
        }
    }

    public @NotNull String checkConfiguration() {
        StringBuilder errors = new StringBuilder();
        File valeBinary = new File(settings.valePath);
        if (settings.valePath.isEmpty() || !valeBinary.exists() || !valeBinary.canExecute()) {
            errors.append(BUNDLE.getString("vale.cli.plugin.settings.invalidexe.message"));
            errors.append(":\t\t").append(settings.valePath);
            errors.append("\n");
        }
        if (projectSettings.getExtensions().isBlank()) {
            errors.append(BUNDLE.getString("vale.cli.plugin.project.settings.invalid_extensions.message"));
            errors.append("\n");
        }
        String configurationFile = projectSettings.getValeSettingsPath();
        if (!configurationFile.isEmpty()) {
            File f = new File(configurationFile);
            if (!f.exists()) {
                errors.append(BUNDLE.getString("vale.cli.plugin.project.settings.invalidfile.message"));
                errors.append(":\t").append(configurationFile);
                errors.append("\n");

            }
        }
        return errors.toString();
    }

    public @NotNull List<String> extensionsAsList() {
        return Arrays.asList(projectSettings.getExtensions().split(","));
    }


    public List<ValeProblem> parseSuccessProcessOutput(@NotNull ProcessOutput processOutput) {
        Map<String, List<ValeProblem>> result = mapper.readValue(processOutput.getStdout(), new TypeReference<>() {
        });
        Optional<Map.Entry<String, List<ValeProblem>>> results = result.entrySet().stream().findFirst();
        List<ValeProblem> alerts = results.map(Map.Entry::getValue).orElseGet(List::of);
        return alerts.stream().sorted(Comparator.comparingInt(ValeProblem::line)).toList();
    }

    public ValeRuntimeError parseErrorProcessOutput(ProcessOutput processOutput) {
        return mapper.readValue(processOutput.getStderr(), new TypeReference<>() {
        });
    }

    private record FixAlertPayload(
            @JsonProperty("Action") FixAction action,
            @JsonProperty("Match") String match,
            @JsonProperty("Check") String check) {
    }

    private record FixAction(
            @JsonProperty("Name") String name,
            @JsonProperty("Params") List<String> params) {
    }

}
