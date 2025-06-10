package org.ideplugins.vale_cli_plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.SystemInfo;
import org.ideplugins.vale_cli_plugin.settings.ValeCliPluginConfigurationState;
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FeedbackActionHelper {
    final String ideVersion;
    final String pluginVersion;
    final String operatingSystem;
    final String jdkVersion;
    final String title;
    final String description;

    private static final String SYSTEM_INFO_TEMPLATE = """
            ## Version Information
            | Attribute                | Value |
            |--------------------------|-------|
            | **OS**                   |   %s  |
            | **IDE**                  |   %s  |
            | **JDK**                  |   %s  |
            | **Vale Plugin Version**  |   %s  |
            | **Vale Version**         |   %s  |
            
            /label ~bug-report
            """;

    private static final String FEATURE_REQUEST_TEMPLATE = """
            ## Feature request
            
            ## Description
            
            
            /label ~feature-request
            """;

    private static final String BUG_TEMPLATE = """
# Checklist
**Mandatory**
- [ ] Steps to reproduce
- [ ] Actual results
- [X] Version Information

_Optional but helpful_
- [ ] Screenshots / Screencast (review no sensitive information is displayed)
- [ ] Attach or paste relevant plugin logs (review no sensitive information is in them)

## Steps to reproduce
1. Step number 1

## Actual results
What does it happen?

## Screenshots or screencast (review no sensitive information is displayed)
Attach a screencast/screenshot to help reproduce the issue.

**Make sure the screenshot or screencast have no sensitive information.**

## Attach or paste relevant plugin logs (review no sensitive information is in them)
Get relevant logs of the plugin by running this grep command:

```bash
grep -i "org.ideplugins.vale_cli_plugin" ~/.cache/JetBrains/IdeaIC2023.3/log/idea.log
```

**Make sure these logs have no sensitive information.**

please
read [Directories used by the IDE to store settings caches plugins and logs](https://intellij-support.jetbrains.com/hc/en-us/articles/206544519-Directories-used-by-the-IDE-to-store-settings-caches-plugins-and-logs)
to find out the directory where to look for the log file according to your operating system.
            """;

    private static final String ISSUES_URL =
            "https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/new?issue[title]=%s&issue[description]=%s";


    public FeedbackActionHelper(AnActionEvent actionEvent){
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyy"));
        title = "Bug Report".equals(actionEvent.getPresentation().getText())?
                "Plugin Bug Report " + date : "Plugin Feature Request " + date ;
        ApplicationInfo applicationInfo = ApplicationInfo.getInstance();
        operatingSystem = SystemInfo.getOsNameAndVersion() + "-" + SystemInfo.OS_ARCH;
        ideVersion = String.join(" ", applicationInfo.getVersionName(),
                applicationInfo.getFullVersion(), applicationInfo.getBuild().asString());
        jdkVersion = String.join(" ", System.getProperty("java.vm.name"),
                SystemInfo.JAVA_VERSION, SystemInfo.JAVA_RUNTIME_VERSION, SystemInfo.JAVA_VENDOR);
        ValeCliPluginConfigurationState pluginSettings =
                ApplicationManager.getApplication().getService(ValeCliPluginConfigurationState.class);
        pluginVersion = pluginSettings.getLastVersion();
        description = "Bug Report".equals(actionEvent.getPresentation().getText())? BUG_TEMPLATE +
                SYSTEM_INFO_TEMPLATE.formatted(operatingSystem, ideVersion, jdkVersion, pluginVersion,
                        ValePluginSettingsState.getInstance().valeVersion) :
                FEATURE_REQUEST_TEMPLATE;
    }

    private String encode(final String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    public String getEncodedUrl() {
        return String.format(ISSUES_URL,encode(title), encode(description));
    }
}
