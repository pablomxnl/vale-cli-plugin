package org.ideplugins.vale_cli_plugin.documentation;

import com.intellij.markdown.utils.MarkdownToHtmlConverterKt;
import com.intellij.model.Pointer;
import com.intellij.openapi.util.TextRange;
import com.intellij.platform.backend.documentation.DocumentationResult;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.DocumentationTargetProvider;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.List;

// TargetPresentation is marked Experimental in 252/253 SDKs and becomes stable in 261.
@SuppressWarnings({"UnstableApiUsage"})
public class ValeDocumentationTargetProvider implements DocumentationTargetProvider {

    private final ValeDocumentationResolver resolver = new ValeDocumentationResolver();

    @Override
    public @NotNull List<? extends DocumentationTarget> documentationTargets(@NotNull PsiFile psiFile, int offset) {
        String fileName = psiFile.getName();
        String fileText = psiFile.getText();
        String markdown = null;
        String presentableText = "Vale";

        if (ValeDocumentationResolver.isValeIniFile(fileName)) {
            int safeOffset = Math.max(0, Math.min(offset, fileText.length()));
            markdown = resolver.resolveForIniOffset(fileText, safeOffset);
            presentableText = "Vale setting";
        } else if (ValeDocumentationResolver.isValeRuleFile(fileName)) {
            PsiElement keyElement = findYamlKeyElementAtOffset(psiFile, offset);
            if (keyElement != null) {
                String rawRuleKey = keyElement.getText();
                markdown = resolver.resolveForRuleKey(rawRuleKey, fileText);
                presentableText = rawRuleKey;
            } else {
                int safeOffset = Math.max(0, Math.min(offset, fileText.length()));
                markdown = resolver.resolveForRuleOffset(fileText, safeOffset);
                presentableText = "Vale rule setting";
            }
        }

        if (markdown == null || markdown.isBlank()) {
            return List.of();
        }
        String html = MarkdownToHtmlConverterKt.convertMarkdownToHtml(markdown);
        return List.of(new ValeDocumentationTarget(presentableText, html));
    }

    private @Nullable PsiElement findYamlKeyElementAtOffset(@NotNull PsiFile psiFile, int offset) {
        PsiElement atOffset = findLeafElement(psiFile, offset);
        PsiElement beforeOffset = findLeafElement(psiFile, offset - 1);
        YAMLKeyValue keyValue = PsiTreeUtil.getParentOfType(atOffset, YAMLKeyValue.class, false);
        if (keyValue == null) {
            keyValue = PsiTreeUtil.getParentOfType(beforeOffset, YAMLKeyValue.class, false);
        }
        if (keyValue == null) {
            return null;
        }
        PsiElement keyElement = keyValue.getKey();
        if (keyElement == null) {
            return null;
        }
        TextRange keyRange = keyElement.getTextRange();
        if (keyRange == null) {
            return null;
        }
        int safeOffset = clampOffset(offset, psiFile.getTextLength());
        if (!keyRange.containsOffset(safeOffset) && !keyRange.containsOffset(Math.max(0, safeOffset - 1))) {
            return null;
        }
        return keyElement;
    }

    private @Nullable PsiElement findLeafElement(@NotNull PsiFile psiFile, int offset) {
        if (psiFile.getTextLength() == 0) {
            return null;
        }
        int safeOffset = clampOffset(offset, psiFile.getTextLength());
        return psiFile.findElementAt(safeOffset);
    }

    private int clampOffset(int offset, int textLength) {
        if (textLength <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(offset, textLength - 1));
    }

    private record ValeDocumentationTarget(@NotNull String presentableText,
                                           @NotNull String htmlDocumentation) implements DocumentationTarget {


            @Override
            public @NotNull Pointer<? extends DocumentationTarget> createPointer() {
                return Pointer.hardPointer(new ValeDocumentationTarget(presentableText, htmlDocumentation));
            }

            @Override
            public @NotNull TargetPresentation computePresentation() {
                return TargetPresentation.builder(presentableText).presentation();
            }

            @Override
            public @NotNull String computeDocumentationHint() {
                return presentableText;
            }

            @Override
            public @NotNull DocumentationResult computeDocumentation() {
                return DocumentationResult.documentation(htmlDocumentation);
            }
        }
}
