package org.ideplugins.plugin.service;

import com.google.gson.JsonObject;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;
import org.ideplugins.plugin.BaseTest;
import org.ideplugins.plugin.testing.PluginTest;
import org.ideplugins.plugin.exception.ValeCliExecutionException;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.zeroturnaround.exec.StartedProcess;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@PluginTest
public class ValeCliExecutorTest extends BaseTest {


    @Test
    @Order(3)
    public void testSingleFile(JavaCodeInsightTestFixture codeInsightTestFixture) throws ValeCliExecutionException {
        codeInsightTestFixture.copyDirectoryToProject("markdown-example", "src");
        PsiFile file = codeInsightTestFixture.configureFromTempProjectFile("src/readme.md");
        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        StartedProcess process = executor.executeValeCliOnFile(file);
        Map<String, List<JsonObject>> result = executor.parseValeJsonResponse(process.getFuture(), 1);
        assertTrue(result.containsKey(file.getVirtualFile().getPath()), "Results should containe file");
        assertEquals(2, result.get(file.getVirtualFile().getPath()).size());
    }

    @Test
    @Order(2)
    public void testMultipleFiles(JavaCodeInsightTestFixture codeInsightTestFixture) throws ValeCliExecutionException {
        codeInsightTestFixture.copyDirectoryToProject("multiplefiles-example", "content");
        PsiFile []files = codeInsightTestFixture.configureByFiles("content/readme.md", "content/manual.md");
        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        StartedProcess process  = executor.executeValeCliOnFiles(Arrays.stream(files).map(f -> f.getVirtualFile().getPath()).collect(Collectors.toList()));
        Map<String, List<JsonObject>> result = executor.parseValeJsonResponse(process.getFuture(), files.length);
        Arrays.stream(files).forEach(file -> {
            assertTrue(result.containsKey(file.getVirtualFile().getPath()), "Results should containe file");
//            assertEquals(2, result.get(file.getVirtualFile().getPath()).size());
        });
    }


    @Test
    @Order(1)
    public void testProject(JavaCodeInsightTestFixture codeInsightTestFixture) throws ValeCliExecutionException {
        codeInsightTestFixture.copyDirectoryToProject("multiplefiles-example", "content");
        PsiFile []files = codeInsightTestFixture.configureByFiles("content/readme.md", "content/manual.md");
        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        StartedProcess process  = executor.executeValeCliOnProject();
        Map<String, List<JsonObject>> result = executor.parseValeJsonResponse(process.getFuture(), files.length);
        Arrays.stream(files).forEach(file -> {
            assertTrue(result.containsKey(file.getVirtualFile().getPath()), "Results should containe file");
//            assertEquals(2, result.get(file.getVirtualFile().getPath()).size());
        });
    }

    @Test
    @Order(20)
    public void testValeConfigurationFileNotFound(JavaCodeInsightTestFixture codeInsightTestFixture) {
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
    public void testBinaryNotFound(JavaCodeInsightTestFixture codeInsightTestFixture) {
        codeInsightTestFixture.copyDirectoryToProject("multiplefiles-example", "content");
        PsiFile file = codeInsightTestFixture.configureFromTempProjectFile("content/readme.md");
        settings.valePath = "/home/tmp/vale";
        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        ValeCliExecutionException exception = assertThrows(ValeCliExecutionException.class, ()-> executor.executeValeCliOnFile(file));
        assertThat(exception.getMessage()).isNotNull().contains("Could not execute [/home/tmp/vale");
    }


}