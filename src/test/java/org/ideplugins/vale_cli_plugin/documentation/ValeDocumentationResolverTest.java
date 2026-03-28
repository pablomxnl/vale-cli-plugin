package org.ideplugins.vale_cli_plugin.documentation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValeDocumentationResolverTest {

    private static final String SAMPLE_INI = """
            StylesPath = styles
            MinAlertLevel = suggestion
            Packages = Google, proselint, write-good
            [formats]
            mdx = md
            [*]
            BasedOnStyles = Vale, Google, proselint, write-good
            """;

    private static final String EXISTENCE_RULE = """
            extends: existence
            message: Consider removing '%s'
            level: warning
            ignorecase: true
            tokens:
              - appears to be
            """;

    private final ValeDocumentationResolver resolver = new ValeDocumentationResolver();

    @Test
    void shouldResolveIniDocumentation() {
        int offset = SAMPLE_INI.indexOf("StylesPath") + 2;
        String doc = resolver.resolveForIniOffset(SAMPLE_INI, offset);
        assertNotNull(doc);
        assertTrue(doc.contains("StylesPath"));
        assertTrue(doc.contains("path_to_directory"));
    }

    @Test
    void shouldResolveYamlCommonDocumentation() {
        String doc = resolver.resolveForRuleKey("message", EXISTENCE_RULE);
        assertNotNull(doc);
        assertTrue(doc.contains("message: \"Consider removing '%s'\""));
        assertTrue(doc.contains("shown in Vale's output"));
    }

    @Test
    void shouldResolveYamlRuleSpecificDocumentation() {
        String doc = resolver.resolveForRuleKey("tokens", EXISTENCE_RULE);
        assertNotNull(doc);
        assertTrue(doc.contains("non-capturing group"));
    }

    @Test
    void shouldMergeExtendsDocumentationWithRuleExample() {
        String doc = resolver.resolveForRuleKey("extends", EXISTENCE_RULE);
        assertNotNull(doc);
        assertTrue(doc.contains("`extends` indicates the extension point"));
        assertTrue(doc.contains("## Example"));
        assertTrue(doc.contains("extends: existence"));
    }

    @Test
    void shouldReturnNullForUnknownToken() {
        String doc = resolver.resolveForRuleKey("does-not-exist", EXISTENCE_RULE);
        assertNull(doc);
    }

    @Test
    void shouldNotResolveIniDocumentationForValueOrSection() {
        int valueOffset = SAMPLE_INI.indexOf("suggestion") + 2;
        String valueDoc = resolver.resolveForIniOffset(SAMPLE_INI, valueOffset);
        assertNull(valueDoc);

        int sectionOffset = SAMPLE_INI.indexOf("[*]") + 1;
        String sectionDoc = resolver.resolveForIniOffset(SAMPLE_INI, sectionOffset);
        assertNull(sectionDoc);
    }

    @Test
    void shouldResolveIniSectionDocumentation() {
        int sectionOffset = SAMPLE_INI.indexOf("formats") + 1;
        String sectionDoc = resolver.resolveForIniOffset(SAMPLE_INI, sectionOffset);
        assertNotNull(sectionDoc);
        assertTrue(sectionDoc.contains("[formats]"));
    }

    @Test
    void shouldNotResolveYamlDocumentationForUnknownKey() {
        String doc = resolver.resolveForRuleKey("warning", EXISTENCE_RULE);
        assertNull(doc);
    }
}
