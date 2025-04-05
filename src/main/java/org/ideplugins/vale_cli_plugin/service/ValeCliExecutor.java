package org.ideplugins.vale_cli_plugin.service;

import com.google.gson.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang3.StringUtils;
import org.ideplugins.vale_cli_plugin.exception.ValeCliExecutionException;
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.listener.ProcessListener;

import java.io.File;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import java.util.*;
import java.util.concurrent.*;

import static org.ideplugins.vale_cli_plugin.settings.OSUtils.wrappCommandWithShellEnv;


@Service(Service.Level.PROJECT)
public final class ValeCliExecutor implements Disposable {

    private final Logger LOGGER = Logger.getInstance(ValeCliExecutor.class);

    private final Project project;

    private Boolean taskRunning = Boolean.FALSE;
    private Long executionTime = 0L;

    public ValeCliExecutor(Project theProject) {
        project = theProject;
    }

    public static ValeCliExecutor getInstance(Project aProject) {
        return aProject.getService(ValeCliExecutor.class);
    }

    public StartedProcess executeValeCliOnFile(PsiFile file) throws ValeCliExecutionException {
        LOGGER.info("Running executeValeCliOnFile on PSI file ");
        return executeValeCliOnFile(file.getVirtualFile());
    }

    public StartedProcess executeValeCliOnFile(VirtualFile file) throws ValeCliExecutionException {
        List<String> command = createValeCommand();
        command.add(file.getPath());
        LOGGER.info("Running executeValeCliOnFile on VirtualFile file " + file.getPath());
        List<String> wrapped = wrappCommandWithShellEnv(String.join(" ", command));
        return executeCommand(wrapped);
    }

    public StartedProcess executeValeCliOnFiles(List<String> files) throws ValeCliExecutionException {
        List<String> command = createValeCommand();
        command.addAll(files);
        List<String> wrapped = wrappCommandWithShellEnv(String.join(" ", command));
        LOGGER.info("Running executeValeCliOnFiles on files " + files);
        return executeCommand(wrapped);
    }

    public StartedProcess executeValeCliOnPath(String directory) throws ValeCliExecutionException {
        LOGGER.info("Running executeValeCliOnPath  " + directory);
        List<String> command = createValeInPathCommand(directory);
        List<String> wrapped = wrappCommandWithShellEnv(String.join(" ", command));
        return executeCommand(wrapped);
    }

    public Boolean isTaskRunning() {
        return taskRunning;
    }

    public Map<String, List<JsonObject>> parseValeJsonResponse(Future<ProcessResult> future,
                                                               ProgressIndicator indicator,
                                                               int filesNumber)
            throws ValeCliExecutionException {

        while (!future.isDone()) {
            indicator.checkCanceled();
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException exception) {
                throw new ValeCliExecutionException(exception);
            }
        }
        return parseValeJsonResponse(future, filesNumber);
    }

    public Map<String, List<JsonObject>> parseValeJsonResponse(Future<ProcessResult> processResultFuture,
                                                               int numberOfFilesToCheck)
            throws ValeCliExecutionException {
        String valeJsonResponse = null;
        try {
            valeJsonResponse = processResultFuture.get(numberOfFilesToCheck*5L, TimeUnit.SECONDS).outputUTF8();
            LOGGER.debug(String.format("exec result: %s", valeJsonResponse));
            return parseJsonResponse(valeJsonResponse);
        } catch (JsonSyntaxException exception){
            String message = "Shell vale command response has invalid json, most likely shell configuration issue\n" +
                    exception.getMessage();
            if (valeJsonResponse!=null) {
                message+= "\nInvalid JSON response\n: " + valeJsonResponse;
            }
            throw new ValeCliExecutionException(message, exception);
        } catch (InterruptedException | ExecutionException | TimeoutException exception) {
            throw new ValeCliExecutionException(exception);
        }
    }

    private Map<String, List<JsonObject>> parseJsonResponse(String valeJsonResponse) {
        Map<String, List<JsonObject>> issuesPerFile = new HashMap<>();
        JsonElement element = JsonParser.parseString(valeJsonResponse);
        if (element.isJsonObject()) {
            JsonObject resultsPerFile = element.getAsJsonObject();
            resultsPerFile.keySet().forEach(filePath -> {
                List<JsonObject> issueList = new ArrayList<>();
                JsonArray issues = resultsPerFile.getAsJsonArray(filePath);
                issues.forEach(jsonElement -> issueList.add(jsonElement.getAsJsonObject()));
                issuesPerFile.put(filePath, issueList);
            });
        }
        return issuesPerFile;
    }

    private StartedProcess executeCommand(List<String> command) throws ValeCliExecutionException {
        LOGGER.info("Executing vale command: " + String.join(" ", command));
        LOGGER.info("In directory: " + project.getBasePath());
        ProcessExecutor processExecutor = new ProcessExecutor()
                .directory(new File(project.getBasePath()))
                .command(command)
                .exitValueNormal()
                .environment(System.getenv())
                .listener(new ValeProcessListener())
                .destroyOnExit()
                .readOutput(true);
        try {
            return processExecutor.start();
        } catch (IOException exception) {
            throw new ValeCliExecutionException(exception);
        }
    }

    private List<String> createValeInPathCommand(final String path) {
        ValePluginSettingsState settingsState = ValePluginSettingsState.getInstance();
        List<String> command = createValeCommand();
        command.add(String.format("--glob=\"*.{%s}\"", settingsState.extensions));
        command.add(path);
        return command;
    }

    private List<String> createValeCommand() {
        ValePluginSettingsState settingsState = ValePluginSettingsState.getInstance();
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

    public long getExecutionTime() {
        return executionTime;
    }

    @Override
    public void dispose() {

    }

    private class ValeProcessListener extends ProcessListener {

        private Instant start;

        @Override
        public void afterStart(Process process, ProcessExecutor executor) {
            start = Instant.now();
            taskRunning = true;
        }

        @Override
        public void afterStop(Process process) {
            Instant end = Instant.now();
            taskRunning = false;
            executionTime = Duration.between(start, end).toSeconds();
        }
    }
}
