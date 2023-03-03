package org.ideplugins.plugin.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang.StringUtils;
import org.ideplugins.plugin.exception.ValeCliExecutionException;
import org.ideplugins.plugin.settings.ValePluginSettingsState;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.listener.ProcessListener;

import java.io.File;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


@Service
public final class ValeCliExecutor {

    private final Project project;

    private int numberOfFiles;

    private Boolean taskRunning = Boolean.FALSE;
    private Long executionTime = 0L;

    public ValeCliExecutor(Project theProject) {
        project = theProject;
    }

    public static ValeCliExecutor getInstance(Project aProject) {
        return aProject.getService(ValeCliExecutor.class);
    }

    public StartedProcess executeValeCliOnFile(PsiFile file) throws ValeCliExecutionException {
        String filePath = file.getVirtualFile().getPath();
        List<String> command = createValeCommand();
        command.add(filePath);
        return executeCommand(command);
    }

    public StartedProcess executeValeCliOnFiles(List<String> files) throws ValeCliExecutionException {
        List<String> command = createValeCommand();
        command.addAll(files);
        return executeCommand(command);
    }

    public StartedProcess executeValeCliOnProject() throws ValeCliExecutionException {
        List<String> command = createValeInProjectCommand();
        return executeCommand(command);
    }

    public Boolean isTaskRunning() {
        return taskRunning;
    }

    public Map<String, List<JsonObject>> parseValeJsonResponse(Future<ProcessResult> future, ProgressIndicator indicator)
            throws ValeCliExecutionException {

        while (!future.isDone()) {
            indicator.checkCanceled();
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException exception) {
                throw new ValeCliExecutionException(exception);
            }
        }
        return parseValeJsonResponse(future, numberOfFiles);
    }

    public Map<String, List<JsonObject>> parseValeJsonResponse(Future<ProcessResult> future, int numberOfFilesToCheck)
            throws ValeCliExecutionException {
        String valeJsonResponse;
        try {
            valeJsonResponse = future.get(numberOfFilesToCheck, TimeUnit.SECONDS).outputUTF8();
            return parseJsonResponse(valeJsonResponse);
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
        ProcessExecutor processExecutor = new ProcessExecutor()
                .directory(new File(project.getBasePath()))
                .command(command)
                .exitValueNormal()
                .listener(new ValeProcessListener())
                .destroyOnExit()
                .readOutput(true);
        try {
            return processExecutor.start();
        } catch (IOException exception) {
            throw new ValeCliExecutionException(exception);
        }
    }


    private List<String> createValeInProjectCommand() {
        ValePluginSettingsState settingsState = ValePluginSettingsState.getInstance();
        List<String> command = createValeCommand();
        command.add(String.format("--glob=*.{%s}", settingsState.extensions));
        command.add(project.getBasePath());
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

    public void setNumberOfFiles(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public long getExecutionTime() {
        return executionTime;
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
            executionTime = Duration.between(start,end).toSeconds();
        }
    }
}
