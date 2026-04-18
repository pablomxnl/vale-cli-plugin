package org.ideplugins.vale_cli_plugin.documentation;

import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.ideplugins.vale_cli_plugin.testing.PluginTest;
import org.ideplugins.vale_cli_plugin.testing.RunInEdtExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginTest
@ExtendWith(RunInEdtExtension.class)
class ValeDocumentationTargetProviderTest {

    private final ValeDocumentationTargetProvider provider = new ValeDocumentationTargetProvider();

    @Test
    void shouldProvideIniDocumentationOnlyOnKey(CodeInsightTestFixture fixture) {
        String ini = """
                StylesPath = styles
                MinAlertLevel = suggestion
                [formats]
                mdx = md
                """;
        PsiFile file = fixture.configureByText(".vale.ini", ini);

        int keyOffset = ini.indexOf("StylesPath") + 1;
        List<? extends DocumentationTarget> keyTargets = provider.documentationTargets(file, keyOffset);
        assertFalse(keyTargets.isEmpty());
        assertEquals("StylesPath", keyTargets.getFirst().computeDocumentationHint());
        assertNotNull(keyTargets.getFirst().computeDocumentation());

        int valueOffset = ini.indexOf("suggestion") + 1;
        List<? extends DocumentationTarget> valueTargets = provider.documentationTargets(file, valueOffset);
        assertTrue(valueTargets.isEmpty());

        int sectionOffset = ini.indexOf("formats") + 1;
        List<? extends DocumentationTarget> sectionTargets = provider.documentationTargets(file, sectionOffset);
        assertFalse(sectionTargets.isEmpty());
        assertEquals("[formats]", sectionTargets.getFirst().computeDocumentationHint());
        assertNotNull(sectionTargets.getFirst().computeDocumentation());
    }

    @Test
    void shouldProvideYamlDocumentationOnlyOnKey(CodeInsightTestFixture fixture) {
        String yml = """
                extends: existence
                message: Consider removing '%s'
                level: warning
                """;
        PsiFile file = fixture.configureByText("Rule.yml", yml);

        int keyOffset = yml.indexOf("message") + 1;
        List<? extends DocumentationTarget> keyTargets = provider.documentationTargets(file, keyOffset);
        assertFalse(keyTargets.isEmpty());
        assertEquals("message", keyTargets.getFirst().computeDocumentationHint());
        assertNotNull(keyTargets.getFirst().computeDocumentation());

        int valueOffset = yml.indexOf("warning") + 1;
        List<? extends DocumentationTarget> valueTargets = provider.documentationTargets(file, valueOffset);
        assertTrue(valueTargets.isEmpty());
    }
}
