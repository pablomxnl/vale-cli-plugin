package org.ideplugins.vale_cli_plugin.service;

import com.google.gson.JsonObject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.ideplugins.vale_cli_plugin.settings.OSUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(Service.Level.PROJECT)
public final class ValeIssuesReporter implements Disposable {

    private Map<String, List<JsonObject>> issuesPerFile;

    public ValeIssuesReporter(Project project) {
        this(new HashMap<>());
    }

    public ValeIssuesReporter(Map<String, List<JsonObject>> issues) {
        issuesPerFile = issues;
    }

    public void updateIssuesForFile(final String filePath, List<JsonObject> issueList) {
        issuesPerFile.put(OSUtils.normalizeFilePath(filePath), issueList);
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

    public int getTotalFiles(){
        return issuesPerFile.size();
    }

    @Override
    public void dispose() {
        issuesPerFile.clear();
    }

    public void remove(String path) {
        issuesPerFile.remove(path);
    }

}
