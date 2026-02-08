package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializerUtil;
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
        if (valePath.isEmpty() || valePath.isBlank()) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                        valePath = OSUtils.findValeBinaryPath();
                        valeVersion = valePath.isEmpty()? OSUtils.valeVersion(valePath) : "";
                        if (!valePath.isEmpty() && !valeVersion.isEmpty()){
                            LOG.info(String.format("Found vale version:%s  executable:%s", valeVersion, valePath));
                        }
                    }
            );
        }
    }

    public String valePath = "";
    public String valeVersion = "";

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


}
