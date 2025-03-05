package org.ideplugins.vale_cli_plugin.toolwindow;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;


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

    private ActionToolbar createToolbar() {
        DefaultActionGroup vale = new DefaultActionGroup();
        vale.add(ActionManager.getInstance().getAction("org.ideplugins.vale_cli_plugin.actions.ValeFeedbackAction"));
        vale.add(ActionManager.getInstance().getAction("org.ideplugins.vale_cli_plugin.actions.ValeBugReportAction"));
        return ActionManager.getInstance()
                .createActionToolbar(ActionPlaces.TOOLBAR, vale, true);
    }

    public ConsoleView getConsoleView() {
        return consoleView;
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