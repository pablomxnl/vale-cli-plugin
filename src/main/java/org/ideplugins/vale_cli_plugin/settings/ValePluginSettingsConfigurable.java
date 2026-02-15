package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.options.Configurable;

import javax.swing.JComponent;

import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ResourceBundle;

import static org.ideplugins.vale_cli_plugin.Constants.PLUGIN_BUNDLE;

public class ValePluginSettingsConfigurable implements Configurable {

    private ValePluginSettingsComponent settingsComponent;
    private ValePluginSettingsState settings;
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(PLUGIN_BUNDLE);

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
        return !settingsComponent.getValePathText().equals(settings.valePath);
    }

    @Override
    public void apply() throws ConfigurationException {
        String value = settingsComponent.getValePathText();
        if (!value.isEmpty()){
            File f = new File(value);
            if (!f.exists())
                throw new ConfigurationException(
                        BUNDLE.getString("vale.cli.plugin.settings.invalidexe.message"),
                        BUNDLE.getString("vale.cli.plugin.invalid.settings.title"));
        }
        settings.valePath = value;
        settings.valeVersion = settingsComponent.getValeVersion();
        ValeVersion.setCurrent(settings.valeVersion);
    }


    @Override
    public void reset() {
        settingsComponent.setValePathText(settings.valePath);
        settingsComponent.setValeVersion(settings.valeVersion.toString());
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
