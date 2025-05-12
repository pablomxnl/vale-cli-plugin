package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.InsertPathAction;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;

public class ValePluginSettingsComponent {

    private final JPanel myMainPanel;
    private final TextFieldWithBrowseButton valePath = createPathBrowseField();
    private final TextFieldWithBrowseButton configurationFilePath = createIniBrowseField();
    private final JBTextField extensionsTextField = new JBTextField();
    private final JBLabel valeVersion = new JBLabel();


    private TextFieldWithBrowseButton createIniBrowseField() {
        final FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
            .withTitle("Provide Vale Configuration File")
            .withDescription("Locate your .vale.ini file")
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

    private TextFieldWithBrowseButton createPathBrowseField() {
        FileChooserDescriptor fileChooserDescriptor =
                FileChooserDescriptorFactory.createSingleFileDescriptor()
                        .withTitle("Provide Vale Binary Location")
                        .withDescription("Locate your vale binary");
        TextFieldWithBrowseButton textField = new TextFieldWithBrowseButton();
        textField.addBrowseFolderListener(new TextBrowseFolderListener(fileChooserDescriptor));
        InsertPathAction.addTo(textField.getTextField(), fileChooserDescriptor);
        return textField;
    }

    public ValePluginSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Enter vale executable location"), valePath, 1, false)
                .addLabeledComponent(new JBLabel("Vale version"),valeVersion, 2, false)
                .addLabeledComponent(new JBLabel("Enter .vale.ini full absolute path"), configurationFilePath, 3, false)
                .addLabeledComponent(new JBLabel("File extensions to check"), extensionsTextField, 4, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JComponent getPreferredFocusedComponent() {
        return valePath;
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    @NotNull
    public String getValePathText() {
        return valePath.getText();
    }

    public void setValePathText(@NotNull String newText) {
        valePath.setText(newText);
    }

    @NotNull
    public String getExtensionsText() {
        return extensionsTextField.getText();
    }

    public void setExtensionsText(@NotNull String newValue) {
        extensionsTextField.setText(newValue);
    }

    public void setValeVersion(String newVersion){
        valeVersion.setText(newVersion);
    }

    @NotNull
    public String getConfigurationFilePathText() {
        return configurationFilePath.getText();
    }

    public void setConfigurationFilePathText(@NotNull String newText) {
        configurationFilePath.setText(newText);
    }
}
