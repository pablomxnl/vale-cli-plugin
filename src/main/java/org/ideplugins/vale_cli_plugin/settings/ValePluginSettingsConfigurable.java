package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ValePluginSettingsConfigurable implements Configurable {

    private ValePluginSettingsComponent settingsComponent;

    public ValePluginSettingsConfigurable() {
        settingsComponent = new ValePluginSettingsComponent();
    }

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
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        ValePluginSettingsState settings = ValePluginSettingsState.getInstance();
        boolean modified = !settingsComponent.getConfigurationFilePathText().equals(settings.valeSettingsPath);
        modified |= settingsComponent.getExtensionsText().equals(settings.extensions);
        modified |= ( settingsComponent.getInstallVale() == settings.installVale);
        modified |= ( settingsComponent.getSyncValeConfigOnStartup() == settings.syncVale);
        return modified;
    }

    @Override
    public void apply() {
        ValePluginSettingsState settings = ValePluginSettingsState.getInstance();
        settings.valeSettingsPath = settingsComponent.getConfigurationFilePathText();
        settings.extensions = settingsComponent.getExtensionsText();
        settings.syncVale = settingsComponent.getSyncValeConfigOnStartup();
        settings.installVale = settingsComponent.getInstallVale();
    }


    @Override
    public void reset() {
        ValePluginSettingsState settings = ValePluginSettingsState.getInstance();
        settingsComponent.setConfigurationFilePathText(settings.valeSettingsPath);
        settingsComponent.setExtensionsText(settings.extensions);
        settingsComponent.setInstallVale(settings.installVale);
        settingsComponent.setSyncValeConfigOnStartup(settings.syncVale);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
