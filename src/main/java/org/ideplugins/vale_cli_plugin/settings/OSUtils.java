package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class OSUtils {

    private static final Logger LOG = Logger.getInstance(OSUtils.class);
    private static final String SHELL = Optional.ofNullable(System.getenv("SHELL")).orElse("/bin/sh");

    public static List<String> wrapCommandWithShellEnv(String command){
        return (SystemInfo.isWindows) ? List.of("cmd.exe","/c", command ) :
                List.of(SHELL, inCI()? "-lc" : "-ilc", command);
    }

    private static boolean inCI() {
        return Optional.ofNullable(System.getenv("CI_PROJECT_DIR")).isPresent();
    }

    public static String valeVersion(String valeBinaryPath){
        String version = "";
        String normalizedPath = Optional.ofNullable(valeBinaryPath).orElse("").trim();
        if (normalizedPath.isBlank()) {
            LOG.info("Skipping version command because vale path is empty");
            return version;
        }
        try {
            GeneralCommandLine cmd = new GeneralCommandLine(normalizedPath).withParameters("--version");
            LOG.info("Executing version command: " + cmd.getCommandLineString());
            CapturingProcessHandler handler = new CapturingProcessHandler(cmd);
            ProcessOutput output = handler.runProcess();
            if (output.getExitCode() == 0) {
                version = output.getStdout().replaceAll("vale version ", "").trim();
                LOG.info("Vale version found:" +  version);
            } else {
                LOG.info("Version command Exit value not zero: " + output.getStdout() + " " + output.getStderr());
            }
        } catch (ExecutionException exception) {
            LOG.info("Unable to find vale version ", exception);
        }
        return version;
    }

    public static synchronized String findValeBinaryPath() {
        String path = "";
        String whichCommand = (SystemInfo.isWindows) ? "where vale.exe" : "which vale";
        List<String> command = wrapCommandWithShellEnv(whichCommand);
        LOG.info("Executing whichWhere command: " + String.join(" ", command));
        try {
            GeneralCommandLine cmd = new GeneralCommandLine(command);
            CapturingProcessHandler handler = new CapturingProcessHandler(cmd);
            ProcessOutput output = handler.runProcess();
            if (output.getExitCode() == 0) {
                path = extractExecutablePath(output.getStdout());
                if (!path.isBlank()) {
                    LOG.info("Vale detected at " + path);
                } else {
                    LOG.info("No executable vale path found in output: " + output.getStdout());
                }
            } else {
                LOG.info("Exit value not zero: " + output.getStdout() + " " + output.getStderr());
            }
        } catch (ExecutionException exception) {
            LOG.info("Unable to find vale binary", exception);
        }
        return path;
    }

    public static String extractExecutablePath(String stdout) {
        if (stdout == null || stdout.isBlank()) {
            return "";
        }
        return stdout.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(OSUtils::isExecutablePath)
                .findFirst()
                .orElse("");
    }

    private static boolean isExecutablePath(String candidate) {
        try {
            Path path = Path.of(candidate);
            return Files.isRegularFile(path) && Files.isExecutable(path);
        } catch (InvalidPathException ignored) {
            return false;
        }
    }

}
