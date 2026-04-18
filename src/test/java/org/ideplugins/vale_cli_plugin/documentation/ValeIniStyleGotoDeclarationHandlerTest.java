package org.ideplugins.vale_cli_plugin.documentation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import ini4idea.lang.psi.IniProperty;
import org.ideplugins.vale_cli_plugin.testing.PluginTest;
import org.ideplugins.vale_cli_plugin.testing.RunInEdtExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@PluginTest
@ExtendWith(RunInEdtExtension.class)
class ValeIniStyleGotoDeclarationHandlerTest {

    private static final String SAMPLE_INI = """
            StylesPath = styles
            MinAlertLevel = suggestion

            [*.{md,txt}]
            BasedOnStyles = Google, Vale, RedHat
            """;
    @Test
    void shouldExtractFirstStyle(CodeInsightTestFixture fixture) {
        PsiFile file = fixture.configureByText(".vale.ini", SAMPLE_INI);
        IniProperty property = findBasedOnStylesProperty(file);

        // Offset pointing at "Google"
        int offset = SAMPLE_INI.indexOf("Google") + 2;
        assertEquals("Google", ValeIniStyleGotoDeclarationHandler.extractStyleAtOffset(property, offset));
    }

    @Test
    void shouldExtractMiddleStyle(CodeInsightTestFixture fixture) {
        PsiFile file = fixture.configureByText(".vale.ini", SAMPLE_INI);
        IniProperty property = findBasedOnStylesProperty(file);

        int offset = SAMPLE_INI.indexOf("Vale") + 1;
        assertEquals("Vale", ValeIniStyleGotoDeclarationHandler.extractStyleAtOffset(property, offset));
    }

    @Test
    void shouldExtractLastStyle(CodeInsightTestFixture fixture) {
        PsiFile file = fixture.configureByText(".vale.ini", SAMPLE_INI);
        IniProperty property = findBasedOnStylesProperty(file);

        int offset = SAMPLE_INI.indexOf("RedHat") + 3;
        assertEquals("RedHat", ValeIniStyleGotoDeclarationHandler.extractStyleAtOffset(property, offset));
    }

    @Test
    void shouldReturnNullWhenOffsetBeforeValue(CodeInsightTestFixture fixture) {
        PsiFile file = fixture.configureByText(".vale.ini", SAMPLE_INI);
        IniProperty property = findBasedOnStylesProperty(file);

        // Offset pointing at "BasedOnStyles" key itself (before '=')
        int offset = SAMPLE_INI.indexOf("BasedOnStyles") + 1;
        assertNull(ValeIniStyleGotoDeclarationHandler.extractStyleAtOffset(property, offset));
    }

    @Test
    void shouldReturnNullForNonIniFile(CodeInsightTestFixture fixture) {
        PsiFile file = fixture.configureByText("rule.yml", "extends: existence\n");
        PsiElement element = file.findElementAt(0);

        ValeIniStyleGotoDeclarationHandler handler = new ValeIniStyleGotoDeclarationHandler();
        PsiElement[] targets = handler.getGotoDeclarationTargets(element, 0, fixture.getEditor());

        assertNull(targets);
    }

    @Test
    void shouldReturnNullForUnrelatedIniProperty(CodeInsightTestFixture fixture) {
        String ini = "StylesPath = styles\n";
        PsiFile file = fixture.configureByText(".vale.ini", ini);

        // Offset on the value "styles"
        int offset = ini.indexOf("styles") + 2;
        PsiElement element = file.findElementAt(offset);

        ValeIniStyleGotoDeclarationHandler handler = new ValeIniStyleGotoDeclarationHandler();
        PsiElement[] targets = handler.getGotoDeclarationTargets(element, offset, fixture.getEditor());

        assertNull(targets);
    }

    @Test
    void shouldReturnNullWhenCursorIsOnPropertyKey(CodeInsightTestFixture fixture) {
        PsiFile file = fixture.configureByText(".vale.ini", SAMPLE_INI);

        // Offset on "BasedOnStyles" key text itself
        int offset = SAMPLE_INI.indexOf("BasedOnStyles") + 3;
        PsiElement element = file.findElementAt(offset);

        ValeIniStyleGotoDeclarationHandler handler = new ValeIniStyleGotoDeclarationHandler();
        PsiElement[] targets = handler.getGotoDeclarationTargets(element, offset, fixture.getEditor());

        assertNull(targets);
    }

    private IniProperty findBasedOnStylesProperty(PsiFile file) {
        return PsiTreeUtil.findChildrenOfType(file, IniProperty.class).stream()
                .filter(p -> "BasedOnStyles".equals(p.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("BasedOnStyles property not found"));
    }
}
