package org.ideplugins.plugin.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang.StringUtils;
import org.ideplugins.plugin.settings.ValePluginSettingsState;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.listener.ProcessListener;

import java.io.File;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Service
public final class ValeCliExecutor {

    private final ValePluginSettingsState settingsState = ValePluginSettingsState.getInstance();
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

    public StartedProcess executeValeCliOnFile(PsiFile file) throws Exception {
        String filePath = file.getVirtualFile().getPath();
        List<String> command = createValeCommand();
        command.add(filePath);
        return executeCommand(command);
    }

    public StartedProcess executeValeCliOnFiles(List<String> files) throws Exception {
        List<String> command = createValeCommand();
        command.addAll(files);
        return executeCommand(command);
    }

    public StartedProcess executeValeCliOnProject() throws IOException {
        List<String> command = createValeInProjectCommand();
        return executeCommand(command);
    }

    public Boolean isTaskRunning() {
        return taskRunning;
    }

    public Map<String, List<JsonObject>> parseValeJsonResponse(Future<ProcessResult> future, ProgressIndicator indicator)
            throws InterruptedException, ExecutionException, TimeoutException {
//        Instant expectedEnd = Instant.now().plusSeconds(numberOfFiles);

        while (!future.isDone()) {
            indicator.checkCanceled();
//            Instant now = Instant.now();
//            Duration duration = Duration.between(now, expectedEnd);
//            double fraction = 1 - (float) duration.toSeconds()/numberOfFiles;
//            System.out.println(String.format("Fraction: %s", fraction) );
//            indicator.setFraction(fraction*2);
            TimeUnit.MILLISECONDS.sleep(200);
        }
        return parseValeJsonResponse(future, numberOfFiles);
    }

    public Map<String, List<JsonObject>> parseValeJsonResponse(Future<ProcessResult> future, int numberOfFilesToCheck)
            throws ExecutionException, InterruptedException, TimeoutException {
        String valeJsonResponse = future.get(numberOfFilesToCheck, TimeUnit.SECONDS).outputUTF8();
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

    private StartedProcess executeCommand(List<String> command) throws IOException {
        String projectPath =
                Arrays.stream(ProjectRootManager.getInstance(project).getContentRoots())
                        .findFirst().get().getPath();
        ProcessExecutor processExecutor = new ProcessExecutor().directory(new File(projectPath))
                .command(command)
                .exitValueNormal()
                .listener(new ValeProcessListener())
                .destroyOnExit()
                .readOutput(true);
        return processExecutor.start();
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
