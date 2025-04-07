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
import org.zeroturnaround.exec.StartedProcess;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginTest
@ExtendWith(RunInEdtExtension.class)
public class ValePopupDirectoryActionTest extends BaseTest {

    @Test
    public void testDirectoryPopupAction(CodeInsightTestFixture codeInsightTestFixture) throws ValeCliExecutionException {
        codeInsightTestFixture.copyDirectoryToProject("directory-example", "content");
        PsiFile[] files = codeInsightTestFixture.configureByFiles("content/docs/readme.md", "content/docs/manual.md");
        codeInsightTestFixture.testAction(new ValePopupDirectoryAction());

        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        StartedProcess process = executor.executeValeCliOnPath(codeInsightTestFixture.getProject().getBasePath());
        Map<String, List<JsonObject>> result = executor.parseValeJsonResponse(process.getFuture(), files.length);
        Arrays.stream(files).forEach(file -> assertTrue(result.containsKey(file.getVirtualFile().getPath()), "Results should containe file"));
    }
}
