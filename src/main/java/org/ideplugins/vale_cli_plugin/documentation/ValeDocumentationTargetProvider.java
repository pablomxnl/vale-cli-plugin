package org.ideplugins.vale_cli_plugin.documentation;

import com.intellij.markdown.utils.MarkdownToHtmlConverterKt;
import com.intellij.model.Pointer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.platform.backend.documentation.DocumentationResult;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.DocumentationTargetProvider;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import ini4idea.lang.psi.IniProperty;
import ini4idea.lang.psi.IniSectionName;
import org.ideplugins.vale_cli_plugin.service.ValeConfigurationPaths;
import org.ideplugins.vale_cli_plugin.service.ValeStylesCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

// TargetPresentation is marked Experimental in 252/253 SDKs and becomes stable in 261.
@SuppressWarnings({"UnstableApiUsage"})
public class ValeDocumentationTargetProvider implements DocumentationTargetProvider {

    private final ValeDocumentationResolver resolver = new ValeDocumentationResolver();

    @Override
    public @NotNull List<? extends DocumentationTarget> documentationTargets(@NotNull PsiFile psiFile, int offset) {
        DocumentationRequest request = findDocumentationRequest(psiFile, offset);
        if (request == null || request.markdown().isBlank()) {
            return List.of();
        }
        String html = MarkdownToHtmlConverterKt.convertMarkdownToHtml(request.markdown());
        return List.of(new ValeDocumentationTarget(request.presentableText(), html));
    }

    private @Nullable DocumentationRequest findDocumentationRequest(@NotNull PsiFile psiFile, int offset) {
        String fileName = psiFile.getName();
        if (ValeDocumentationResolver.isValeIniFile(fileName)) {
            return findIniDocumentationRequest(psiFile, offset);
        }
        if (ValeDocumentationResolver.isValeRuleFile(fileName)) {
            return findYamlDocumentationRequest(psiFile, offset);
        }
        return null;
    }

    private @Nullable DocumentationRequest findIniDocumentationRequest(@NotNull PsiFile psiFile, int offset) {
        int safeOffset = clampOffset(offset, psiFile.getTextLength());
        DocumentationRequest request = findIniDocumentationRequestAt(psiFile, safeOffset);
        if (request != null || safeOffset == 0) {
            return request;
        }
        return findIniDocumentationRequestAt(psiFile, safeOffset - 1);
    }

    private @Nullable DocumentationRequest findIniDocumentationRequestAt(@NotNull PsiFile psiFile, int offset) {
        PsiElement element = findLeafElement(psiFile, offset);
        if (element == null) {
            return null;
        }

        IniProperty property = PsiTreeUtil.getParentOfType(element, IniProperty.class, false);
        if (property != null && isOffsetInside(property.getNameElement(), offset)) {
            String propertyName = property.getName();
            String markdown = resolver.resolveForIniKey(propertyName);
            if (markdown != null) {
                return new DocumentationRequest(propertyName, markdown);
            }
        }

        IniSectionName sectionName = PsiTreeUtil.getParentOfType(element, IniSectionName.class, false);
        if (sectionName != null && isOffsetInside(sectionName, offset)) {
            String markdown = resolver.resolveForIniSection(sectionName.getText());
            if (markdown != null) {
                return new DocumentationRequest(sectionName.getText(), markdown);
            }
        }

        if (property != null && ValeDocumentationResolver.STYLE_PROPERTIES.contains(property.getName())) {
            PsiElement nameEl = property.getNameElement();
            if (nameEl == null || !isOffsetInside(nameEl, offset)) {
                String styleName = ValeIniStyleGotoDeclarationHandler.extractStyleAtOffset(property, offset);
                if (styleName != null && !styleName.isBlank()) {
                    String markdown = resolveStyleDocumentation(styleName, psiFile.getProject());
                    if (markdown != null) {
                        return new DocumentationRequest(styleName, markdown);
                    }
                }
            }
        }

        return null;
    }

    private @Nullable String resolveStyleDocumentation(@NotNull String styleName, @NotNull Project project) {
        if (ValeDocumentationResolver.VALE_BUILTIN_STYLE.equals(styleName)) {
            return resolver.resolveForBuiltinValeStyle();
        }
        ValeConfigurationPaths paths = ValeStylesCache.getInstance(project).getCachedPaths();
        return findStyleReadme(styleName, paths);
    }

    private @Nullable String findStyleReadme(@NotNull String styleName,
                                              @NotNull ValeConfigurationPaths paths) {
        for (String rawPath : paths.paths()) {
            if (rawPath == null || rawPath.isBlank()) continue;
            try {
                Path styleDir = Paths.get(rawPath.trim()).resolve(styleName);
                if (!Files.isDirectory(styleDir)) continue;
                try (Stream<Path> entries = Files.list(styleDir)) {
                    Optional<Path> readme = entries
                            .filter(p -> p.getFileName().toString().equalsIgnoreCase("readme.md"))
                            .findFirst();
                    if (readme.isPresent()) {
                        return Files.readString(readme.get());
                    }
                } catch (IOException ignore) {
                }
            } catch (InvalidPathException ignore) {
            }
        }
        return null;
    }

    private @Nullable DocumentationRequest findYamlDocumentationRequest(@NotNull PsiFile psiFile, int offset) {
        PsiElement keyElement = findYamlKeyElementAtOffset(psiFile, offset);
        if (keyElement != null) {
            String rawRuleKey = keyElement.getText();
            String markdown = resolver.resolveForRuleKey(rawRuleKey, psiFile.getText());
            if (markdown != null) {
                return new DocumentationRequest(rawRuleKey, markdown);
            }
        }

        String fallbackRuleKey = resolver.findRuleKeyAtOffset(psiFile.getText(), clampOffset(offset, psiFile.getTextLength()));
        if (fallbackRuleKey == null) {
            return null;
        }
        String fallbackMarkdown = resolver.resolveForRuleKey(fallbackRuleKey, psiFile.getText());
        if (fallbackMarkdown == null) {
            return null;
        }
        return new DocumentationRequest(fallbackRuleKey, fallbackMarkdown);
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

    private boolean isOffsetInside(@Nullable PsiElement element, int offset) {
        if (element == null) {
            return false;
        }
        TextRange range = element.getTextRange();
        return range != null && range.containsOffset(offset);
    }

    private int clampOffset(int offset, int textLength) {
        if (textLength <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(offset, textLength - 1));
    }

    private record DocumentationRequest(@NotNull String presentableText,
                                        @NotNull String markdown) {
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
