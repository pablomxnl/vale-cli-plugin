package org.ideplugins.vale_cli_plugin.annotator;

import com.intellij.codeInsight.highlighting.TooltipLinkHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.notification.NotificationType;
import org.ideplugins.vale_cli_plugin.service.ValeConfigurationPaths;
import org.ideplugins.vale_cli_plugin.service.ValeLsConfigService;
import org.ideplugins.vale_cli_plugin.utils.NotificationHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class ValeRuleLinkHandler extends TooltipLinkHandler {

    public static final String PREFIX = "#vale-rule/";
    private static final Logger LOGGER = Logger.getInstance(ValeRuleLinkHandler.class);
    private static final String CHECK_PREFIX = "check/";

    @Override
    public boolean handleLink(@NotNull String ref, @NotNull Editor editor) {
        LOGGER.info("Vale rule link clicked: " + ref);
        String payload = extractPayload(ref);
        if (payload.isBlank()) {
            return false;
        }
        if (payload.startsWith(CHECK_PREFIX)) {
            String check = payload.substring(CHECK_PREFIX.length());
            Project project = editor.getProject();
            if (project == null) {
                return false;
            }
            ValeLsConfigService lsConfigService = ValeLsConfigService.getInstance(project);
            lsConfigService.requestConfigurationPaths(configurationPaths -> {
                if (project.isDisposed()) {
                    return;
                }
                Path ruleFile = findRuleFile(check, configurationPaths);
                if (ruleFile == null) {
                    new NotificationHelper(project).showNotificationWithConfigurationActions(
                            "Rule file not found for: " + check,
                            NotificationType.WARNING
                    );
                    return;
                }
                VirtualFile vFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(ruleFile.toString());
                if (vFile == null) {
                    new NotificationHelper(project).showNotificationWithConfigurationActions(
                            "Rule file not found for: " + check,
                            NotificationType.WARNING
                    );
                    return;
                }
                FileEditorManager.getInstance(project).openFile(vFile, true);
            });
            return true;
        }
        return false;
    }

    @Override
    public String getDescription(@NotNull String ref, @NotNull Editor editor) {
        return null;
    }

    @Override
    public @NotNull String getDescriptionTitle(@NotNull String ref, @NotNull Editor editor) {
        return "Vale rule";
    }

    private static String extractPayload(String ref) {
        if (ref.startsWith(PREFIX)) {
            return ref.substring(PREFIX.length());
        }
        return ref;
    }

    private static @Nullable Path findRuleFile(@NotNull String check,
                                               @Nullable ValeConfigurationPaths configurationPaths) {
        String[] parts = check.trim().split("\\.", 2);
        if (parts.length != 2) {
            return null;
        }
        String styleName = parts[0].trim();
        String ruleName = parts[1].trim();

        if (styleName.isBlank() || ruleName.isBlank()) {
            return null;
        }

        if (configurationPaths == null || configurationPaths.paths().isEmpty()) {
            return null;
        }

        List<Path> stylesRoots = new ArrayList<>();
        for (String raw : configurationPaths.paths()) {
            if (raw == null) {
                continue;
            }
            String pathTrimmed = raw.trim();
            if (pathTrimmed.isBlank()) {
                continue;
            }
            try {
                stylesRoots.add(Paths.get(pathTrimmed));
            } catch (InvalidPathException ignore) {
                // Skip malformed entries from external tooling.
            }
        }

        if (stylesRoots.isEmpty()) {
            return null;
        }

        // Only check for .yml and not .yaml as vale also does it that way.
        // Source: Heads up! Section at https://vale.sh/docs/styles
        String ruleFile = ruleName + ".yml";
        for (Path root : stylesRoots) {
            Path candidate = root.resolve(styleName).resolve(ruleFile);
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}
