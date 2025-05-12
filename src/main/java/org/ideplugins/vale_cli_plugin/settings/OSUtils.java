package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class OSUtils {

    private static final Logger LOG = Logger.getInstance(OSUtils.class);
    private static final String SHELL = Optional.ofNullable(System.getenv("SHELL")).orElse("/bin/sh");

    public static List<String> wrapCommandWithShellEnv(String command){
        return (SystemInfo.isWindows) ? List.of("cmd.exe","/c", command ) :
                List.of(SHELL, inCI()? "-lc" : "-ilc", command);
    }

    private static boolean inCI() {
        return  Optional.ofNullable(System.getenv("CI_PROJECT_DIR")).isPresent();
    }

    public static String valeVersion(String fullPath){
        String version = "";
        List<String> versionCommand = wrapCommandWithShellEnv( fullPath + " --version");
        LOG.info("Executing version command: " + versionCommand);
        try {
            StartedProcess startedProcess = new ProcessExecutor()
                    .command(versionCommand)
                    .readOutput(true)
                    .start();
            Future<ProcessResult> future = startedProcess.getFuture();
            ProcessResult result = future.get();
            if (result.getExitValue() == 0) {
                version = result.outputUTF8().replaceAll("vale version ", "").trim();
                LOG.info("Vale version found:" +  version);
            } else {
                LOG.info("Version command Exit value not zero: " + result.outputUTF8());
            }
        } catch (IOException | InterruptedException | ExecutionException exception) {
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
            StartedProcess startedProcess = new ProcessExecutor()
                    .command(command)
                    .readOutput(true)
                    .start();
            Future<ProcessResult> future = startedProcess.getFuture();
            ProcessResult result = future.get();
            if (result.getExitValue() == 0) {
                path = result.outputUTF8().trim();
                LOG.info("Vale detected at " +  path);
            } else {
                LOG.info("Exit value not zero: " + result.outputUTF8());
            }
        } catch (IOException | InterruptedException | ExecutionException exception) {
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
