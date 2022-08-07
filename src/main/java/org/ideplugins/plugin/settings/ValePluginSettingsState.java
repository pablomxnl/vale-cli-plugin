package org.ideplugins.plugin.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@State(
        name = "org.ideplugins.vale_cli_plugin.settings",
        storages = {@Storage("SdkSettingsPlugin.xml")}
)
public class ValePluginSettingsState implements PersistentStateComponent<ValePluginSettingsState> {

    public String valePath = "";
    public String valeSettingsPath = System.getProperty("user.home") + File.separator + "vale.ini";
    public String extensions = "md,adoc";

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
        return ServiceManager.getService(ValePluginSettingsState.class);
    }

}
