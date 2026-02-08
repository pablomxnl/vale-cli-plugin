package org.ideplugins.vale_cli_plugin.action;

import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.ideplugins.vale_cli_plugin.BaseTest;
import org.ideplugins.vale_cli_plugin.actions.SettingsToolbarAction;
import org.ideplugins.vale_cli_plugin.testing.PluginTest;
import org.ideplugins.vale_cli_plugin.testing.RunInEdtExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@PluginTest
@ExtendWith(RunInEdtExtension.class)
public class SettingsToolbarActionTest extends BaseTest{

    @Test
    public void testAction(CodeInsightTestFixture codeInsightTestFixture){
        codeInsightTestFixture.testAction(new SettingsToolbarAction());
    }
}
