package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;

import java.io.File;
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

    public static String valeVersion(String fullPath){
        String version = "";
        List<String> versionCommand = wrapCommandWithShellEnv(fullPath + " --version");
        LOG.info("Executing version command: " + versionCommand);
        try {
            GeneralCommandLine cmd = new GeneralCommandLine(versionCommand.toArray(new String[0]));
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

    public static String findValeBinaryPath() {
        String path = "";
        String whichCommand = (SystemInfo.isWindows) ? "where vale.exe" : "which vale";
        List<String> command = wrapCommandWithShellEnv(whichCommand);
        LOG.info("Executing whichWhere command: " + String.join(" ", command));
        try {
            GeneralCommandLine cmd = new GeneralCommandLine(command.toArray(new String[0]));
            CapturingProcessHandler handler = new CapturingProcessHandler(cmd);
            ProcessOutput output = handler.runProcess();
            if (output.getExitCode() == 0) {
                path = output.getStdout().trim();
                LOG.info("Vale detected at " +  path);
            } else {
                LOG.info("Exit value not zero: " + output.getStdout() + " " + output.getStderr());
            }
        } catch (ExecutionException exception) {
            LOG.info("Unable to find vale binary", exception);
        }
        return path;
    }

    public static String normalizeFilePath(String filePath) {
        String tempPath = filePath;
        if (SystemInfo.isWindows && !filePath.contains(File.separator)) {
            tempPath = filePath.replace('/', File.separatorChar);
        }
        return tempPath;
    }
}
