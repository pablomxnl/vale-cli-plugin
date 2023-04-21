package org.ideplugins.vale_cli_plugin.annotator;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.daemon.impl.DefaultHighlightVisitorBasedInspection;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;
import org.ideplugins.vale_cli_plugin.BaseTest;
import org.ideplugins.vale_cli_plugin.testing.PluginTest;
import org.ideplugins.vale_cli_plugin.exception.ValeCliExecutionException;
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor;
import org.ideplugins.vale_cli_plugin.service.ValeIssuesReporter;
import org.ideplugins.vale_cli_plugin.testing.RunInEdtExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.zeroturnaround.exec.StartedProcess;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PluginTest
@ExtendWith(RunInEdtExtension.class)
public class ValeExternalAnnotatorProcessorTest extends BaseTest {

    @Test
    public void testAnnotator(JavaCodeInsightTestFixture codeInsightTestFixture) throws ValeCliExecutionException {
        codeInsightTestFixture.copyDirectoryToProject("annotator-test", "src");
        PsiFile file = codeInsightTestFixture.configureFromTempProjectFile("src/readme.md");
        ValeCliExecutor executor = ValeCliExecutor.getInstance(codeInsightTestFixture.getProject());
        ValeIssuesReporter reporter = codeInsightTestFixture.getProject().getService(ValeIssuesReporter.class);
        StartedProcess process = executor.executeValeCliOnFile(file);
        Map<String, List<JsonObject>> result = executor.parseValeJsonResponse(process.getFuture(), 1);
        reporter.updateIssuesForFile(file.getVirtualFile().getPath(), result.get(file.getVirtualFile().getPath()));
        assertTrue(result.containsKey(file.getVirtualFile().getPath()), "Results should containe file");
        assertEquals(7, result.get(file.getVirtualFile().getPath()).size());
        ValeExternalAnnotatorProcessor annotatorProcessor = new ValeExternalAnnotatorProcessor();
        InitialAnnotatorInfo info = annotatorProcessor.collectInformation(file);
        AnnotatorResult annotatorResult = annotatorProcessor.doAnnotate(info);
        AnnotationHolder holder = mock(AnnotationHolder.class);
        DefaultHighlightVisitorBasedInspection.runAnnotatorsInGeneralHighlighting(file, false, true);
        AnnotationBuilder builder = mock(AnnotationBuilder.class);
        when(holder.newAnnotation(any(HighlightSeverity.class), anyString())).thenReturn(builder);
        when(builder.tooltip(anyString())).thenReturn(builder);
        when(builder.range(any(TextRange.class))).thenReturn(builder);
        annotatorProcessor.apply(file, annotatorResult, holder);
        codeInsightTestFixture.testHighlighting();

    }

}