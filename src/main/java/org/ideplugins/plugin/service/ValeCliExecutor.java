package org.ideplugins.plugin.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang.StringUtils;
import org.ideplugins.plugin.settings.ValePluginSettingsState;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


@Service
public final class ValeCliExecutor {

    private final ValePluginSettingsState settingsState = ValePluginSettingsState.getInstance();
    private final Project project;

    public ValeCliExecutor(Project theProject) {
        project = theProject;
    }

    public static ValeCliExecutor getInstance(Project aProject) {
        return aProject.getService(ValeCliExecutor.class);
    }

    public Map<String, List<JsonObject>> executeValeCliOnFile(PsiFile file) throws Exception {
        String filePath = file.getVirtualFile().getPath();
        List<String> command = createValeCommand();
        command.add(filePath);
        String valeJsonResponse = executeCommand(command, 5);
        return parseValeJsonResponse(valeJsonResponse);
    }

    public Map<String, List<JsonObject>> executeValeCliOnFiles(List<String> files) throws Exception {
        List<String> command = createValeCommand();
        command.addAll(files);
        String valeJsonResponse = executeCommand(command, 10);
        return parseValeJsonResponse(valeJsonResponse);
    }

    public Map<String, List<JsonObject>> executeValeCliOnProject() throws Exception {
        List<String> command = createValeInProjectCommand();
        String valeJsonResponse = executeCommand(command, 30);
        return parseValeJsonResponse(valeJsonResponse);
    }

    private Map<String, List<JsonObject>> parseValeJsonResponse(String valeJsonResponse) {
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

    private String executeCommand(List<String> command, int timeout) throws Exception {
        String projectPath =
                Arrays.stream(ProjectRootManager.getInstance(project).getContentRoots())
                        .findFirst().get().getPath();
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


}
