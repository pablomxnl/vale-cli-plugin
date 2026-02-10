package org.ideplugins.vale_cli_plugin;

import com.intellij.openapi.util.io.IoTestUtil;
import org.apache.commons.io.FileUtils;
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class BaseTest {

    protected ValePluginSettingsState settings;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Files.copy(Path.of("build", "resources", "test", ".vale.ini"),
                Path.of(IoTestUtil.getTempDirectory().getPath(),  ".vale.ini"), REPLACE_EXISTING);
        FileUtils.copyDirectory(Path.of("build", "resources", "test", "styles").toFile(),
                Path.of(IoTestUtil.getTempDirectory().getPath(),  "styles").toFile());
    }


    @BeforeEach
    public void setUp() {
        settings = ValePluginSettingsState.getInstance();
        settings.valeSettingsPath = "";
        settings.extensions = "adoc,md";
    }

    private static Boolean areTestRunningInCI() {
        return  Optional.ofNullable(System.getenv("CI_PROJECT_DIR")).isPresent();
    }
}
