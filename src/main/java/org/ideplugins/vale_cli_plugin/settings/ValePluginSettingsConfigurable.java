package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.options.Configurable;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

public class ValePluginSettingsConfigurable implements Configurable {

    private ValePluginSettingsComponent settingsComponent;

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Vale CLI Plugin";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return settingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsComponent = new ValePluginSettingsComponent();
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        ValePluginSettingsState settings = ValePluginSettingsState.getInstance();
        boolean modified = !settingsComponent.getValePathText().equals(settings.valePath);
        modified |= settingsComponent.getConfigurationFilePathText().equals(settings.valeSettingsPath);
        modified |= settingsComponent.getExtensionsText().equals(settings.extensions);
        return modified;
    }

    @Override
    public void apply() {
        ValePluginSettingsState settings = ValePluginSettingsState.getInstance();
        settings.valePath = settingsComponent.getValePathText();
        settings.valeSettingsPath = settingsComponent.getConfigurationFilePathText();
        settings.extensions = settingsComponent.getExtensionsText();
    }


    @Override
    public void reset() {
        ValePluginSettingsState settings = ValePluginSettingsState.getInstance();
        settingsComponent.setValePathText(settings.valePath);
        settingsComponent.setConfigurationFilePathText(settings.valeSettingsPath);
        settingsComponent.setExtensionsText(settings.extensions);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
