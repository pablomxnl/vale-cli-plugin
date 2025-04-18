package org.ideplugins.vale_cli_plugin.service;

import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.ideplugins.vale_cli_plugin.BaseTest;
import org.ideplugins.vale_cli_plugin.testing.PluginTest;
import org.ideplugins.vale_cli_plugin.exception.ValeCliExecutionException;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.zeroturnaround.exec.StartedProcess;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@PluginTest
public class ValeCliExecutorTest extends BaseTest {


    @Test
    @Order(3)
    public void testSingleFile(CodeInsightTestFixture codeInsightTestFixture) throws ValeCliExecutionException {
        codeInsightTestFixture.copyDirectoryToProject("markdown-example", "src");
        PsiFile file = codeInsightTestFixture.configureFromTempProjectFile("src/readme.md");
        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        StartedProcess process = executor.executeValeCliOnFile(file);
        Map<String, List<JsonObject>> result = executor.parseValeJsonResponse(process.getFuture(), 5);
        assertTrue(result.containsKey(file.getVirtualFile().getPath()), "Results should containe file");
        assertEquals(2, result.get(file.getVirtualFile().getPath()).size());
    }

    @Test
    @Order(2)
    public void testMultipleFiles(CodeInsightTestFixture codeInsightTestFixture) throws ValeCliExecutionException {
        codeInsightTestFixture.copyDirectoryToProject("multiplefiles-example", "content");
        PsiFile []files = codeInsightTestFixture.configureByFiles("content/readme.md", "content/manual.md");
        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        StartedProcess process  = executor.executeValeCliOnFiles(Arrays.stream(files).map(f -> f.getVirtualFile().getPath()).collect(Collectors.toList()));
        Map<String, List<JsonObject>> result = executor.parseValeJsonResponse(process.getFuture(), files.length);
        Arrays.stream(files).forEach(file -> {
            assertTrue(result.containsKey(file.getVirtualFile().getPath()), "Results should contain file");
//            assertEquals(2, result.get(file.getVirtualFile().getPath()).size());
        });
    }


    @Test
    @Order(1)
    public void testProject(CodeInsightTestFixture codeInsightTestFixture) throws ValeCliExecutionException {
        codeInsightTestFixture.copyDirectoryToProject("multiplefiles-example", "src");
        PsiFile []files = codeInsightTestFixture.configureByFiles("src/readme.md", "src/manual.md");
        Project project = codeInsightTestFixture.getProject();
        ValeCliExecutor executor = ValeCliExecutor.getInstance(project);
        StartedProcess process  = executor.executeValeCliOnPath(project.getBasePath());
        Map<String, List<JsonObject>> result = executor.parseValeJsonResponse(process.getFuture(), files.length);
        Arrays.stream(files).forEach(file -> assertTrue(result.containsKey(file.getVirtualFile().getPath()), "Results should contain file"));
    }

    @Test
    @Order(20)
    public void testValeConfigurationFileNotFound(CodeInsightTestFixture codeInsightTestFixture) {
        codeInsightTestFixture.copyDirectoryToProject("multiplefiles-example", "content");
        PsiFile file = codeInsightTestFixture.configureFromTempProjectFile("content/readme.md");
        settings.valeSettingsPath = "/tmp/.valeddd.ini";
        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        ValeCliExecutionException exception = assertThrows(ValeCliExecutionException.class, ()->{
            StartedProcess process = executor.executeValeCliOnFile(file);
            executor.parseValeJsonResponse(process.getFuture(),2);
        });
        assertThat(exception.getMessage()).isNotNull().contains("Unexpected exit value: 2, allowed exit values: [0]");
    }

    @Test
    @Order(21)
    public void testBinaryNotFound(CodeInsightTestFixture codeInsightTestFixture) throws ValeCliExecutionException {
        settings.valePath = "/home/tmp/vale";
        codeInsightTestFixture.copyDirectoryToProject("multiplefiles-example", "content");
        PsiFile file = codeInsightTestFixture.configureFromTempProjectFile("content/readme.md");
        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        StartedProcess sp = executor.executeValeCliOnFile(file);
        ExecutionException exception = assertThrows(ExecutionException.class, ()-> sp.getFuture().get());
        assertThat(exception.getMessage()).isNotNull().contains("/home/tmp/vale");
    }


}