package org.ideplugins.vale_cli_plugin.documentation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValeDocumentationResolverTest {

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
    void shouldResolveIniKeyDocumentation() {
        String doc = resolver.resolveForIniKey("StylesPath");
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
    void shouldNotResolveIniDocumentationForUnknownKeyOrWildcardSection() {
        String valueDoc = resolver.resolveForIniKey("suggestion");
        assertNull(valueDoc);

        String sectionDoc = resolver.resolveForIniSection("[*]");
        assertNull(sectionDoc);
    }

    @Test
    void shouldResolveIniSectionDocumentation() {
        String sectionDoc = resolver.resolveForIniSection("[formats]");
        assertNotNull(sectionDoc);
        assertTrue(sectionDoc.contains("[formats]"));
    }

    @Test
    void shouldNotResolveYamlDocumentationForUnknownKey() {
        String doc = resolver.resolveForRuleKey("warning", EXISTENCE_RULE);
        assertNull(doc);
    }
}
