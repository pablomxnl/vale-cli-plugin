package org.ideplugins.vale_cli_plugin.annotator;

import org.jetbrains.annotations.NotNull;

public final class ValeRuleDescriptionHelper {

    private ValeRuleDescriptionHelper() {
    }

    public static @NotNull String buildBuiltInDescription(@NotNull String check) {
        return switch (check) {
            case "Vale.Spelling" -> buildSpellingDescription();
            case "Vale.Terms" -> "Enforces the current project's accepted Vocabulary terms.";
            case "Vale.Avoid" -> "Enforces the current project's rejected Vocabulary terms.";
            case "Vale.Repetition" -> "Flags repeated words.";
            default -> "";
        };
    }

    private static @NotNull String buildSpellingDescription() {
        return "Checks for spelling errors in your content. Consumes any Hunspell-compatible dictionaries stored in "
                + "the Vale styles path under config/dictionaries.";
    }
}
