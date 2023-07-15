package org.ideplugins.vale_cli_plugin.annotator;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.util.FileContentUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class ValeReplaceQuickFix extends BaseIntentionAction {

    private final String term;
    private final String replacement;

    private final TextRange range;

    public ValeReplaceQuickFix(final String theTerm, final String theReplacement, final TextRange theRange){
        term = theTerm;
        replacement = theReplacement;
        range= theRange;
    }

    @Override
    public @NotNull String getText() {
        return String.format("Replace %s with %s", term, replacement);
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
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        Document document = editor.getDocument();
        WriteCommandAction.runWriteCommandAction(project, ()-> document.replaceString(range.getStartOffset(), range.getEndOffset(), replacement));
        ApplicationManager.getApplication().runReadAction(FileContentUtil::reparseOpenedFiles);
    }
}
