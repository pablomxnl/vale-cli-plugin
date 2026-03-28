package org.ideplugins.vale_cli_plugin.documentation;

import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Rudimentary implementation of an ini syntax parser.
 * <br>
 * We only care for properties with their key and value as we want to offer documentation and navigation for those.
 * It is open for adding support for other elements in the future if needed, but for now we only really map key-value
 * properties and nothing else.
 */
final class ValeIniSyntax {

    // Keep key names in a filename-safe subset so they map cleanly to doc resources.
    private static final Pattern VALID_KEY_PATTERN = Pattern.compile("[A-Za-z0-9._-]+");

    private ValeIniSyntax() {
    }

    static @Nullable Property findPropertyKeyAtOffset(@NotNull String text, int offset) {
        if (text.isEmpty()) {
            return null;
        }
        int safeOffset = Math.max(0, Math.min(offset, text.length() - 1));
        TextRange lineRange = lineRangeAtOffset(text, safeOffset);
        Property property = parsePropertyLine(text, lineRange.getStartOffset(), lineRange.getEndOffset());
        if (property == null || !property.containsKeyOffset(safeOffset)) {
            return null;
        }
        return property;
    }

    static @Nullable Section findSectionNameAtOffset(@NotNull String text, int offset) {
        if (text.isEmpty()) {
            return null;
        }
        int safeOffset = Math.max(0, Math.min(offset, text.length() - 1));
        TextRange lineRange = lineRangeAtOffset(text, safeOffset);
        Section section = parseSectionLine(text, lineRange.getStartOffset(), lineRange.getEndOffset());
        if (section == null || !section.containsNameOffset(safeOffset)) {
            return null;
        }
        return section;
    }

    static @NotNull TextRange lineRangeAtOffset(@NotNull String text, int offset) {
        int lineStart = text.lastIndexOf('\n', Math.max(0, offset - 1));
        lineStart = lineStart < 0 ? 0 : lineStart + 1;
        int lineEnd = text.indexOf('\n', offset);
        lineEnd = lineEnd < 0 ? text.length() : lineEnd;
        return TextRange.create(lineStart, lineEnd);
    }

    private static @Nullable Property parsePropertyLine(@NotNull String text, int start, int end) {
        if (start >= end) {
            return null;
        }
        int firstNonWhitespace = firstNonWhitespace(text, start, end);
        if (firstNonWhitespace < 0) {
            return null;
        }

        char first = text.charAt(firstNonWhitespace);

        // ignore sections ("[") and comments (";", "#")
        if (first == ';' || first == '#' || first == '[') {
            return null;
        }

        int separator = findSeparator(text, firstNonWhitespace, end);
        if (separator < 0) {
            return null;
        }

        int keyStart = firstNonWhitespace;
        int keyEnd = trimEnd(text, keyStart, separator);
        if (keyStart >= keyEnd) {
            return null;
        }
        String key = text.substring(keyStart, keyEnd);
        if (!VALID_KEY_PATTERN.matcher(key).matches()) {
            return null;
        }

        int valueStart = separator + 1;
        int trimmedValueStart = firstNonWhitespace(text, valueStart, end);
        String value = trimmedValueStart < 0 ? "" : text.substring(trimmedValueStart, end).trim();
        int valueRangeStart = trimmedValueStart < 0 ? end : trimmedValueStart;
        return new Property(key, value.isEmpty() ? null : value, keyStart, keyEnd, valueRangeStart, end);
    }

    private static @Nullable Section parseSectionLine(@NotNull String text, int start, int end) {
        if (start >= end) {
            return null;
        }
        int firstNonWhitespace = firstNonWhitespace(text, start, end);
        if (firstNonWhitespace < 0 || text.charAt(firstNonWhitespace) != '[') {
            return null;
        }

        int closingBracket = text.indexOf(']', firstNonWhitespace + 1);
        if (closingBracket < 0 || closingBracket >= end) {
            return null;
        }

        int rawNameStart = firstNonWhitespace + 1;
        int nameStart = firstNonWhitespace(text, rawNameStart, closingBracket);
        if (nameStart < 0) {
            return null;
        }
        int nameEnd = trimEnd(text, nameStart, closingBracket);
        if (nameStart >= nameEnd) {
            return null;
        }
        String name = text.substring(nameStart, nameEnd);
        if (!VALID_KEY_PATTERN.matcher(name).matches()) {
            return null;
        }
        return new Section(name, nameStart, nameEnd);
    }

    private static int findSeparator(@NotNull String text, int start, int end) {
        int equals = text.indexOf('=', start);
        int colon = text.indexOf(':', start);
        if (equals < 0 || equals >= end) {
            equals = -1;
        }
        if (colon < 0 || colon >= end) {
            colon = -1;
        }
        if (equals < 0) {
            return colon;
        }
        if (colon < 0) {
            return equals;
        }
        return Math.min(equals, colon);
    }

    private static int firstNonWhitespace(@NotNull String text, int start, int end) {
        for (int i = start; i < end; i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static int trimEnd(@NotNull String text, int start, int endExclusive) {
        int end = endExclusive;
        while (end > start && Character.isWhitespace(text.charAt(end - 1))) {
            end--;
        }
        return end;
    }

    record Property(@NotNull String key,
                    @Nullable String value,
                    int keyStartByte,
                    int keyEndByteExclusive,
                    int valueStartByte,
                    int valueEndByteExclusive) {
        boolean containsKeyOffset(int offset) {
            return offset >= keyStartByte && offset < keyEndByteExclusive;
        }
    }

    record Section(@NotNull String name,
                   int nameStartByte,
                   int nameEndByteExclusive) {
        boolean containsNameOffset(int offset) {
            return offset >= nameStartByte && offset < nameEndByteExclusive;
        }
    }
}
