package org.ideplugins.plugin.service;

import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.serviceContainer.NonInjectable;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValeIssuesReporter {
    private final Project userProject;
    private Map<String, List<JsonObject>> issuesPerFile;

    @NonInjectable
    public ValeIssuesReporter(Project project) {
        this(project, new HashMap<>());
    }

    public ValeIssuesReporter(Project project, Map<String, List<JsonObject>> issues) {
        userProject = project;
        issuesPerFile = issues;
    }

    public void updateIssuesForFile(String filePath, List<JsonObject> issueList) {
        issuesPerFile.remove(filePath);
        issuesPerFile.put(filePath, issueList);
    }

    public void populateIssuesFromValeResponse(Map<String, List<JsonObject>> issues) {
        issuesPerFile.clear();
        this.issuesPerFile = issues;
    }

    public boolean hasIssuesForFile(String filePath) {
        return issuesPerFile.containsKey(filePath);
    }

    public List<JsonObject> getIssues(String filePath) {
        return issuesPerFile.get(filePath);
    }

    public String getTotalIssues() {
        Map<String, Integer> resultsPerSeverity = new HashMap<>();
        StringBuilder message = new StringBuilder("Vale found: \n");
        issuesPerFile.forEach((file, issues) -> issues.forEach(jsonObject -> {
            String severity = jsonObject.get("Severity").getAsString();
            resultsPerSeverity.merge(severity, 1, Integer::sum);
        }));
        resultsPerSeverity.forEach((key, value) -> {
            String line = MessageFormat.format("{0} {1}s ", value, key);
            message.append(line).append("\n");
        });

        String files = MessageFormat.format("In {0} files", issuesPerFile.size());
        message.append(files);
        return message.toString();
    }
}
