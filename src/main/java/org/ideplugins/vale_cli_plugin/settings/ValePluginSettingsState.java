package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import org.jetbrains.annotations.NotNull;

@State(
        name = "org.ideplugins.vale_cli_plugin.settings",
        storages = {@Storage("valeCliSettings.xml")}
)
@Service(Service.Level.APP)
public final class ValePluginSettingsState implements PersistentStateComponent<ValePluginSettingsState> {

    private static final Logger LOG = Logger.getInstance(ValePluginSettingsState.class);

    @Override
    public void initializeComponent() {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    if (valePath.isBlank()) {
                        valePath = OSUtils.findValeBinaryPath();
                    }
                    String rawVersion = valePath.isBlank() ? "" : OSUtils.valeVersion(valePath);
                    valeVersion = ValeVersion.parse(rawVersion);
                    ValeVersion.setCurrent(valeVersion);
                    if (!valePath.isBlank() && valeVersion.isKnown()){
                        LOG.info(String.format("Found vale version:%s  executable:%s", valeVersion, valePath));
                    }
                }
        );
    }

    public String valePath = "";

    @OptionTag(converter = ValeVersionConverter.class)
    public ValeVersion valeVersion = ValeVersion.parse(null);

    @Override
    public ValePluginSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ValePluginSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
        if (valeVersion == null) {
            valeVersion = ValeVersion.parse(null);
        }
        ValeVersion.setCurrent(valeVersion);
    }

    public static ValePluginSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(ValePluginSettingsState.class);
    }


}
