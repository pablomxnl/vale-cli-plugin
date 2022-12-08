package org.ideplugins.plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@State(
        name = "org.ideplugins.vale_cli_plugin.settings",
        storages = {@Storage("valeCliSettings.xml")}
)
public class ValePluginSettingsState implements PersistentStateComponent<ValePluginSettingsState> {

    public String valePath;

    public String valeSettingsPath = "";
    public String extensions = "md,adoc,rst";

    @Nullable
    @Override
    public ValePluginSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ValePluginSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static ValePluginSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(ValePluginSettingsState.class);
    }

    @Override
    public void initializeComponent() {
        if (valePath == null){
            valePath = OSUtils.findValeBinaryPath();
        }
        PersistentStateComponent.super.initializeComponent();
    }
}
