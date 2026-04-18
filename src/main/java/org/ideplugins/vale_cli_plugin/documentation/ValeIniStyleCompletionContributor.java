package org.ideplugins.vale_cli_plugin.documentation;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import ini4idea.lang.psi.IniProperty;
import org.ideplugins.vale_cli_plugin.service.ValeConfigurationPaths;
import org.ideplugins.vale_cli_plugin.service.ValeStylesCache;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class ValeIniStyleCompletionContributor extends CompletionContributor {

    public ValeIniStyleCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new StyleCompletionProvider());
    }

    private static final class StyleCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            PsiElement position = parameters.getPosition();
            PsiFile file = position.getContainingFile();
            if (file == null || !ValeDocumentationResolver.isValeIniFile(file.getName())) return;

            IniProperty property = PsiTreeUtil.getParentOfType(position, IniProperty.class, false);
            if (property == null || !ValeDocumentationResolver.STYLE_PROPERTIES.contains(property.getName())) return;

            // Don't complete on the key, only on the value side
            PsiElement nameElement = property.getNameElement();
            if (nameElement != null && nameElement.getTextRange().containsOffset(parameters.getOffset())) return;

            String prefix = extractPrefix(parameters);
            CompletionResultSet prefixedResult = result.withPrefixMatcher(prefix);

            Project project = position.getProject();
            ValeConfigurationPaths configPaths = ValeStylesCache.getInstance(project).getCachedPaths();

            for (String styleName : findAvailableStyles(configPaths)) {
                prefixedResult.addElement(LookupElementBuilder.create(styleName));
            }
        }

        /**
         * Extracts the prefix for the current style token, i.e. the text typed after the last comma
         * up to the cursor, stripping the IntelliJ completion dummy identifier.
         */
        private String extractPrefix(@NotNull CompletionParameters parameters) {
            PsiElement position = parameters.getPosition();
            String elementText = position.getText();

            // Strip the dummy identifier IntelliJ appends at the caret
            int dummyPos = elementText.indexOf(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED);
            String textWithoutDummy = dummyPos >= 0 ? elementText.substring(0, dummyPos) : elementText;

            // Restrict to the portion before the caret within this element
            int localOffset = parameters.getOffset() - position.getTextRange().getStartOffset();
            String textUpToCaret = textWithoutDummy.substring(0, Math.min(localOffset, textWithoutDummy.length()));

            // The prefix is whatever comes after the last comma
            int lastComma = textUpToCaret.lastIndexOf(',');
            return lastComma >= 0 ? textUpToCaret.substring(lastComma + 1).stripLeading() : textUpToCaret.stripLeading();
        }

        @NotNull
        private Set<String> findAvailableStyles(@NotNull ValeConfigurationPaths configPaths) {
            Set<String> styles = new TreeSet<>();
            for (String raw : configPaths.paths()) {
                if (raw == null || raw.isBlank()) continue;
                try {
                    Path root = Paths.get(raw.trim());
                    if (!Files.isDirectory(root)) continue;
                    try (Stream<Path> entries = Files.list(root)) {
                        entries.filter(Files::isDirectory)
                                .map(p -> p.getFileName().toString())
                                .forEach(styles::add);
                    } catch (IOException ignore) {
                    }
                } catch (InvalidPathException ignore) {
                }
            }
            return styles;
        }
    }
}
