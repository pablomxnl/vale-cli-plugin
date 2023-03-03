package org.ideplugins.plugin.actions;

import com.google.gson.JsonObject;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;
import org.ideplugins.plugin.BaseTest;
import org.ideplugins.plugin.exception.ValeCliExecutionException;
import org.ideplugins.plugin.service.ValeCliExecutor;
import org.ideplugins.plugin.testing.PluginTest;
import org.ideplugins.plugin.testing.RunInEdtExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.zeroturnaround.exec.StartedProcess;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginTest
@ExtendWith(RunInEdtExtension.class)
class ValePopupActionTest extends BaseTest {

    @Test
    public void testAction(JavaCodeInsightTestFixture codeInsightTestFixture) throws ValeCliExecutionException {
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

}