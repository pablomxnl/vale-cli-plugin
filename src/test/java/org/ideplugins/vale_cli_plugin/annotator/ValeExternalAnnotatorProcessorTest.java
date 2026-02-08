package org.ideplugins.vale_cli_plugin.annotator;

import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.ideplugins.vale_cli_plugin.BaseTest;
import org.ideplugins.vale_cli_plugin.settings.ValePluginProjectSettingsState;
import org.ideplugins.vale_cli_plugin.testing.PluginTest;
import org.ideplugins.vale_cli_plugin.testing.RunInEdtExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PluginTest
@ExtendWith(RunInEdtExtension.class)
public class ValeExternalAnnotatorProcessorTest extends BaseTest {

    @Test
    public void testAnnotator(CodeInsightTestFixture codeInsightTestFixture){
        codeInsightTestFixture.copyDirectoryToProject("annotator-test", "src");
        PsiFile file = codeInsightTestFixture.configureFromTempProjectFile("src/readme.md");
        ValeExternalAnnotatorProcessor annotatorProcessor = new ValeExternalAnnotatorProcessor();

        ValeExternalAnnotatorProcessor.InitialInfo info = annotatorProcessor.collectInformation(file);
        ValeExternalAnnotatorProcessor.AnalysisResult result = annotatorProcessor.doAnnotate(info);

        AnnotationHolder holder = mock(AnnotationHolder.class);
        AnnotationBuilder builder = mock(AnnotationBuilder.class);
        when(builder.range(any(TextRange.class))).thenReturn(builder);
        when(builder.withFix(any())).thenReturn(builder);
        when(holder.newAnnotation(any(HighlightSeverity.class), anyString()))
                .thenReturn(builder);

        annotatorProcessor.apply(file, result, holder);
    }

    @Test
    public void testShouldParseRuntimeError(CodeInsightTestFixture codeInsightTestFixture){
        codeInsightTestFixture.copyDirectoryToProject("annotator-test", "src");
        PsiFile file = codeInsightTestFixture.configureFromTempProjectFile("src/index.rst");

        ValeExternalAnnotatorProcessor annotatorProcessor = new ValeExternalAnnotatorProcessor();
        ValeExternalAnnotatorProcessor.InitialInfo info = annotatorProcessor.collectInformation(file);
        ValeExternalAnnotatorProcessor.AnalysisResult result = annotatorProcessor.doAnnotate(info);

        AnnotationHolder holder = mock(AnnotationHolder.class);
        AnnotationBuilder builder = mock(AnnotationBuilder.class);
        when(builder.range(any(TextRange.class))).thenReturn(builder);
        when(holder.newAnnotation(any(HighlightSeverity.class), anyString()))
                .thenReturn(builder);

        annotatorProcessor.apply(file, result, holder);
    }

    @Test
    public void testAnnotatorAdoc(CodeInsightTestFixture codeInsightTestFixture){
        codeInsightTestFixture.copyDirectoryToProject("annotator-test", "src");
        PsiFile file = codeInsightTestFixture.configureFromTempProjectFile("src/index.adoc");

        ValeExternalAnnotatorProcessor annotatorProcessor = new ValeExternalAnnotatorProcessor();
        ValeExternalAnnotatorProcessor.InitialInfo info = annotatorProcessor.collectInformation(file);
        ValeExternalAnnotatorProcessor.AnalysisResult result = annotatorProcessor.doAnnotate(info);

        AnnotationHolder holder = mock(AnnotationHolder.class);
        AnnotationBuilder builder = mock(AnnotationBuilder.class);
        when(builder.range(any(TextRange.class))).thenReturn(builder);
        when(holder.newAnnotation(any(HighlightSeverity.class), anyString()))
                .thenReturn(builder);

        annotatorProcessor.apply(file, result, holder);
    }

    @Test
    public void testAnnotatorDocumentChanged(CodeInsightTestFixture codeInsightTestFixture){
        codeInsightTestFixture.copyDirectoryToProject("annotator-test", "src");
        PsiFile file = codeInsightTestFixture.configureFromTempProjectFile("src/readme.md");
        ValeExternalAnnotatorProcessor annotatorProcessor = new ValeExternalAnnotatorProcessor();
        String markDownContent  = """
        ## This is a test
        This text is being written in passive voice.
        """;
        ApplicationManager.getApplication().runWriteAction(()-> file.getFileDocument().setText(markDownContent));

        ValeExternalAnnotatorProcessor.InitialInfo info = annotatorProcessor.collectInformation(file);
        ValeExternalAnnotatorProcessor.AnalysisResult result = annotatorProcessor.doAnnotate(info);

        AnnotationHolder holder = mock(AnnotationHolder.class);
        AnnotationBuilder builder = mock(AnnotationBuilder.class);
        when(builder.range(any(TextRange.class))).thenReturn(builder);
        when(holder.newAnnotation(any(HighlightSeverity.class), anyString()))
                .thenReturn(builder);

        annotatorProcessor.apply(file, result, holder);
    }


    @Test
    public void testCollectReturnsNull(CodeInsightTestFixture fixture) {
        fixture.copyDirectoryToProject("multiplefiles-example", "src");
        PsiFile file = fixture.configureFromTempProjectFile("src/adoc.adoc");
        ValePluginProjectSettingsState pluginProjectSettingsState = fixture.getProject()
                .getService(ValePluginProjectSettingsState.class);
        pluginProjectSettingsState.setExtensions("md,xml,rst");

        ValeExternalAnnotatorProcessor annotatorProcessor = new ValeExternalAnnotatorProcessor();
        ValeExternalAnnotatorProcessor.InitialInfo info = annotatorProcessor.collectInformation(file);
        assertNull(info, "Should return null for not configured extension");
        ValeExternalAnnotatorProcessor.AnalysisResult result = annotatorProcessor.doAnnotate(info);
        assertNull(result, "Should return null for not configured extension");
    }

    @Test
    public void testDoAnnotateReturnsNull(CodeInsightTestFixture fixture) {
        fixture.copyDirectoryToProject("multiplefiles-example", "src");
        PsiFile file = fixture.configureFromTempProjectFile("src/adoc.adoc");
        ValePluginProjectSettingsState pluginProjectSettingsState = fixture.getProject()
                .getService(ValePluginProjectSettingsState.class);
        pluginProjectSettingsState.setExtensions("md,xml,adoc");
        ValeExternalAnnotatorProcessor annotatorProcessor = new ValeExternalAnnotatorProcessor();
        ValeExternalAnnotatorProcessor.InitialInfo info = annotatorProcessor.collectInformation(file);
        assertNotNull(info, "Should not return null for configured extension");
        pluginProjectSettingsState.setExtensions("md,xml,rst");
        ValeExternalAnnotatorProcessor.AnalysisResult result = annotatorProcessor.doAnnotate(info);
        assertNull(result, "Should return null for not configured extension");
        AnnotationHolder holder = mock(AnnotationHolder.class);
        AnnotationBuilder builder = mock(AnnotationBuilder.class);
        when(builder.range(any(TextRange.class))).thenReturn(builder);
        when(holder.newAnnotation(any(HighlightSeverity.class), anyString()))
                .thenReturn(builder);

        annotatorProcessor.apply(file, result, holder);
    }

    @Test
    public void testDoAnnotateReturnsNullWhenBadConfig(CodeInsightTestFixture fixture) {
        fixture.copyDirectoryToProject("multiplefiles-example", "src");
        PsiFile file = fixture.configureFromTempProjectFile("src/readme.md");
        ValePluginProjectSettingsState pluginProjectSettingsState = fixture.getProject()
                .getService(ValePluginProjectSettingsState.class);
        ValeExternalAnnotatorProcessor annotatorProcessor = new ValeExternalAnnotatorProcessor();
        ValeExternalAnnotatorProcessor.InitialInfo info = annotatorProcessor.collectInformation(file);
        assertNotNull(info, "Should not return null for configured extension");
        pluginProjectSettingsState.setExtensions("");
        ValeExternalAnnotatorProcessor.AnalysisResult result = annotatorProcessor.doAnnotate(info);
        assertNull(result, "Should return null for not configured extension");
    }

    @Test
    public void testParseValeRuntimeError(CodeInsightTestFixture fixture){
        fixture.copyDirectoryToProject("multiplefiles-example", "src");
        PsiFile file = fixture.configureFromTempProjectFile("src/adoc.adoc");
        ValePluginProjectSettingsState pluginProjectSettingsState = fixture.getProject()
                .getService(ValePluginProjectSettingsState.class);
        pluginProjectSettingsState.setValeSettingsPath(BaseTest.testProjectPath + "/.vale.ini");

        ValeExternalAnnotatorProcessor annotatorProcessor = new ValeExternalAnnotatorProcessor();
        ValeExternalAnnotatorProcessor.InitialInfo info = annotatorProcessor.collectInformation(file);
        ValeExternalAnnotatorProcessor.AnalysisResult result = annotatorProcessor.doAnnotate(info);

        AnnotationHolder holder = mock(AnnotationHolder.class);
        AnnotationBuilder builder = mock(AnnotationBuilder.class);
        when(builder.range(any(TextRange.class))).thenReturn(builder);
        when(builder.withFix(any())).thenReturn(builder);
        when(holder.newAnnotation(any(HighlightSeverity.class), anyString()))
                .thenReturn(builder);

        annotatorProcessor.apply(file, result, holder);
    }


}
