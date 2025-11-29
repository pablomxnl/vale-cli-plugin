package org.ideplugins.vale_cli_plugin.settings;

import org.ideplugins.vale_cli_plugin.BaseTest;
import org.ideplugins.vale_cli_plugin.testing.PluginTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginTest
public class ValePluginSettingsStateTest extends BaseTest {

    @Test
    void allSettingsBlank() {
        settings.valePath = "";
        settings.extensions = "";
        assertFalse(settings.areSettingsValid().getKey(), "Should return false");
    }

    @Test
    void validSettings() {
        assertTrue(settings.areSettingsValid().getKey(), "Should return false");
    }



    @Test
    void validSettingsFile() {
//        settings.valeSettingsPath = "/tmp/.vale.ini";
        assertTrue(settings.areSettingsValid().getKey(), "Should return false");
    }
}