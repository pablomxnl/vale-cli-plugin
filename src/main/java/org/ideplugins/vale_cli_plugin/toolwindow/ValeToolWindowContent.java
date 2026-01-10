package org.ideplugins.vale_cli_plugin.toolwindow;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;


public class ValeToolWindowContent extends SimpleToolWindowPanel implements Disposable {
    private ConsoleView consoleView;
    private Project project;

    public ValeToolWindowContent(@NotNull Project project) {
        super(true);
        this.project = project;
        ActionToolbar actionToolbar = createToolbar();
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(this.project).getConsole();
        add(consoleView.getComponent());
        actionToolbar.setTargetComponent(null);
        setToolbar(actionToolbar.getComponent());
    }

    private @NotNull ActionToolbar createToolbar() {
        DefaultActionGroup vale = new DefaultActionGroup();
        ActionManager instance = ActionManager.getInstance();
        vale.add(instance.getAction("org.ideplugins.vale_cli_plugin.actions.ValeFeedbackAction"));
        vale.add(instance.getAction("org.ideplugins.vale_cli_plugin.actions.ValeBugReportAction"));
        vale.add(instance.getAction("org.ideplugins.vale_cli_plugin.actions.ValeProjectSettingsAction"));
        AnAction settings = instance.getAction("org.ideplugins.vale_cli_plugin.actions.ValeSettingsAction");
        JBColor color = new JBColor(0x3577E3, 0x3577E3);
        Icon icon = IconUtil.colorize(Objects.requireNonNull(settings.getTemplatePresentation().getIcon()), color);
        settings.getTemplatePresentation().setIcon(icon);
        vale.add(settings);
        AnAction syncAction = instance.getAction("org.ideplugins.vale_cli_plugin.actions.ValeSyncAction");
        syncAction.getTemplatePresentation().setIcon(IconUtil.colorize(Objects.requireNonNull(syncAction.getTemplatePresentation().getIcon()), color));
        vale.add(syncAction);
        return instance
                .createActionToolbar(ActionPlaces.TOOLBAR, vale, true);
    }

    @Override
    public void dispose() {
        consoleView.clear();
        removeAll();
        Disposer.dispose(consoleView);
        project = null;
        consoleView = null;
    }
}