package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;

public final class ValeVersionConverter extends Converter<ValeVersion> {

    @Override
    public ValeVersion fromString(@NotNull String value) {
        if (value.isBlank()) {
            return ValeVersion.parse(null);
        }
        return ValeVersion.parse(value);
    }

    @Override
    public String toString(@NotNull ValeVersion value) {
        String raw = value.getRaw();
        if (raw == null) {
            return value.getMajor() + "." + value.getMinor() + "." + value.getPatch();
        }
        return raw.isBlank() ? ValeVersion.UNKNOWN_VERSION_NAME : raw;
    }
}
