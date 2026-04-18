package org.ideplugins.vale_cli_plugin.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Project-level cache for {@link ValeConfigurationPaths}.
 * <p>
 * Callers that run under IntelliJ's {@code ReadAction} (e.g. completion contributors,
 * goto declaration handlers) must NOT call {@link ValeLsConfigService#loadConfigurationPaths()}
 * directly, because that executes {@code vale ls-config} as a subprocess which is forbidden
 * while a read lock is held.
 * <p>
 * Instead they should call {@link #getCachedPaths()}, which returns the last snapshot computed
 * outside the read lock.  The cache is warmed at project startup via {@link #refresh()}.
 */
@Service(Service.Level.PROJECT)
public final class ValeStylesCache {

    private final AtomicReference<ValeConfigurationPaths> cache =
            new AtomicReference<>(ValeConfigurationPaths.empty());
    private final Project project;

    public ValeStylesCache(@NotNull Project project) {
        this.project = project;
    }

    public static @NotNull ValeStylesCache getInstance(@NotNull Project project) {
        return project.getService(ValeStylesCache.class);
    }

    /** Returns the last successfully fetched configuration paths (never {@code null}). */
    public @NotNull ValeConfigurationPaths getCachedPaths() {
        return cache.get();
    }

    /** Directly stores a freshly computed result. Called by {@link ValeLsConfigService} after every successful load. */
    public void update(@NotNull ValeConfigurationPaths paths) {
        cache.set(paths);
    }

    /**
     * Triggers a background refresh of the cached paths.
     * Safe to call from any thread (EDT or pooled).
     */
    public void refresh() {
        ValeLsConfigService.getInstance(project).requestConfigurationPaths(cache::set);
    }
}
