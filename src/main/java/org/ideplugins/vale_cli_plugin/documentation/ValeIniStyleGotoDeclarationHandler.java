package org.ideplugins.vale_cli_plugin.documentation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import ini4idea.lang.psi.IniProperty;
import org.ideplugins.vale_cli_plugin.service.ValeConfigurationPaths;
import org.ideplugins.vale_cli_plugin.service.ValeStylesCache;
import org.ideplugins.vale_cli_plugin.utils.NotificationHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ValeIniStyleGotoDeclarationHandler implements GotoDeclarationHandler {

    private final ValeDocumentationResolver resolver = new ValeDocumentationResolver();

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(
            @Nullable PsiElement sourceElement, int offset, @NotNull Editor editor) {

        if (sourceElement == null) return null;

        PsiFile file = sourceElement.getContainingFile();
        if (file == null || !ValeDocumentationResolver.isValeIniFile(file.getName())) return null;

        IniProperty property = PsiTreeUtil.getParentOfType(sourceElement, IniProperty.class, false);
        if (property == null || !ValeDocumentationResolver.STYLE_PROPERTIES.contains(property.getName())) return null;

        // Only activate when the cursor is in the value area, not on the key
        PsiElement nameElement = property.getNameElement();
        if (nameElement != null && isOffsetInside(nameElement.getTextRange(), offset)) return null;

        String styleName = extractStyleAtOffset(property, offset);
        if (styleName == null || styleName.isBlank()) return null;

        Project project = editor.getProject();
        if (project == null || project.isDisposed()) return null;

        if (ValeDocumentationResolver.VALE_BUILTIN_STYLE.equals(styleName)) {
            String markdown = resolver.resolveForBuiltinValeStyle();
            LightVirtualFile vFile = new LightVirtualFile("Vale (built-in style).md",
                    PlainTextFileType.INSTANCE, markdown);
            vFile.setWritable(false);
            PsiFile psiFile = PsiManager.getInstance(project).findFile(vFile);
            return psiFile != null ? new PsiElement[]{psiFile} : PsiElement.EMPTY_ARRAY;
        }

        ValeConfigurationPaths configPaths = ValeStylesCache.getInstance(project).getCachedPaths();
        Path styleFolder = findStyleFolder(styleName, configPaths);

        if (styleFolder == null) {
            notifyStyleNotFound(project, styleName);
            return PsiElement.EMPTY_ARRAY;
        }

        VirtualFile vFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(styleFolder.toString());
        if (vFile == null || !vFile.isDirectory()) {
            notifyStyleNotFound(project, styleName);
            return PsiElement.EMPTY_ARRAY;
        }

        PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(vFile);
        return psiDirectory != null ? new PsiElement[]{psiDirectory} : PsiElement.EMPTY_ARRAY;
    }

    private void notifyStyleNotFound(@NotNull Project project, @NotNull String styleName) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (!project.isDisposed()) {
                new NotificationHelper(project).showNotificationWithConfigurationActions(
                        "Style folder not found: " + styleName,
                        NotificationType.WARNING
                );
            }
        });
    }

    private boolean isOffsetInside(@Nullable TextRange range, int offset) {
        return range != null && range.containsOffset(offset);
    }

    /**
     * Extracts the style name token from the comma-separated BasedOnStyles value at the given offset.
     * For example, given {@code BasedOnStyles = Google, Vale, RedHat} and an offset pointing at "Vale",
     * this returns {@code "Vale"}.
     */
    @Nullable
    static String extractStyleAtOffset(@NotNull IniProperty property, int offset) {
        String propertyText = property.getText();
        if (propertyText == null) return null;

        int equalsPos = propertyText.indexOf('=');
        if (equalsPos < 0) return null;

        String afterEquals = propertyText.substring(equalsPos + 1);
        // Preserve leading whitespace offset so that positions align correctly
        int leadingSpaces = afterEquals.length() - afterEquals.stripLeading().length();
        String valueText = afterEquals.stripLeading();

        int propertyStart = property.getTextRange().getStartOffset();
        int valueStartAbsolute = propertyStart + equalsPos + 1 + leadingSpaces;
        int relativeOffset = offset - valueStartAbsolute;

        if (relativeOffset < 0 || relativeOffset > valueText.length()) return null;

        // Walk through comma-separated tokens and find which one contains relativeOffset
        int pos = 0;
        for (String rawToken : valueText.split(",", -1)) {
            int tokenEnd = pos + rawToken.length();
            if (relativeOffset >= pos && relativeOffset <= tokenEnd) {
                return rawToken.trim();
            }
            pos = tokenEnd + 1; // +1 for the comma
        }

        return null;
    }

    @Nullable
    private static Path findStyleFolder(@NotNull String styleName,
                                        @NotNull ValeConfigurationPaths configPaths) {
        for (String raw : configPaths.paths()) {
            if (raw == null || raw.isBlank()) continue;
            try {
                Path candidate = Paths.get(raw.trim()).resolve(styleName);
                if (Files.isDirectory(candidate)) {
                    return candidate;
                }
            } catch (InvalidPathException ignore) {
            }
        }
        return null;
    }
}
