package org.ideplugins.vale_cli_plugin.actions;

import com.google.gson.JsonObject;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.ideplugins.vale_cli_plugin.BaseTest;
import org.ideplugins.vale_cli_plugin.exception.ValeCliExecutionException;
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor;
import org.ideplugins.vale_cli_plugin.testing.PluginTest;
import org.ideplugins.vale_cli_plugin.testing.RunInEdtExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PluginTest
@ExtendWith(RunInEdtExtension.class)
class ValePopupActionTest extends BaseTest {

    @Test
    public void testAction(CodeInsightTestFixture codeInsightTestFixture) throws ValeCliExecutionException {
        codeInsightTestFixture.copyDirectoryToProject("multiplefiles-example", "content");
        PsiFile[] files = codeInsightTestFixture.configureByFiles("content/readme.md", "content/manual.md");
        codeInsightTestFixture.testAction(new ValePopupAction());
        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        StartedProcess process =
                executor.executeValeCliOnFiles(Arrays.stream(files).map(f -> f.getVirtualFile().getPath()).collect(Collectors.toList()));
        Map<String, List<JsonObject>> result = executor.parseValeJsonResponse(process.getFuture(), files.length);
        Arrays.stream(files).forEach(file ->
                assertTrue(result.containsKey(file.getVirtualFile().getPath()), "Results should containe file"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSingleFileConsoleError(CodeInsightTestFixture codeInsightTestFixture) throws IOException {
        String nonJsonOutput = Files.readString(Path.of("build/resources/test/faultyJsonConsoleResponse.json"));
        codeInsightTestFixture.copyDirectoryToProject("markdown-example", "src");
        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        Future<ProcessResult> future = (Future<ProcessResult>) mock(Future.class);
        ProcessResult processResult = mock(ProcessResult.class);
        ValeCliExecutionException badJson = assertThrows(ValeCliExecutionException.class, ()-> {
            when(future.get(anyLong(), any(TimeUnit.class))).thenReturn(processResult);
            when(processResult.outputUTF8()).thenReturn(nonJsonOutput);
            executor.parseValeJsonResponse(future,2);
        });
        assertTrue( badJson.getMessage().contains("Invalid JSON"), "Should have invalid JSON format message");
    }

}