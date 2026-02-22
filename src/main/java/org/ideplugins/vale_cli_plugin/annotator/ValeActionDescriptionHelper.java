package org.ideplugins.vale_cli_plugin.annotator;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds human-readable labels for Vale action entries while keeping a technical fallback
 * for custom or unsupported actions.
 */
public final class ValeActionDescriptionHelper {

    private ValeActionDescriptionHelper() {
    }

    public static @NotNull String buildLazyLabel(@NotNull ValeProblem problem) {
        ValeAction action = problem.action();
        String actionName = action == null || action.name() == null ? "" : action.name();
        if (actionName.isBlank()) {
            return "Vale: Apply fix";
        }
        List<String> params = action.parameters().isPresent()
                ? action.parameters().get()
                : List.of();
        String normalized = actionName.toLowerCase(Locale.ENGLISH);
        return switch (normalized) {
            case "suggest" -> buildSuggestLabel(params);
            case "replace" -> buildReplaceLabel(problem.match(), params);
            case "remove" -> buildRemoveLabel(problem.match());
            case "edit" -> "Vale: Edit matched text";
            default -> buildTechnicalLabel(actionName, params);
        };
    }

    private static @NotNull String buildRemoveLabel(@NotNull String match) {
        return "Vale: Remove '%s'".formatted(match);
    }

    private static String buildSuggestLabel(@NotNull List<String> params) {
        if (params.isEmpty()) {
            return "Vale: Suggest alternatives";
        }
        String param = params.getFirst();
        if (param != null && param.equalsIgnoreCase("spellings")) {
            return "Vale: Suggest spelling corrections";
        }
        if (param != null && param.endsWith(".tengo")) {
            return "Vale: Suggest alternatives via " + param;
        }
        return "Vale: Suggest alternatives";
    }

    private static String buildReplaceLabel(@NotNull String match, @NotNull List<String> params) {
        if (params.isEmpty()) {
            return "Vale: Replace with suggested term";
        }
        int count = params.size();
        if (count == 1) {
            if (params.getFirst() == null || params.getFirst().isEmpty()) {
                return "Vale: Remove '%s'".formatted(match);
            }
            return "Vale: Replace with '%s'".formatted(formatActionParams(params));
        }
        return "Vale: Replace with one of " + count + " options (%s)".formatted(formatActionParams(params));
    }

    private static String buildTechnicalLabel(@NotNull String actionName, @NotNull List<String> params) {
        String formatted = formatActionParams(params);
        if (formatted.isBlank()) {
            return "Vale: Action " + actionName;
        }
        return "Vale: Action " + actionName + " Params: [" + formatted + "]";
    }

    private static String formatActionParams(@NotNull List<String> params) {
        if (params.isEmpty()) {
            return "";
        }
        List<String> formatted = new ArrayList<>(params.size());
        for (String param : params) {
            if (param == null) {
                formatted.add("null");
                continue;
            }
            if (param.isEmpty()) {
                formatted.add("''");
                continue;
            }
            if (param.isBlank()) {
                formatted.add("'" + param.replace("\t", "\\t").replace("\n", "\\n") + "'");
                continue;
            }
            formatted.add(param);
        }
        return String.join(", ", formatted);
    }
}
