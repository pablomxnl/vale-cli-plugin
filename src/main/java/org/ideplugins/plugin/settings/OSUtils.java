package org.ideplugins.plugin.settings;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class OSUtils {

    private static final Logger LOG = Logger.getInstance(OSUtils.class);
    private static final String SHELL = Optional.ofNullable(System.getenv("SHELL")).orElse("/bin/sh");

    public static String findValeBinaryPath() {
        String path = "";
        String[] whichCommand = (SystemInfo.isWindows) ? new String[]{"cmd.exe", "/c", "where vale.exe"} :
                (SystemInfo.isChromeOS) ? new String[]{SHELL, "-ic", "which vale"} :
                        new String[]{SHELL, "-c", "which vale"};
        try {
            Future<ProcessResult> future = new ProcessExecutor()
                    .command(whichCommand)
                    .readOutput(true)
                    .start().getFuture();
            ProcessResult result = future.get();
            if (result.getExitValue() == 0) {
                path = result.outputUTF8().trim();
            }
        } catch (IOException | InterruptedException | ExecutionException exception) {
            LOG.error("Unable to find vale binary", exception);
        }
        return path;
    }
}
