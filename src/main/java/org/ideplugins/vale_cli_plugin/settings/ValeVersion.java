package org.ideplugins.vale_cli_plugin.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ValeVersion implements Comparable<ValeVersion> {

    public static final String UNKNOWN_VERSION_NAME = "unknown";
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    private static final AtomicReference<ValeVersion> CURRENT = new AtomicReference<>(new ValeVersion(null));

    private final int major;
    private final int minor;
    private final int patch;
    private final String raw;

    private ValeVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.raw = null;
    }

    private ValeVersion(String raw) {
        this.major = Integer.MAX_VALUE;
        this.minor = Integer.MAX_VALUE;
        this.patch = Integer.MAX_VALUE;
        this.raw = raw;
    }

    public static @NotNull ValeVersion parse(@Nullable String rawVersion) {
        if (rawVersion == null || rawVersion.isBlank()) {
            return new ValeVersion(UNKNOWN_VERSION_NAME);
        }

        String normalized = rawVersion.trim();
        Matcher matcher = VERSION_PATTERN.matcher(normalized);
        if (!matcher.matches()) {
            return new ValeVersion(normalized);
        }

        return new ValeVersion(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3))
        );
    }

    public static void setCurrent(@NotNull ValeVersion version) {
        CURRENT.set(version);
    }

    public static @NotNull ValeVersion getCurrent() {
        return CURRENT.get();
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    /**
     * Gets the raw version descriptor of vale.
     * @return unparsed version String. Null if the version was correctly parsed.
     */
    @Nullable
    public String getRaw() {
        return raw;
    }

    public boolean isKnown() {
        return raw == null;
    }

    public boolean isAtLeast(@NotNull ValeVersion other) {
        return compareTo(other) >= 0;
    }

    @Override
    public int compareTo(@NotNull ValeVersion other) {
        int majorDiff = Integer.compare(major, other.major);
        if (majorDiff != 0) {
            return majorDiff;
        }
        int minorDiff = Integer.compare(minor, other.minor);
        if (minorDiff != 0) {
            return minorDiff;
        }
        return Integer.compare(patch, other.patch);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ValeVersion other)) {
            return false;
        }
        return major == other.major
                && minor == other.minor
                && patch == other.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, raw);
    }

    @Override
    public String toString() {
        if (raw != null) {
            return raw;
        }

        return major + "." + minor + "." + patch;
    }
}
