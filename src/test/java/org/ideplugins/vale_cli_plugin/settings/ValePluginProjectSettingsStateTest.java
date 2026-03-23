package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.components.impl.ProjectPathMacroManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.ideplugins.vale_cli_plugin.testing.PluginTest;
import org.ideplugins.vale_cli_plugin.testing.RunInEdtExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@PluginTest
@ExtendWith(RunInEdtExtension.class)
class ValePluginProjectSettingsStateTest {

    @Test
    void setValeSettingsPathNormalizesWindowsSeparators(CodeInsightTestFixture fixture) {
        ValePluginProjectSettingsState settings = fixture.getProject().getService(ValePluginProjectSettingsState.class);
        String windowsPath = "C:\\MyUser\\MyProject\\MyProjectRootDir\\.vale.ini";

        settings.setValeSettingsPath(windowsPath);

        assertNotNull(settings.getState());
        assertEquals("C:/MyUser/MyProject/MyProjectRootDir/.vale.ini", settings.getState().valeSettingsPath);
        assertEquals("C:/MyUser/MyProject/MyProjectRootDir/.vale.ini", settings.getValeSettingsPath());
    }

    @Test
    void setRootIniNormalizesWindowsSeparators(CodeInsightTestFixture fixture) {
        ValePluginProjectSettingsState settings = fixture.getProject().getService(ValePluginProjectSettingsState.class);
        String windowsPath = "C:\\MyUser\\MyProject\\MyProjectRootDir\\.vale.ini";

        settings.setRootIni(windowsPath);

        assertNotNull(settings.getState());
        assertEquals("C:/MyUser/MyProject/MyProjectRootDir/.vale.ini", settings.getState().rootIni);
        assertEquals("C:/MyUser/MyProject/MyProjectRootDir/.vale.ini", settings.getRootIni());
    }

    @Test
    void defaultPathMacroManagerCollapsesProjectPath(CodeInsightTestFixture fixture) {
        String projectPath = fixture.getProject().getBasePath();
        assertNotNull(projectPath);
        String configPath = FileUtil.toSystemIndependentName(Path.of(projectPath, ".vale.ini").toString());
        PathMacroManager pathMacroManager = PathMacroManager.getInstance(fixture.getProject());

        assertEquals("$PROJECT_DIR$/.vale.ini", pathMacroManager.collapsePath(configPath));
    }

    @Test
    void defaultProjectPathMacroManagerCollapsesOutsideWindowsPathAndExpandsBack() {
        String projectBasePath = "C:/MyUser/MyProjects/MyProjectRootDir";
        String absoluteWindowsPath = "C:\\MyUser\\Desktop\\.vale.ini";
        PathMacroManager macroManager = ProjectPathMacroManager.createInstance(
                () -> projectBasePath + "/project.ipr",
                () -> projectBasePath,
                () -> "MyProjectRootDir"
        );
        String normalizedWindowsPath = FileUtil.toSystemIndependentName(absoluteWindowsPath);
        String collapsedPath = macroManager.collapsePath(normalizedWindowsPath);

        assertEquals("$PROJECT_DIR$/../../Desktop/.vale.ini", collapsedPath);
        assertEquals(normalizedWindowsPath, macroManager.expandPath(collapsedPath));
    }
}
