package org.ideplugins.vale_cli_plugin.annotator;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.util.DocumentUtil;
import com.intellij.util.FileContentUtil;
import com.intellij.util.IncorrectOperationException;
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Intention action that fetches Vale suggestions on demand.
 */
public class ValeLazyFixQuickFix extends BaseIntentionAction {

    private final String actionName;
    private final String check;
    private final String match;
    private final TextRange range;
    private final String label;
    private final boolean alwaysShowChooser;

    public ValeLazyFixQuickFix(@NotNull ValeProblem problem,
                               @NotNull TextRange range,
                               boolean alwaysShowChooser) {
        ValeAction action = problem.action();
        this.actionName = action == null || action.name() == null ? "" : action.name();
        this.check = problem.check() == null ? "" : problem.check();
        this.match = problem.match() == null ? "" : problem.match();
        this.range = range;
        this.label = ValeActionDescriptionHelper.buildLazyLabel(problem);
        this.alwaysShowChooser = alwaysShowChooser;
    }

    @Override
    public @NotNull String getText() {
        return label;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Vale fix";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        if (editor == null || actionName.isBlank()) {
            return;
        }
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Vale: Fetching suggestions", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                ValeQuickFixHelper.FixTarget target = ValeQuickFixHelper.findFixTarget(project, editor, file,
                        actionName, check, match, range);
                if (target == null) {
                    return;
                }
                List<String> suggestions = ValeCliExecutor.getInstance(project).runFix(target.problem());
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (editor.isDisposed()) {
                        return;
                    }
                    applySuggestions(project, editor, target.range(), suggestions);
                });
            }
        });
    }

    private void applySuggestions(@NotNull Project project,
                                  @NotNull Editor editor,
                                  @NotNull TextRange targetRange,
                                  @NotNull List<String> suggestions) {
        Document document = editor.getDocument();
        if (!DocumentUtil.isValidOffset(targetRange.getStartOffset(), document)
                || !DocumentUtil.isValidOffset(targetRange.getEndOffset(), document)) {
            return;
        }
        String currentText = document.getText(targetRange);
        String displayText = buildInlineDisplay(currentText);
        Set<String> unique = new LinkedHashSet<>();
        for (String suggestion : suggestions) {
            if (suggestion == null) {
                continue;
            }
            unique.add(suggestion);
        }
        if (unique.isEmpty()) {
            return;
        }
        if (unique.size() == 1 && !alwaysShowChooser) {
            applyReplacement(project, editor, targetRange, unique.iterator().next());
            return;
        }
        List<SuggestionOption> options = new ArrayList<>();
        if (unique.contains("")) {
            options.add(new SuggestionOption("Empty string (removes '" + displayText + "')", ""));
        }
        for (String suggestion : unique) {
            if (suggestion.isEmpty()) {
                continue;
            }
            options.add(new SuggestionOption(buildInlineDisplay(suggestion), suggestion));
        }
        JBPopupFactory.getInstance()
                .createPopupChooserBuilder(options)
                .setTitle("Replace '" + displayText + "' with")
                .setItemChosenCallback(option -> applyReplacement(project, editor, targetRange, option.replacement()))
                .createPopup()
                .showInBestPositionFor(editor);
    }

    /**
     * Applies the replacement and then forces PSI/analysis refresh so annotations update immediately.
     */
    private void applyReplacement(@NotNull Project project,
                                  @NotNull Editor editor,
                                  @NotNull TextRange targetRange,
                                  @NotNull String replacement) {
        Document document = editor.getDocument();
        int start = targetRange.getStartOffset();
        int end = targetRange.getEndOffset();
        if (!DocumentUtil.isValidOffset(start, document)
                || !DocumentUtil.isValidOffset(end, document)) {
            return;
        }
        PsiDocumentManager psiManager = PsiDocumentManager.getInstance(project);
        WriteCommandAction.runWriteCommandAction(project,
                () -> document.replaceString(start, end, replacement));

        // Commit ensures PSI is up to date for analysis and navigation.
        psiManager.commitDocument(document);


        // Restart analysis on the EDT after PSI is stable to refresh highlights.
        ApplicationManager.getApplication().invokeLater(() -> {
            PsiFile psiFile = psiManager.getPsiFile(document);
            if (psiFile != null && !project.isDisposed()) {
                DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
            }
        });
    }

    private static String buildInlineDisplay(@NotNull String value) {
        return value
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private record SuggestionOption(@NotNull String label, String replacement) {
        @Override
        public @NotNull String toString() {
            return label;
        }
    }
}
