package org.ideplugins.vale_cli_plugin.annotator;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.util.DocumentUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class ValeRemoveQuickFix extends BaseIntentionAction {

    private final String term;

    private final TextRange range;

    public ValeRemoveQuickFix(final String theTerm, final TextRange theRange){
        term = theTerm;
        range= theRange;
    }

    @Override
    public @NotNull String getText() {
        return String.format("Remove %s", term);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Vale fix";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        Document document = editor.getDocument();
        return  DocumentUtil.isValidOffset(range.getStartOffset(), document) &&
                DocumentUtil.isValidOffset(range.getEndOffset(), document);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        Document document = editor.getDocument();
        WriteCommandAction.runWriteCommandAction(project, ()-> document.replaceString(range.getStartOffset(), range.getEndOffset(), ""));
    }
}
