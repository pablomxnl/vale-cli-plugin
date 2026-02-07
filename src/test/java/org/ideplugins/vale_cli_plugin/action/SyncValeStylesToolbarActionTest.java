package org.ideplugins.vale_cli_plugin.action;

import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.ideplugins.vale_cli_plugin.BaseTest;
import org.ideplugins.vale_cli_plugin.actions.SyncValeStylesToolbarAction;
import org.ideplugins.vale_cli_plugin.testing.PluginTest;
import org.ideplugins.vale_cli_plugin.testing.RunInEdtExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@PluginTest
@ExtendWith(RunInEdtExtension.class)
public class SyncValeStylesToolbarActionTest extends BaseTest{

    @Test
    public void testAction(CodeInsightTestFixture codeInsightTestFixture){
        codeInsightTestFixture.testAction(new SyncValeStylesToolbarAction());
    }

    @Test
    public void testActionWithInvalidSettings(CodeInsightTestFixture codeInsightTestFixture){
        settings.valePath = "";
        codeInsightTestFixture.testAction(new SyncValeStylesToolbarAction());
    }
}
