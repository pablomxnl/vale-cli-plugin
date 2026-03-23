package org.ideplugins.vale_cli_plugin.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OSUtilsTest {

    @Test
    void extractExecutablePathReturnsEmptyWhenNoExecutablePathExists() {
        String whichOutput = """
                vale: shell function
                /tmp/this/path/does/not/exist
                """;

        String detectedPath = OSUtils.extractExecutablePath(whichOutput);

        assertEquals("", detectedPath);
    }

    @Test
    void valeVersionReturnsEmptyForBlankPath() {
        assertEquals("", OSUtils.valeVersion("   "));
    }
}
