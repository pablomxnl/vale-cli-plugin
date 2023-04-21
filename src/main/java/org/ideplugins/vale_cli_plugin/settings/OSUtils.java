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

    public static String valeVersion() {
        String version = "";
        List<String> versionCommand = (SystemInfo.isWindows) ? List.of("cmd.exe","/c", "vale.exe --version" ) :
                List.of(SHELL, "-c", "vale --version");
        try {
            StartedProcess startedProcess = new ProcessExecutor()
                    .command(versionCommand)
                    .readOutput(true)
                    .start();
            Future<ProcessResult> future = startedProcess.getFuture();
            ProcessResult result = future.get();
            if (result.getExitValue() == 0) {
                version = result.outputUTF8().trim();
                LOG.info("Vale version found:" +  version);
            } else {
                LOG.info("Version command Exit value not zero: " + result.outputUTF8());
                LOG.info("Version Commands: " + versionCommand);
            }
        } catch (IOException | InterruptedException | ExecutionException exception) {
            LOG.info("Unable to find vale version ", exception);
            LOG.info("Version Commands: " + versionCommand);
        }
        return version;
    }

    public static String findValeBinaryPath() {
        String path = "";
        List<String> whichCommand = (SystemInfo.isWindows) ? List.of("cmd.exe", "/c", "where vale.exe") :
                 List.of(SHELL, "-ilc", "which vale");
        try {
            StartedProcess startedProcess = new ProcessExecutor()
                    .command(whichCommand)
                    .readOutput(true)
                    .start();
            Future<ProcessResult> future = startedProcess.getFuture();
            ProcessResult result = future.get();
            if (result.getExitValue() == 0) {
                path = result.outputUTF8().trim();
                LOG.info("Vale detected at " +  path);
            } else {
                LOG.info("Exit value not zero: " + result.outputUTF8());
                LOG.info("findVale Commands: " + whichCommand);
            }
        } catch (IOException | InterruptedException | ExecutionException exception) {
            LOG.info("Unable to find vale binary", exception);
            LOG.info("findVale Commands: " + whichCommand);
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
