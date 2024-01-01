package org.ideplugins.vale_cli_plugin.annotator;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import org.ideplugins.vale_cli_plugin.settings.ValePluginSettingsState;

public class ValeProblemHighlightFilter implements Condition<VirtualFile> {
    @Override
    public boolean value(VirtualFile virtualFile) {
        ValePluginSettingsState settings = ValePluginSettingsState.getInstance();
        String extension = virtualFile.getExtension();
        return extension != null && settings.extensions.contains(extension);
    }
}
