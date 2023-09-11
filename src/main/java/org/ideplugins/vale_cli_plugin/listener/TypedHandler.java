package org.ideplugins.vale_cli_plugin.listener;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TypedHandler extends TypedHandlerDelegate {


    private final Map<String, Instant> keyStrokesTimeStamps;
    private ValePluginSettingsState settings;

    public TypedHandler() {
        keyStrokesTimeStamps = new ConcurrentHashMap<>();
    }

    @Override
    public @NotNull Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (settings==null){
            settings = ValePluginSettingsState.getInstance();
        }
        Optional.ofNullable(file.getVirtualFile()).ifPresent(virtualFile -> {
            if (virtualFile.getExtension()!=null && settings.extensions.contains(virtualFile.getExtension())) {
                Instant begin = Instant.now();
                keyStrokesTimeStamps.put(file.getVirtualFile().getPath(), begin);
            }
        });
        return Result.CONTINUE;
    }


    public boolean isEditorIdle(String filePath){
        boolean isIdle = false;
        Instant begin = keyStrokesTimeStamps.get(filePath);
        if (begin != null){
            long duration = Duration.between(begin, Instant.now()).toMillis();
            if (duration >= 2000){
                isIdle = true;
                keyStrokesTimeStamps.put(filePath, Instant.now());
            }
        }
        return isIdle;
    }
}
