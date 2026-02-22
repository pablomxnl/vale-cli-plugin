package org.ideplugins.vale_cli_plugin.annotator;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

public record ValeAction(
        String name,
        @JsonProperty(value = "Params")
        Optional<List<String>> parameters
) {
}
