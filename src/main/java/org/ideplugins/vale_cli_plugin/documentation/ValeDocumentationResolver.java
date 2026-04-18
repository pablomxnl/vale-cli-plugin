package org.ideplugins.vale_cli_plugin.documentation;

import com.intellij.openapi.util.TextRange;
import org.ideplugins.vale_cli_plugin.annotator.ValeRuleDescriptionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValeDocumentationResolver {

    public static final String VALE_BUILTIN_STYLE = "Vale";
    public static final Set<String> STYLE_PROPERTIES = Set.of("BasedOnStyles", "Packages");

    private static final Pattern YML_KEY_PATTERN = Pattern.compile("^\\s*([A-Za-z][A-Za-z0-9_-]*)\\s*:");
    private static final Pattern EXTENDS_PATTERN = Pattern.compile(
            "(?mi)^\\s*extends\\s*:\\s*['\"]?([A-Za-z0-9_-]+)['\"]?\\s*(?:#.*)?$"
    );
    private static final String BUILTIN_VALE_STYLE_MARKDOWN = buildBuiltinValeStyleMarkdown();
    private final Map<String, Optional<String>> resourceCache = new ConcurrentHashMap<>();
    private final ClassLoader classLoader = ValeDocumentationResolver.class.getClassLoader();

    private static @NotNull String buildBuiltinValeStyleMarkdown() {
        String[] rules = {"Vale.Spelling", "Vale.Terms", "Vale.Avoid", "Vale.Repetition"};
        StringBuilder md = new StringBuilder(
                "Vale comes with a single built-in style named `Vale` that implements a few rules:\n\n"
        );
        for (String rule : rules) {
            md.append("- **`").append(rule).append("`**  \n  ")
              .append(ValeRuleDescriptionHelper.buildBuiltInDescription(rule))
              .append("\n");
        }
        return md.toString();
    }

    public static boolean isValeIniFile(@NotNull String fileName) {
        return fileName.toLowerCase(Locale.ENGLISH).endsWith(".ini");
    }

    public static boolean isValeRuleFile(@NotNull String fileName) {
        return fileName.toLowerCase(Locale.ENGLISH).endsWith(".yml");
    }

    public @NotNull String resolveForBuiltinValeStyle() {
        return BUILTIN_VALE_STYLE_MARKDOWN;
    }

    public @Nullable String resolveForIniKey(@Nullable String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return null;
        }
        return readResource("doc/ini/" + rawKey.trim() + ".md");
    }

    public @Nullable String resolveForIniSection(@Nullable String rawSectionName) {
        if (rawSectionName == null) {
            return null;
        }
        String normalizedSectionName = normalizeIniSectionName(rawSectionName);
        if (normalizedSectionName.isEmpty()) {
            return null;
        }
        return resolveIniSectionDocumentation(normalizedSectionName);
    }

    public @Nullable String resolveForRuleKey(@Nullable String rawRuleKey, @NotNull String fileText) {
        if (rawRuleKey == null || rawRuleKey.isBlank()) {
            return null;
        }
        String token = rawRuleKey.toLowerCase(Locale.ENGLISH);
        String extendsValue = extractRuleExtends(fileText);
        if ("extends".equals(token)) {
            return mergeExtendsWithExample(extendsValue);
        }

        String common = readResource("doc/yml/" + token + ".md");
        if (common != null) {
            return common;
        }
        if (extendsValue == null) {
            return null;
        }
        return readResource("doc/yml/" + extendsValue + "/" + token + ".md");
    }

    public @Nullable String findRuleKeyAtOffset(@NotNull String fileText, int offset) {
        LineToken lineToken = extractLineToken(fileText, offset);
        if (lineToken == null || !lineToken.isOffsetInToken()) {
            return null;
        }
        return lineToken.token();
    }

    private @Nullable String mergeExtendsWithExample(@Nullable String extendsValue) {
        String extendsDoc = readResource("doc/yml/extends.md");
        if (extendsDoc == null || extendsValue == null) {
            return extendsDoc;
        }
        String example = readResource("doc/yml/" + extendsValue + "/example.md");
        if (example == null || example.isBlank()) {
            return extendsDoc;
        }
        return extendsDoc + "\n\n## Example for " + extendsValue + "\n\n" + example;
    }

    private @Nullable String resolveIniSectionDocumentation(@NotNull String sectionName) {
        String exact = readResource("doc/ini/section/" + sectionName + ".md");
        if (exact != null) {
            return exact;
        }
        if (sectionName.isEmpty()) {
            return null;
        }
        String normalized = Character.toUpperCase(sectionName.charAt(0))
                + sectionName.substring(1).toLowerCase(Locale.ENGLISH);
        if (normalized.equals(sectionName)) {
            return null;
        }
        return readResource("doc/ini/section/" + normalized + ".md");
    }

    private @Nullable String extractRuleExtends(@NotNull String fileText) {
        Matcher matcher = EXTENDS_PATTERN.matcher(fileText);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).toLowerCase(Locale.ENGLISH);
    }

    private @Nullable LineToken extractLineToken(@NotNull String fileText, int offset) {
        if (fileText.isEmpty()) {
            return null;
        }
        int safeOffset = Math.max(0, Math.min(offset, fileText.length()));
        TextRange lineBounds = lineRangeAtOffset(fileText, safeOffset);

        String line = fileText.substring(lineBounds.getStartOffset(), lineBounds.getEndOffset());
        Matcher matcher = YML_KEY_PATTERN.matcher(line);
        if (!matcher.find()) {
            return null;
        }

        String token = matcher.group(1).toLowerCase(Locale.ENGLISH);
        int tokenStartInLine = matcher.start(1);
        int tokenEndInLineExclusive = matcher.end(1);
        int column = safeOffset - lineBounds.getStartOffset();
        boolean isInToken = column >= tokenStartInLine && column < tokenEndInLineExclusive;
        return new LineToken(token, isInToken);
    }

    private @NotNull String normalizeIniSectionName(@NotNull String rawSectionName) {
        String normalized = rawSectionName.trim();
        if (normalized.length() >= 2 && normalized.startsWith("[") && normalized.endsWith("]")) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        return normalized;
    }

    private static @NotNull TextRange lineRangeAtOffset(@NotNull String text, int offset) {
        int lineStart = text.lastIndexOf('\n', Math.max(0, offset - 1));
        lineStart = lineStart < 0 ? 0 : lineStart + 1;
        int lineEnd = text.indexOf('\n', offset);
        lineEnd = lineEnd < 0 ? text.length() : lineEnd;
        return TextRange.create(lineStart, lineEnd);
    }

    private @Nullable String readResource(@NotNull String path) {
        return resourceCache.computeIfAbsent(path, this::loadResource).orElse(null);
    }

    private @NotNull Optional<String> loadResource(@NotNull String path) {
        try (InputStream inputStream = classLoader.getResourceAsStream(path)) {
            if (inputStream == null) {
                return Optional.empty();
            }
            return Optional.of(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    private record LineToken(@NotNull String token, boolean isOffsetInToken) {
    }
}
