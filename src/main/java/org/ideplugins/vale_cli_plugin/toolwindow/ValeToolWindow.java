package org.ideplugins.vale_cli_plugin.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;


import static com.intellij.openapi.wm.ToolWindowType.DOCKED;

public class ValeToolWindow implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ValeToolWindowContent valeToolWindowContent = new ValeToolWindowContent(project);
        Content content = toolWindow.getContentManager().getFactory()
                .createContent(valeToolWindowContent, "Vale Results", true);
        content.setDisplayName("Vale Results");
        toolWindow.setTitle("Vale Console");
        toolWindow.setType(DOCKED, null);
        content.setDisposer(valeToolWindowContent);
        ContentManager manager = toolWindow.getContentManager();
        manager.addContent(content);
    }

}
