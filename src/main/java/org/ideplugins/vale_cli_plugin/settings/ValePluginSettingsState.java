package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.annotations.OptionTag;
import org.jetbrains.annotations.NotNull;

@State(
        name = "org.ideplugins.vale_cli_plugin.settings",
        storages = {@Storage("valeCliSettings.xml")}
)
@Service(Service.Level.APP)
public final class ValePluginSettingsState implements PersistentStateComponent<ValePluginSettingsState.State> {

    private static final Logger LOG = Logger.getInstance(ValePluginSettingsState.class);
    private ValePluginSettingsState.State state = new State();

    public static class State {
        public String valePath = "";
        @OptionTag(converter = ValeVersionConverter.class)
        public ValeVersion valeVersion = ValeVersion.parse(null);
    }

    @Override
    public void initializeComponent() {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    if (state.valePath.isBlank()) {
                        LOG.info("ValePluginSettingsState.initializeComponent: ValePath is empty");
                        setValePath(OSUtils.findValeBinaryPath());
                    }
                    if (!state.valeVersion.isKnown()){
                        String rawVersion = state.valePath.isBlank() ? "" : OSUtils.valeVersion(state.valePath);
                        state.valeVersion = ValeVersion.parse(rawVersion);
                        ValeVersion.setCurrent(state.valeVersion);
                    }
                    if (!state.valePath.isBlank() && state.valeVersion.isKnown()) {
                        LOG.info(String.format("Found vale version:%s  executable:%s", state.valeVersion, state.valePath));
                    }
                }
        );
    }


    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
        if (this.state.valeVersion == null) {
            this.state.valeVersion = ValeVersion.parse(null);
        }
        ValeVersion.setCurrent(this.state.valeVersion);
    }

    public static ValePluginSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(ValePluginSettingsState.class);
    }

    public String getValePath() {
        return state.valePath;
    }

    public void setValePath(String valePath) {
        state.valePath = valePath;
    }

    public ValeVersion getValeVersion() {
        return state.valeVersion;
    }

    public void setValeVersion(ValeVersion valeVersion) {
        this.state.valeVersion = valeVersion;
    }

}
