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
import com.intellij.util.messages.MessageBusConnection;
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
import java.util.Optional;
import java.util.concurrent.Future;

import static com.intellij.AppTopics.FILE_DOCUMENT_SYNC;

@Service(Service.Level.PROJECT)
final public class FileSavedListener implements Disposable, FileDocumentManagerListener, BulkAwareDocumentListener.Simple {

    private static final Logger LOGGER = Logger.getInstance(FileSavedListener.class);

    private final @NotNull Project myProject;
    private final ValePluginSettingsState settings;

    private final ValeCliExecutor cliExecutor;
    private final ValeIssuesReporter reporter;


    public FileSavedListener(@NotNull Project project) {
        myProject = project;
        settings = ApplicationManager.getApplication().getService(ValePluginSettingsState.class);
        cliExecutor = ValeCliExecutor.getInstance(myProject);
        reporter = myProject.getService(ValeIssuesReporter.class);
    }

    public static FileSavedListener getInstance(Project project) {
        return project.getService(FileSavedListener.class);
    }

    private static VirtualFile createSyncedFile(Document doc, Path tmp) throws IOException {
        try {
            try (BufferedWriter out = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                out.write(doc.getText());
            }
            return LocalFileSystem.getInstance().findFileByPath(tmp.toString());
        } catch (IOException ex) {
            LOGGER.error("There was a problem while preparing a temp file.", ex);
            throw ex;
        }
    }

    @Override
    public void dispose() {

    }

    @Override
    public void afterDocumentChange(@NotNull Document document) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        Optional.ofNullable(TypedHandlerDelegate.EP_NAME.findExtension(TypedHandler.class)).ifPresent(handler -> {
            if (file != null && file.getExtension() != null
                    && reporter.hasIssuesForFile(file.getPath())
                    && settings.areSettingsValid().getKey()
                    && settings.extensions.contains(file.getExtension())) {
                if (handler.isEditorIdle(file.getPath())) {
                    try {
                        executeValeAfterChange(document);
                    } catch (ValeCliExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void executeValeAfterChange(Document document) throws ValeCliExecutionException {
        VirtualFile original = FileDocumentManager.getInstance().getFile(document);
        LOGGER.info("executeValeAfterChange");
        Path tmp = null;
        try {
            tmp = Files.createTempFile(null, "." + original.getExtension());
            VirtualFile file = createSyncedFile(document, tmp);
            Future<ProcessResult> future = cliExecutor.executeValeCliOnFile(file).getFuture();
            Map<String, List<JsonObject>> results = cliExecutor.parseValeJsonResponse(future, 6);
            List<JsonObject> resultsFile = results.get(file.getPath());
            reporter.remove(file.getPath());
            reporter.updateIssuesForFile(original.getPath(), resultsFile);
        } catch (IOException ex) {
            throw new ValeCliExecutionException(ex);
        } finally {
            try {
                if (tmp != null) {
                    Files.delete(tmp);
                }
            } catch (IOException e) {
                LOGGER.info("unable to delete tmp file\n" + e.getMessage() );
            }
        }
    }

    public void activate() {
        MessageBusConnection connection = myProject.getMessageBus().connect(this);
        //FileDocumentManagerListener.TOPIC
        connection.subscribe(FILE_DOCUMENT_SYNC, this);
    }


}
