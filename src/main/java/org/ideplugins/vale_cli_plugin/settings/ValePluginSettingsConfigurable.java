package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.options.Configurable;

import javax.swing.JComponent;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

public class ValePluginSettingsConfigurable implements Configurable {

    private ValePluginSettingsComponent settingsComponent;
    private ValePluginSettingsState settings;

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Vale CLI";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return settingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settings = ValePluginSettingsState.getInstance();
        settingsComponent = new ValePluginSettingsComponent();
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        boolean pathModified = !settingsComponent.getValePathText().equals(settings.valePath);
        boolean settingsModified = !settingsComponent.getConfigurationFilePathText().equals(settings.valeSettingsPath);
        boolean extensionsModified = !settingsComponent.getExtensionsText().equals(settings.extensions);
        return pathModified || settingsModified || extensionsModified;
    }

    @Override
    public void apply() {
        if (!settingsComponent.getValePathText().equals(settings.valePath)){
            settings.valeVersion = OSUtils.valeVersion(settingsComponent.getValePathText());
            settingsComponent.setValeVersion(settings.valeVersion);
        }
        settings.valePath = settingsComponent.getValePathText();
        settings.valeSettingsPath = settingsComponent.getConfigurationFilePathText();
        settings.extensions = settingsComponent.getExtensionsText();
    }


    @Override
    public void reset() {
        settingsComponent.setValePathText(settings.valePath);
        settingsComponent.setConfigurationFilePathText(settings.valeSettingsPath);
        settingsComponent.setExtensionsText(settings.extensions);
        settingsComponent.setValeVersion(settings.valeVersion);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
