package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.InsertPathAction;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.ResourceBundle;

public final class ValePluginProjectSettingsConfigurable implements Configurable {

    private final Project myProject;

    private final JPanel myMainPanel;
    private final JBCheckBox runSynOnStartup = new JBCheckBox();
    private final TextFieldWithBrowseButton configurationFilePath = createIniBrowseField();
    private ValePluginProjectSettingsState settings;
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("ValePlugin");

    @NotNull
    private String getConfigurationFilePathText() {
        return configurationFilePath.getText();
    }

    private void setConfigurationFilePathText(@NotNull String newText) {
        configurationFilePath.setText(newText);
    }

    private void setRunSyncOnStartup(boolean b) {
        runSynOnStartup.setSelected(b);
    }

    private TextFieldWithBrowseButton createIniBrowseField() {
        final FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
                .withTitle(BUNDLE.getString("vale.cli.plugin.project.settings.filechooser.title"))
                .withDescription(BUNDLE.getString("vale.cli.plugin.project.settings.filechooser.description"))
                .withShowHiddenFiles(true)
                .withFileFilter(file -> {
                    String fileName = file.getName();
                    return fileName.equals(".vale.ini") || fileName.equals("vale.ini") || fileName.endsWith(".ini");
                });
        TextFieldWithBrowseButton textField = new TextFieldWithBrowseButton();
        textField.addBrowseFolderListener(new TextBrowseFolderListener(fileChooserDescriptor));
        InsertPathAction.addTo(textField.getTextField(), fileChooserDescriptor);
        return textField;
    }

    public ValePluginProjectSettingsConfigurable(Project project) {
        myProject = project;
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel(BUNDLE.getString("vale.cli.plugin.project.settings.filechooser.label")), configurationFilePath,
                        1, false)
                .addLabeledComponent(new JBLabel(BUNDLE.getString("vale.cli.plugin.project.settings.sync.label")), runSynOnStartup,
                        1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Vale CLI Project Settings";
    }


    @Override
    public @Nullable JComponent createComponent() {
        settings = ValePluginProjectSettingsState.getInstance(myProject);
        return myMainPanel;
    }

    @Override
    public boolean isModified() {
        return !getConfigurationFilePathText().equals(settings.getValeSettingsPath()) ||
                !runSynOnStartup.isSelected() == settings.getRunSyncOnStartup();
    }

    @Override
    public void apply() throws ConfigurationException {
        File f = new File(getConfigurationFilePathText());
        if (!f.exists())
            throw new ConfigurationException(
                    BUNDLE.getString("vale.cli.plugin.project.settings.invalidfile.message"),
                    BUNDLE.getString("vale.cli.plugin.project.invalid.settings.title"));
        settings.setValeSettingsPath(getConfigurationFilePathText());
        settings.setRunSyncOnStartup(runSynOnStartup.isSelected());
    }

    @Override
    public void reset() {
        setConfigurationFilePathText(settings.getValeSettingsPath());
        setRunSyncOnStartup(settings.getRunSyncOnStartup());
    }
}
