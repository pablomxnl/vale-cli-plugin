package org.ideplugins.vale_cli_plugin;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationsConfiguration;
import com.intellij.openapi.util.io.IoTestUtil;
import com.intellij.testFramework.common.ThreadLeakTracker;
import org.apache.commons.io.FileUtils;
import org.ideplugins.vale_cli_plugin.settings.ValePluginProjectSettingsState;
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.ideplugins.vale_cli_plugin.settings.OSUtils.findValeBinaryPath;

public class BaseTest {

    protected ValePluginSettingsState settings;
    protected ValePluginProjectSettingsState.State projectSettings;
    protected static String testProjectPath;

    @BeforeAll
    public static void beforeAll() throws IOException {
        testProjectPath = IoTestUtil.getTempDirectory().getPath();
        Files.copy(Path.of("build", "resources", "test", ".vale.ini"),
                Path.of(testProjectPath,  ".vale.ini"), REPLACE_EXISTING);
        FileUtils.copyDirectory(Path.of("build", "resources", "test", "styles").toFile(),
                Path.of(testProjectPath,  "styles").toFile());
    }


    @BeforeEach
    public void setUp() {
        Application app = ApplicationManager.getApplication();
        ThreadLeakTracker.longRunningThreadCreated(app, "SystemPropertyWatcher");
        NotificationsConfiguration notificationsConfiguration = NotificationsConfiguration.getNotificationsConfiguration();
        notificationsConfiguration.register(Constants.VALE_NOTIFICATION_GROUP, NotificationDisplayType.BALLOON, false, false);
        notificationsConfiguration.register(Constants.UPDATE_NOTIFICATION_GROUP, NotificationDisplayType.BALLOON, false, false);
        settings = ValePluginSettingsState.getInstance();
        settings.setValePath(areTestRunningInCI()? "/usr/bin/vale" : findValeBinaryPath());
        projectSettings = new ValePluginProjectSettingsState.State();
    }

    private static Boolean areTestRunningInCI() {
        return  Optional.ofNullable(System.getenv("CI_PROJECT_DIR")).isPresent();
    }
}
