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
public class ValeToolsMenuActionTest extends BaseTest {

    @Test
    public void testToolMenuAction(CodeInsightTestFixture codeInsightTestFixture) throws ValeCliExecutionException {
        codeInsightTestFixture.copyDirectoryToProject("multiplefiles-example", "content");
        PsiFile[] files = codeInsightTestFixture.configureByFiles("content/readme.md", "content/manual.md");
        codeInsightTestFixture.testAction(new ValeToolsMenuAction());

        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        StartedProcess process =
                executor.executeValeCliOnFiles(Arrays.stream(files).map(f -> f.getVirtualFile().getPath()).collect(Collectors.toList()));
        Map<String, List<JsonObject>> result = executor.parseValeJsonResponse(process.getFuture(), files.length);
        Arrays.stream(files).forEach(file -> assertTrue(result.containsKey(file.getVirtualFile().getPath()), "Results should containe file"));
    }


}