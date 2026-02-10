package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.InsertPathAction;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ItemEvent;

public class ValePluginSettingsComponent {

    private final JPanel myMainPanel;
    private final TextFieldWithBrowseButton configurationFilePath = createIniBrowseField();
    private final JBTextField extensionsTextField = new JBTextField();
    private final JBCheckBox installVale = new JBCheckBox("Download and Install Vale CLI?");
    private final JBCheckBox syncValeConfigOnStartup = new JBCheckBox("Sync vale config on startup?");


    public boolean getSyncValeConfigOnStartup() {
        return syncValeConfigOnStartup.isSelected();
    }

    public boolean getInstallVale() {
        return installVale.isSelected();
    }

    public void setInstallVale(boolean b){
        installVale.setSelected(b);
    }

    public void setSyncValeConfigOnStartup(boolean b){
        syncValeConfigOnStartup.setSelected(b);
    }

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
        final FileChooserDescriptor fileChooserDescriptor =
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
                .addLabeledComponent(new JBLabel("<html>" +
                        "If unchecked vale must be on system path"), installVale, 1,false)
                .addComponent(syncValeConfigOnStartup, 2)
                .addLabeledComponent(new JBLabel("Enter .vale.ini full absolute path"), configurationFilePath, 3, false)
                .addLabeledComponent(new JBLabel("File extensions to check"), extensionsTextField, 4, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JComponent getPreferredFocusedComponent() {
        return installVale;
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    @NotNull
    public String getExtensionsText() {
        return extensionsTextField.getText();
    }

    public void setExtensionsText(@NotNull String newValue) {
        extensionsTextField.setText(newValue);
    }

    @NotNull
    public String getConfigurationFilePathText() {
        return configurationFilePath.getText();
    }

    public void setConfigurationFilePathText(@NotNull String newText) {
        configurationFilePath.setText(newText);
    }
}
