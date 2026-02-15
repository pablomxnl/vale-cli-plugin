package org.ideplugins.vale_cli_plugin.service;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.CapturingProcessAdapter;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.ideplugins.vale_cli_plugin.BaseTest;
import org.ideplugins.vale_cli_plugin.settings.ValePluginProjectSettingsState;
import org.ideplugins.vale_cli_plugin.testing.PluginTest;
import org.ideplugins.vale_cli_plugin.testing.RunInEdtExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.extension.ExtendWith;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@PluginTest
@ExtendWith(RunInEdtExtension.class)
public class ValeCliExecutorTest extends BaseTest {


    private final String markDownContent = """
        ## This is a test
        This text is being written in passive voice.
        """;

    @Test
    @DisabledIf("isGithubReachableForStylesSync")
    public void testRunSyncCommand(CodeInsightTestFixture codeInsightTestFixture) throws Exception {
        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        ProcessOutput output = executor.runSyncCommand(new CapturingProcessAdapter(){

        });
        assertEquals(0, output.getExitCode(), "sync command should exit 0");
    }

    static boolean isGithubReachableForStylesSync(){
        try (HttpClient client = HttpClient.newHttpClient()){

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://github.com/errata-ai/Google/releases.atom"))
                    .HEAD()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() != 200;
        } catch (IOException | URISyntaxException | InterruptedException e) {
            return true;
        }
    }

    @Test
    public void testRunSyncCommandAndInvalidConfig(CodeInsightTestFixture codeInsightTestFixture) throws Exception {
        ValePluginProjectSettingsState pluginProjectSettingsState = codeInsightTestFixture.getProject().getService(ValePluginProjectSettingsState.class);
        pluginProjectSettingsState.setValeSettingsPath(BaseTest.testProjectPath + "/.valetest.ini");
        ValeCliExecutor executor = new ValeCliExecutor(codeInsightTestFixture.getProject(), this.settings, pluginProjectSettingsState);
        ProcessOutput output = executor.runSyncCommand(new CapturingProcessAdapter(){});
        assertNotEquals(0, output.getExitCode(), "sync command should exit 0");
    }

    @Test
    public void testRunLintStdinCommand(CodeInsightTestFixture codeInsightTestFixture) throws Exception {
        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        String markDownContent  = """
        ## This is a test
        This text is being written in passive voice.
        """;
        ProcessOutput output = executor.runLintStdinCommand(markDownContent, "md", "dummy.md");
        assertEquals(0, output.getExitCode(), "lint stdin command should exit 0");
    }

    @Test
    public void testRunLintStdinCommandInvalidExePath(CodeInsightTestFixture codeInsightTestFixture){
        this.settings.valePath = "/tmp/path/doesnot/exist";
        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());

        assertThrows(ExecutionException.class, () -> {
            ProcessOutput output = executor.runLintStdinCommand(markDownContent, "md", "dummy.md");
            assertNotEquals(0, output.getExitCode(), "lint stdin command should exit 0");
        });

    }


    @Test
    public void testRunLintCommandOnFileAndWithCustomConfig(CodeInsightTestFixture codeInsightTestFixture) throws Exception {
        ValePluginProjectSettingsState pluginProjectSettingsState = codeInsightTestFixture.getProject().getService(ValePluginProjectSettingsState.class);
        pluginProjectSettingsState.getState().valeSettingsPath = BaseTest.testProjectPath + "/.vale.ini";
        ValeCliExecutor executor = new ValeCliExecutor(codeInsightTestFixture.getProject(), this.settings, pluginProjectSettingsState);
        ProcessOutput output = executor.runLintStdinCommand(markDownContent, "md", "dummy.md");
        assertEquals(0, output.getExitCode(), "lint file command should exit 0");
    }

    @Test
    public void testRunLintCommandOnFileAndNonExecutableFile(CodeInsightTestFixture codeInsightTestFixture) {
        settings.valePath = BaseTest.testProjectPath + "/.vale.ini";
        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        assertThrows(ExecutionException.class, ()-> {
            ProcessOutput output = executor.runLintStdinCommand(markDownContent, "md", "dummy.md");
            assertNotEquals(0, output.getExitCode(), "lint file command should exit 0");
        });
    }

    @Test
    public void testRunLintCommandOnFileAndInvalidConfig(CodeInsightTestFixture codeInsightTestFixture) throws Exception {
        ValePluginProjectSettingsState pluginProjectSettingsState = codeInsightTestFixture.getProject().getService(ValePluginProjectSettingsState.class);
        pluginProjectSettingsState.setValeSettingsPath(BaseTest.testProjectPath + "/.valetest.ini");
        ValeCliExecutor executor = new ValeCliExecutor(codeInsightTestFixture.getProject(), this.settings, pluginProjectSettingsState);
        ProcessOutput output = executor.runLintStdinCommand(markDownContent, "md", "dummy.md");
        assertNotEquals(0, output.getExitCode(), "lint file command should exit non 0");
    }

    @Test
    public void testInvalidSettings(@NotNull CodeInsightTestFixture codeInsightTestFixture) throws Exception {
        ValePluginProjectSettingsState pluginProjectSettingsState = codeInsightTestFixture.getProject()
                .getService(ValePluginProjectSettingsState.class);
        pluginProjectSettingsState.getState().valeSettingsPath = BaseTest.testProjectPath + "/.does.not.exist.vale.ini";
        settings.valePath = BaseTest.testProjectPath + "/.vale.ini";
        ValeCliExecutor executor = new ValeCliExecutor(codeInsightTestFixture.getProject(), this.settings, pluginProjectSettingsState);
        executor.checkConfiguration();
    }

    @Test
    public void testInvalidConfigPath(@NotNull CodeInsightTestFixture codeInsightTestFixture) throws Exception {
        ValePluginProjectSettingsState pluginProjectSettingsState = codeInsightTestFixture.getProject()
                .getService(ValePluginProjectSettingsState.class);
        pluginProjectSettingsState.getState().valeSettingsPath = BaseTest.testProjectPath + "/.does.not.exist.vale.ini";
        settings.valePath = "";
        ValeCliExecutor executor = new ValeCliExecutor(codeInsightTestFixture.getProject(), this.settings, pluginProjectSettingsState);
        executor.checkConfiguration();
    }
    @Test
    public void testInvalidSettingsNotExecutableBinary(@NotNull CodeInsightTestFixture codeInsightTestFixture) throws Exception {
        ValePluginProjectSettingsState pluginProjectSettingsState = codeInsightTestFixture.getProject()
                .getService(ValePluginProjectSettingsState.class);
        pluginProjectSettingsState.getState().valeSettingsPath = BaseTest.testProjectPath + "/.does.not.exist.vale.ini";
        settings.valePath = BaseTest.testProjectPath + "/.does.not.exist.vale.ini";
        ValeCliExecutor executor = new ValeCliExecutor(codeInsightTestFixture.getProject(), this.settings, pluginProjectSettingsState);
        executor.checkConfiguration();
    }

}
