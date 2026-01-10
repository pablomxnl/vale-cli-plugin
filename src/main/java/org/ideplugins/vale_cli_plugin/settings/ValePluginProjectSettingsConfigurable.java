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
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.ResourceBundle;

public final class ValePluginProjectSettingsConfigurable implements Configurable {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("ValePlugin");
    private final Project myProject;
    private final JPanel myMainPanel;
    private final JBCheckBox runSyncOnStartup = new JBCheckBox();
    private final JBTextField extensionsTextField = new JBTextField();
    private final TextFieldWithBrowseButton configurationFilePath = createIniBrowseField();
    private ValePluginProjectSettingsState settings;

    public ValePluginProjectSettingsConfigurable(Project project) {
        myProject = project;
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel(BUNDLE.getString("vale.cli.plugin.project.settings.filechooser.label")), configurationFilePath,
                        1, false)
                .addLabeledComponent(new JBLabel(BUNDLE.getString("vale.cli.plugin.project.settings.sync.label")), runSyncOnStartup,
                        2, false)
                .addLabeledComponent(new JBLabel(
                        "<html><body>File extensions to check.<br/>Default:adoc,md,rst <br/>Examples: adoc,md,rst,py,rs,java</body></html>"), extensionsTextField, 3, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

    }

    @NotNull
    private String getConfigurationFilePathText() {
        return configurationFilePath.getText();
    }

    private void setConfigurationFilePathText(@NotNull String newText) {
        configurationFilePath.setText(newText);
    }

    private void setRunSyncOnStartup(boolean b) {
        runSyncOnStartup.setSelected(b);
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
                !runSyncOnStartup.isSelected() == settings.getRunSyncOnStartup() ||
                !getExtensionsText().equals(settings.getExtensions());
    }

    @Override
    public void apply() throws ConfigurationException {
        validateValues();
        settings.setValeSettingsPath(getConfigurationFilePathText());
        settings.setExtensions(getExtensionsText());
        settings.setRunSyncOnStartup(runSyncOnStartup.isSelected());
    }

    private void validateValues() throws ConfigurationException {
        String configurationFile = getConfigurationFilePathText();
        String extensions = getExtensionsText();
        StringBuilder errors = new StringBuilder();
        if (!configurationFile.isEmpty()) {
            File f = new File(configurationFile);
            if (!f.exists()) {
                errors.append(BUNDLE.getString("vale.cli.plugin.project.settings.invalidfile.message")).append("\n");
            }
        }
        if (extensions.isBlank()) {
            errors.append(BUNDLE.getString("vale.cli.plugin.project.settings.invalid_extensions.message"));
        }
        if (!errors.isEmpty()) {
            throw new ConfigurationException(
                    errors.toString(),
                    BUNDLE.getString("vale.cli.plugin.project.invalid.settings.title"));
        }
    }

    @Override
    public void reset() {
        setConfigurationFilePathText(settings.getValeSettingsPath());
        setRunSyncOnStartup(settings.getRunSyncOnStartup());
        setExtensionsText(settings.getExtensions());
    }

    @NotNull
    public String getExtensionsText() {
        return extensionsTextField.getText();
    }

    public void setExtensionsText(@NotNull String newValue) {
        extensionsTextField.setText(newValue);
    }

}
