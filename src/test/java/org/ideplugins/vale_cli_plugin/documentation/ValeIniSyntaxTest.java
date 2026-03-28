package org.ideplugins.vale_cli_plugin.documentation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ValeIniSyntaxTest {

    private static final String SAMPLE = """
            ; comment
            [*]
            [formats]
            StylesPath = styles
            MinAlertLevel: warning
            valid-key_1.2 = value
            invalid/key = value
            """;

    @Test
    void shouldFindPropertyKeyAtOffset() {
        int keyOffset = SAMPLE.indexOf("StylesPath") + 2;
        ValeIniSyntax.Property property = ValeIniSyntax.findPropertyKeyAtOffset(SAMPLE, keyOffset);
        assertNotNull(property);
        assertEquals("StylesPath", property.key());
        assertEquals("styles", property.value());
    }

    @Test
    void shouldNotReturnPropertyForValueOrCommentOrSection() {
        int valueOffset = SAMPLE.indexOf("warning") + 2;
        assertNull(ValeIniSyntax.findPropertyKeyAtOffset(SAMPLE, valueOffset));

        int commentOffset = SAMPLE.indexOf("comment") + 1;
        assertNull(ValeIniSyntax.findPropertyKeyAtOffset(SAMPLE, commentOffset));

        int sectionOffset = SAMPLE.indexOf("[*]") + 1;
        assertNull(ValeIniSyntax.findPropertyKeyAtOffset(SAMPLE, sectionOffset));
    }

    @Test
    void shouldFindSectionNameAtOffset() {
        int sectionOffset = SAMPLE.indexOf("formats") + 2;
        ValeIniSyntax.Section section = ValeIniSyntax.findSectionNameAtOffset(SAMPLE, sectionOffset);
        assertNotNull(section);
        assertEquals("formats", section.name());
    }

    @Test
    void shouldIgnoreWildcardSection() {
        // We only accept certain characters as part of allowed names in ValeIniSyntax.
        // "*" is an example but also "/" would make the name invalid
        int sectionOffset = SAMPLE.indexOf("[*]") + 1;
        assertNull(ValeIniSyntax.findSectionNameAtOffset(SAMPLE, sectionOffset));
    }

    @Test
    void shouldIgnoreInvalidPropertyKey() {
        int invalidKeyOffset = SAMPLE.indexOf("invalid/key") + 2;
        assertNull(ValeIniSyntax.findPropertyKeyAtOffset(SAMPLE, invalidKeyOffset));
    }

    @Test
    void shouldAcceptFilenameSafeKeyCharacters() {
        int keyOffset = SAMPLE.indexOf("valid-key_1.2") + 2;
        ValeIniSyntax.Property property = ValeIniSyntax.findPropertyKeyAtOffset(SAMPLE, keyOffset);
        assertNotNull(property);
        assertEquals("valid-key_1.2", property.key());
    }
}
