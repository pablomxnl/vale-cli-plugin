package org.ideplugins.vale_cli_plugin.annotator;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.util.DocumentUtil;
import org.ideplugins.vale_cli_plugin.service.ValeCliExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

final class ValeQuickFixHelper {

    private static final Logger LOGGER = Logger.getInstance(ValeQuickFixHelper.class);

    private ValeQuickFixHelper() {
    }

    @Nullable
    static FixTarget findFixTarget(@NotNull Project project,
                                   @NotNull Editor editor,
                                   @NotNull PsiFile file,
                                   @NotNull String actionName,
                                   @NotNull String check,
                                   @NotNull String match,
                                   @Nullable TextRange hintRange) {
        ReadSnapshot snapshot = ReadAction.compute(() -> {
            String extension = file.getViewProvider().getVirtualFile().getExtension();
            String path = file.getViewProvider().getVirtualFile().getPath();
            if (path.isBlank()) {
                return null;
            }
            Document document = editor.getDocument();
            long documentStamp = document.getModificationStamp();
            int caretOffset = editor.getCaretModel().getOffset();
            String documentText = document.getImmutableCharSequence().toString();
            String safeExtension = extension == null ? "" : extension;
            return new ReadSnapshot(path, safeExtension, documentText, caretOffset, documentStamp);
        });
        if (snapshot == null) {
            return null;
        }

        ValeCliExecutor cliExecutor = ValeCliExecutor.getInstance(project);
        ProcessOutput output;
        try {
            output = cliExecutor.runLintStdinCommand(
                    snapshot.documentText(),
                    snapshot.extension(),
                    snapshot.path());
        } catch (ExecutionException | IOException e) {
            LOGGER.debug("Vale quick fix failed to run Vale CLI", e);
            return null;
        }

        if (output.getExitCode() != 0) {
            LOGGER.debug("Vale quick fix lint failed with exit code: " + output.getExitCode());
            return null;
        }

        List<ValeProblem> problems;
        try {
            problems = cliExecutor.parseSuccessProcessOutput(output);
        } catch (RuntimeException e) {
            LOGGER.debug("Vale quick fix failed to parse Vale output", e);
            return null;
        }

        return ReadAction.compute(() -> {
            Document document = editor.getDocument();
            if (document.getModificationStamp() != snapshot.documentStamp()) {
                return null;
            }
            int caretOffset = snapshot.caretOffset();

            ValeProblem bestProblem = null;
            TextRange bestRange = null;
            int bestScore = Integer.MAX_VALUE;

            for (ValeProblem problem : problems) {
                if (problem.action() == null || problem.action().name() == null) {
                    continue;
                }
                if (!actionName.equalsIgnoreCase(problem.action().name())) {
                    continue;
                }
                String problemCheck = problem.check() == null ? "" : problem.check();
                String problemMatch = problem.match() == null ? "" : problem.match();
                if (!check.equals(problemCheck)) {
                    continue;
                }
                if (!match.equals(problemMatch)) {
                    continue;
                }

                TextRange range = problem.getRange(document);
                if (range == null) {
                    continue;
                }
                if (!DocumentUtil.isValidOffset(range.getStartOffset(), document)
                        || !DocumentUtil.isValidOffset(range.getEndOffset(), document)) {
                    continue;
                }

                int score = scoreRange(range, caretOffset, hintRange);
                if (score < bestScore) {
                    bestScore = score;
                    bestProblem = problem;
                    bestRange = range;
                    if (score == 0) {
                        break;
                    }
                }
            }

            if (bestProblem == null || bestRange == null) {
                return null;
            }
            return new FixTarget(bestProblem, bestRange);
        });
    }

    /**
     * Scores how well a candidate range matches the user's current context.
     * Lower is better:
     * <ul>
     *   <li>0: caret is inside the candidate range.</li>
     *   <li>1: candidate range intersects the hint range.</li>
     *   <li>2: no proximity to caret or hint range.</li>
     * </ul>
     */
    private static int scoreRange(@NotNull TextRange range, int caretOffset, @Nullable TextRange hintRange) {
        if (range.containsOffset(caretOffset)) {
            return 0;
        }
        if (hintRange != null && range.intersects(hintRange)) {
            return 1;
        }
        return 2;
    }

    record FixTarget(@NotNull ValeProblem problem, @NotNull TextRange range) {
    }

    private record ReadSnapshot(@NotNull String path,
                                @NotNull String extension,
                                @NotNull String documentText,
                                int caretOffset,
                                long documentStamp) {
    }
}
