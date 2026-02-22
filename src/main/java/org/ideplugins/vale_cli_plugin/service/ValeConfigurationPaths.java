package org.ideplugins.vale_cli_plugin.service;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ValeConfigurationPaths(@NotNull List<String> configFiles,
                                     @NotNull List<String> paths) {

    public static @NotNull ValeConfigurationPaths empty() {
        return new ValeConfigurationPaths(List.of(), List.of());
    }
}
