package org.ideplugins.vale_cli_plugin.annotator;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.xml.util.XmlStringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

final class ValeTooltipHelper {

    private ValeTooltipHelper() {
    }

    static @Nullable String buildTooltip(@NotNull ValeProblem problem) {
        String check = problem.check() == null ? "" : problem.check();
        if (check.isBlank()) {
            return null;
        }
        String message = problem.message() == null ? "" : problem.message();
        String safeMessage = XmlStringUtil.escapeString(message);
        String safeCheck = XmlStringUtil.escapeString(check);
        boolean isBuiltIn = isValeBuiltInStyle(check);
        String link = problem.link() == null ? "" : problem.link();
        String safeLink = link.isBlank() ? "" : XmlStringUtil.escapeString(link);

        String documentationRow = createDocumentationLinkRow(safeLink, isBuiltIn);
        String descriptionRow = createDescriptionRow(problem, isBuiltIn, check);

        if (!isBuiltIn) {
            String safeHref = ValeRuleLinkHandler.PREFIX + "check/" + StringUtil.escapeQuotes(check);
            return "<html>" + safeMessage + "<br/><br/>"
                    + "<table cellpadding=\"0\" cellspacing=\"0\">"
                    + "<tr><td>Rule:&nbsp;</td><td><a href=\""
                    + safeHref + "\">" + safeCheck + "</a></td></tr>"
                    + documentationRow
                    + descriptionRow
                    + "</table></html>";
        }
        return "<html>" + safeMessage + "<br/><br/>"
                + "<table cellpadding=\"0\" cellspacing=\"0\">"
                + "<tr><td>Rule:&nbsp;</td><td>"
                + safeCheck + " (built-in rule)"
                + "</td></tr>"
                + documentationRow
                + descriptionRow
                + "</table></html>";
    }

    private static @NotNull String createDescriptionRow(@NotNull ValeProblem problem,
                                                        boolean isBuiltIn, String check) {
        String description = problem.description() == null ? "" : problem.description();
        if (description.isBlank() && isBuiltIn) {
            description = ValeRuleDescriptionHelper.buildBuiltInDescription(check);
        }
        boolean hasDescription = !description.isBlank();
        String safeDescription = hasDescription ? XmlStringUtil.escapeString(description) : "";
        return hasDescription
                ? "<tr><td></td><td><i>" + safeDescription + "</i></td></tr>"
                : "";
    }

    private static @NotNull String createDocumentationLinkRow(String safeLink, boolean isBuiltIn) {
        String documentationRow;
        if (safeLink.isEmpty()) {
            if (isBuiltIn) {
                String valeLink = "https://vale.sh/docs/styles#vale";
                documentationRow = "<tr><td>Link:&nbsp;</td><td><a href=\""
                        + valeLink + "\">" + valeLink + "</a></td></tr>";
            } else {
                documentationRow = "";
            }
        } else {
            String safeLinkHref = StringUtil.escapeQuotes(safeLink);
            documentationRow = "<tr><td>Link:&nbsp;</td><td><a href=\""
                    + safeLinkHref + "\">" + safeLink + "</a></td></tr>";
        }
        return documentationRow;
    }

    private static boolean isValeBuiltInStyle(@NotNull String check) {
        return check.toLowerCase(Locale.ENGLISH).startsWith("vale.");
    }
}
