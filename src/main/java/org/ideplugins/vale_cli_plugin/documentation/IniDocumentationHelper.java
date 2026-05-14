package org.ideplugins.vale_cli_plugin.documentation;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import ini4idea.lang.psi.IniProperty;
import ini4idea.lang.psi.IniSectionName;
import org.ideplugins.vale_cli_plugin.service.ValeConfigurationPaths;
import org.ideplugins.vale_cli_plugin.service.ValeStylesCache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class IniDocumentationHelper {

    public record IniDoc(String presentableText, String markdown) {}

    public static IniDoc findIniDocumentationRequestAt(PsiFile psiFile, int offset, ValeDocumentationResolver resolver) {
        int safeOffset = clampOffset(offset, psiFile.getTextLength());
        PsiElement element = findLeafElement(psiFile, safeOffset);
        if (element == null) {
            return null;
        }

        IniProperty property = PsiTreeUtil.getParentOfType(element, IniProperty.class, false);
        if (property != null && isOffsetInside(property.getNameElement(), offset)) {
            String propertyName = property.getName();
            String markdown = resolver.resolveForIniKey(propertyName);
            if (markdown != null) {
                return new IniDoc(propertyName, markdown);
            }
        }

        IniSectionName sectionName = PsiTreeUtil.getParentOfType(element, IniSectionName.class, false);
        if (sectionName != null && isOffsetInside(sectionName, offset)) {
            String markdown = resolver.resolveForIniSection(sectionName.getText());
            if (markdown != null) {
                return new IniDoc(sectionName.getText(), markdown);
            }
        }

        if (property != null && ValeDocumentationResolver.STYLE_PROPERTIES.contains(property.getName())) {
            PsiElement nameEl = property.getNameElement();
            if (nameEl == null || !isOffsetInside(nameEl, offset)) {
                String styleName = ValeIniStyleGotoDeclarationHandler.extractStyleAtOffset(property, offset);
                if (styleName != null && !styleName.isBlank()) {
                    String markdown = resolveStyleDocumentation(styleName, psiFile.getProject(), resolver);
                    if (markdown != null) {
                        return new IniDoc(styleName, markdown);
                    }
                }
            }
        }

        return null;
    }

    private static String resolveStyleDocumentation(String styleName, Project project, ValeDocumentationResolver resolver) {
        if (ValeDocumentationResolver.VALE_BUILTIN_STYLE.equals(styleName)) {
            return resolver.resolveForBuiltinValeStyle();
        }
        ValeConfigurationPaths paths = ValeStylesCache.getInstance(project).getCachedPaths();
        return findStyleReadme(styleName, paths);
    }

    private static String findStyleReadme(String styleName, ValeConfigurationPaths paths) {
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

    private static PsiElement findLeafElement(PsiFile psiFile, int offset) {
        if (psiFile.getTextLength() == 0) {
            return null;
        }
        int safeOffset = clampOffset(offset, psiFile.getTextLength());
        return psiFile.findElementAt(safeOffset);
    }

    private static boolean isOffsetInside(PsiElement element, int offset) {
        if (element == null) {
            return false;
        }
        var range = element.getTextRange();
        return range != null && range.containsOffset(offset);
    }

    private static int clampOffset(int offset, int textLength) {
        if (textLength <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(offset, textLength - 1));
    }
}

