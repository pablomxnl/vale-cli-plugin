package org.ideplugins.vale_cli_plugin.annotator;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;
import java.util.List;

/**
 * Parses the JSON output of {@code vale fix} into suggestions and error metadata.
 */
public final class ValeFixOutputParser {

    private ValeFixOutputParser() {
    }

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .configure(JsonReadFeature.ALLOW_MISSING_VALUES, true)
            .build();

    public static @NotNull FixResult parse(String rawOutput) {
        try {
            if (rawOutput == null || rawOutput.isBlank()) {
                return new FixResult(Collections.emptyList(), "");
            }
            JsonNode root = MAPPER.readTree(rawOutput);
            if (root == null || !root.isObject()) {
                return new FixResult(Collections.emptyList(), "");
            }
            FixResultDTO dto = MAPPER.treeToValue(root, FixResultDTO.class);
            if (dto == null) {
                return new FixResult(Collections.emptyList(), "");
            }
            List<String> suggestions = dto.suggestions == null ? Collections.emptyList() : dto.suggestions;
            String error = dto.error == null ? "" : dto.error;
            return new FixResult(suggestions, error);
        } catch (Exception ex) {
            return new FixResult(Collections.emptyList(), "");
        }
    }

    private static final class FixResultDTO {
        /** Suggested replacements from Vale for the given alert. */
        @JsonProperty("suggestions")
        private List<String> suggestions;
        /** Optional error string if Vale could not compute suggestions. */
        @JsonProperty("error")
        private String error;
    }

    public record FixResult(@NotNull List<String> suggestions, @NotNull String error) {
    }
}
