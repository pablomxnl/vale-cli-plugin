package org.ideplugins.plugin.testing;

import com.intellij.testFramework.TestApplicationManager;
import com.intellij.testFramework.TestIndexingModeSupporter;
import com.intellij.testFramework.fixtures.*;
import org.junit.jupiter.api.extension.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

public class PluginCodeInsightTestFixtureProvider
        implements ParameterResolver, BeforeEachCallback, AfterEachCallback {

    private static final String DEFAULT_TEST_DATA_PATH = "build";
    private static JavaCodeInsightTestFixture codeInsightTestFixture;
    private static TempDirTestFixture tempDirTestFixture;

    @Override
    public boolean supportsParameter(
            ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return JavaCodeInsightTestFixture.class.isAssignableFrom(
                parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(
            ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        if (codeInsightTestFixture == null) {
            throw new ParameterResolutionException("CodeInsightTestFixture is missing.");
        }

        return codeInsightTestFixture;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        try {
            if (codeInsightTestFixture != null) {
                codeInsightTestFixture.tearDown();
            }
        } catch (Exception e) {
            context.publishReportEntry("", "Failed to teardown JavaCodeInsightTestFixture.");
        }
        TestApplicationManager.getInstance().setDataProvider(null);
        codeInsightTestFixture = null;
        tempDirTestFixture = null;
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        Optional<PluginTestDataPath> pluginTestDataPathAnnotation =
                extensionContext
                        .getTestClass()
                        .flatMap(testClass -> findAnnotation(testClass, PluginTestDataPath.class));


        String testDataPath =
                pluginTestDataPathAnnotation.map(PluginTestDataPath::value).orElse(DEFAULT_TEST_DATA_PATH);

        codeInsightTestFixture = createLightCodeInsightTestFixture(testDataPath, extensionContext);
    }

    private JavaCodeInsightTestFixture createLightCodeInsightTestFixture(String testDataPath, ExtensionContext extensionContext) {
        String projectName = extensionContext.getRequiredTestMethod().getName() + UUID.randomUUID();
        Path projectPath = Path.of(System.getProperty("java.io.tmpdir", "/tmp"), projectName);
        tempDirTestFixture = new TempDirProjectImpl(projectPath);

        IdeaProjectTestFixture projectTestFixture = IdeaTestFixtureFactory.getFixtureFactory().
                createFixtureBuilder(projectName, projectPath, false)
                .getFixture();


        return createCodeInsightTextFixture(testDataPath, projectTestFixture);
    }

    private JavaCodeInsightTestFixture createCodeInsightTextFixture(
            String testDataPath, IdeaProjectTestFixture projectTestFixture) {
        codeInsightTestFixture =
                JavaTestFixtureFactory.getFixtureFactory()
                        .createCodeInsightFixture(projectTestFixture, tempDirTestFixture);
        codeInsightTestFixture =
                JavaIndexingModeCodeInsightTestFixture.Companion.wrapFixture(
                        codeInsightTestFixture, TestIndexingModeSupporter.IndexingMode.SMART);

        codeInsightTestFixture.setTestDataPath(testDataPath);

        try {
            codeInsightTestFixture.setUp();
            Files.createDirectories(Path.of(codeInsightTestFixture.getProject().getBasePath()));
            return codeInsightTestFixture;
        } catch (Exception e) {
            throw new ParameterResolutionException("Could not create CodeInsightTextFixture.", e);
        }
    }


}
