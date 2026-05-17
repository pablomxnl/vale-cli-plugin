package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.ideplugins.vale_cli_plugin.Constants.PLUGIN_BUNDLE;
import static org.ideplugins.vale_cli_plugin.Constants.PLUGIN_ID;

@Service(Service.Level.APP)
@State(name = PLUGIN_ID+"-app", storages = {@Storage("valeCliSettings-app.xml")})
public final class ValeCliPluginConfigurationState
        implements PersistentStateComponent<ValeCliPluginConfigurationState.PluginSettings> {

    private static final String LAST_VERSION = "lastVersion";
    private static final String SENTRY_DSN = "sentryDsn";
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(PLUGIN_BUNDLE);

    private PluginSettings settings = createInitialSettings();

    private PluginSettings createInitialSettings() {
        return PluginSettings.create(BUNDLE.getString("vale.cli.plugin.version"));
    }

    @Override
    public @Nullable ValeCliPluginConfigurationState.PluginSettings getState() {
        return settings;
    }

    @Override
    public void loadState(@NotNull PluginSettings pluginSettings) {
        settings = pluginSettings;
    }

    public String getLastVersion(){
        return settings.configuration.get(LAST_VERSION);
    }

    public void setLastVersion(String version ){
        settings.configuration.put(LAST_VERSION, version);
    }

    public String getSentryDsn(){
        return settings.configuration.get(SENTRY_DSN);
    }

    public static class PluginSettings {
        @MapAnnotation
        private Map<String,String> configuration;

        static PluginSettings create(final String version){
            final PluginSettings instance = new PluginSettings();
            ResourceBundle rb = ResourceBundle.getBundle(PLUGIN_BUNDLE);
            String dsn = rb.getString("sentry.dsn");
            instance.configuration = new TreeMap<>(
                    Map.of(LAST_VERSION, version,
                            SENTRY_DSN , dsn)
            );
            return  instance;
        }

    }
}
