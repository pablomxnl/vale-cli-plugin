package org.ideplugins.vale_cli_plugin.activity;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;
import org.ideplugins.settings.SettingsProvider;
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor;
import org.ideplugins.vale_cli_plugin.testing.PluginTest;
import org.ideplugins.vale_cli_plugin.testing.RunInEdtExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@PluginTest(fixture = LightTempDirTestFixtureImpl.class)
@ExtendWith(RunInEdtExtension.class)
@TestDataPath("src/test/resource/multiplefiles-example")
public class ValeStartupActivityTest {

    @Test
    @Disabled("Works on my machine, fails in gitlab-runner")
    public void runActivity(CodeInsightTestFixture codeInsightTestFixture){
        mockSettings();
        PsiFile f1 = codeInsightTestFixture.configureByText("readme.md", "### readme.md");
        ValeStartupActivity vsa = new ValeStartupActivity();
        vsa.runActivity(codeInsightTestFixture.getProject());
        ValeCliExecutor executor = codeInsightTestFixture.getProject().getService(ValeCliExecutor.class);
        assertEquals(1, executor.getNumberOfFiles(), "Activity didn't initialize number of files");
    }

    private static void mockSettings() {
        MockedStatic<SettingsProvider> singleton = mockStatic(SettingsProvider.class);
        SettingsProvider provider = mock(SettingsProvider.class);
        singleton.when(SettingsProvider::getInstance).thenReturn(provider);
        when(provider.getSentryUrl(anyString())).thenReturn(UUID.randomUUID().toString());
    }

}


