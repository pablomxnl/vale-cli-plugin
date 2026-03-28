package org.ideplugins.vale_cli_plugin.documentation;

import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValeDocumentationResolver {

    private static final Pattern YML_KEY_PATTERN = Pattern.compile("^\\s*([A-Za-z][A-Za-z0-9_-]*)\\s*:");
    private static final Pattern EXTENDS_PATTERN = Pattern.compile(
            "(?mi)^\\s*extends\\s*:\\s*['\"]?([A-Za-z0-9_-]+)['\"]?\\s*(?:#.*)?$"
    );
    private final Map<String, Optional<String>> resourceCache = new ConcurrentHashMap<>();
    private final ClassLoader classLoader = ValeDocumentationResolver.class.getClassLoader();

    public static boolean isValeIniFile(@NotNull String fileName) {
        return fileName.toLowerCase(Locale.ENGLISH).endsWith(".ini");
    }

    public static boolean isValeRuleFile(@NotNull String fileName) {
        return fileName.toLowerCase(Locale.ENGLISH).endsWith(".yml");
    }

    public @Nullable String resolveForIniOffset(@NotNull String fileText, int offset) {
        ValeIniSyntax.Property property = ValeIniSyntax.findPropertyKeyAtOffset(fileText, offset);
        if (property != null) {
            return readResource("doc/ini/" + property.key() + ".md");
        }

        ValeIniSyntax.Section section = ValeIniSyntax.findSectionNameAtOffset(fileText, offset);
        if (section == null) {
            return null;
        }
        return resolveIniSectionDocumentation(section.name());
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

    public @Nullable String resolveForRuleOffset(@NotNull String fileText, int offset) {
        LineToken lineToken = extractLineToken(fileText, offset);
        if (lineToken == null || !lineToken.isOffsetInToken()) {
            return null;
        }
        return resolveForRuleKey(lineToken.token(), fileText);
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

    private @Nullable LineToken extractLineToken(@NotNull String fileText,
                                                 int offset) {
        if (fileText.isEmpty()) {
            return null;
        }
        int safeOffset = Math.max(0, Math.min(offset, fileText.length()));
        TextRange lineBounds = ValeIniSyntax.lineRangeAtOffset(fileText, safeOffset);

        String line = fileText.substring(lineBounds.getStartOffset(), lineBounds.getEndOffset());
        Matcher matcher = ValeDocumentationResolver.YML_KEY_PATTERN.matcher(line);
        if (!matcher.find()) {
            return null;
        }
        String token = matcher.group(1);
        int tokenStartInLine = matcher.start(1);
        int tokenEndInLineExclusive = matcher.end(1);
        int column = safeOffset - lineBounds.getStartOffset();
        boolean isInToken = column >= tokenStartInLine && column < tokenEndInLineExclusive;
        String normalizedToken = token.toLowerCase(Locale.ENGLISH);

        return new LineToken(normalizedToken, isInToken);
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

    private record LineToken(String token, boolean isOffsetInToken) {
    }
}
