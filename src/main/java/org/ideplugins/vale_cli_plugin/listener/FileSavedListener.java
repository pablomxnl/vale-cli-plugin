package org.ideplugins.vale_cli_plugin.listener;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.BulkAwareDocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.util.messages.MessageBusConnection;
import org.ideplugins.vale_cli_plugin.actions.ActionHelper;
import org.ideplugins.vale_cli_plugin.exception.ValeCliExecutionException;
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor;
import org.ideplugins.vale_cli_plugin.service.ValeIssuesReporter;
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState;
import org.jetbrains.annotations.NotNull;
import org.zeroturnaround.exec.ProcessResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

@Service(Service.Level.PROJECT)
final public class FileSavedListener implements Disposable, FileDocumentManagerListener, BulkAwareDocumentListener.Simple {

    private static final Logger LOGGER = Logger.getInstance(FileSavedListener.class);

    private final @NotNull Project myProject;
    private final ValePluginSettingsState settings;

    private final ValeCliExecutor cliExecutor;
    private final ValeIssuesReporter reporter;
    private final WolfTheProblemSolver problemSolver;

    public FileSavedListener(@NotNull Project project) {
        myProject = project;
        settings = ValePluginSettingsState.getInstance();
        cliExecutor = ValeCliExecutor.getInstance(myProject);
        reporter = myProject.getService(ValeIssuesReporter.class);
        problemSolver = WolfTheProblemSolver.getInstance(myProject);
    }

    public static FileSavedListener getInstance(Project project) {
        return project.getService(FileSavedListener.class);
    }


    private void writeSyncedFile(Document doc, Path tmp) throws IOException {
        try (BufferedWriter out = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            out.write(doc.getText());
        } catch (IOException ex) {
            LOGGER.error("There was a problem while preparing a temp file.", ex);
            throw ex;
        }
    }

    @Override
    public void dispose() {

    }

    @Override
    public void beforeDocumentChange(@NotNull Document document) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        TypedHandler handler = TypedHandlerDelegate.EP_NAME.findExtension(TypedHandler.class);
        if (handler != null && file != null && file.getExtension() != null
                && reporter.hasIssuesForFile(file.getPath())
                && settings.areSettingsValid().getKey()
                && settings.extensions.contains(file.getExtension())) {
            if (handler.isEditorIdle(file.getPath())) {
                try {
                    executeValeAfterChange(document);
                } catch (ValeCliExecutionException e) {
                    ActionHelper.handleError(myProject, e);
                }
            }
        }
    }

    private void executeValeAfterChange(Document document) throws ValeCliExecutionException {
        VirtualFile original = FileDocumentManager.getInstance().getFile(document);
        Objects.requireNonNull(original);
        AtomicReference<ValeCliExecutionException> error = new AtomicReference<>();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                syncFileAndExecuteVale(document, original);
            } catch (IOException ex) {
                error.set(new ValeCliExecutionException(ex));
            } catch (ValeCliExecutionException e) {
                error.set(e);
            }
        });
        if (error.get() != null) {
            throw error.get();
        }
    }

    private void syncFileAndExecuteVale(Document document, VirtualFile original)
            throws IOException, ValeCliExecutionException {
        Path tmp = null;
        try {
            tmp = Files.createTempFile(null, "." + original.getExtension());
            writeSyncedFile(document, tmp);
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(tmp.toString());
            if (file != null) {
                Future<ProcessResult> future = cliExecutor.executeValeCliOnFile(file).getFuture();
                Map<String, List<JsonObject>> results = cliExecutor.parseValeJsonResponse(future, 6);
                List<JsonObject> resultsFile = results.get(file.getPath());
                reporter.remove(file.getPath());
                problemSolver.clearProblems(original);
                reporter.updateIssuesForFile(original.getPath(), resultsFile);
            }
        } finally {
            try {
                if (tmp != null) {
                    Files.delete(tmp);
                }
            } catch (IOException e) {
                LOGGER.warn("Unable to delete tmp file", e);
            }
        }
    }

    public void activate() {
        MessageBusConnection connection = myProject.getMessageBus().connect(this);
        connection.subscribe(FileDocumentManagerListener.TOPIC, this);
    }

}
