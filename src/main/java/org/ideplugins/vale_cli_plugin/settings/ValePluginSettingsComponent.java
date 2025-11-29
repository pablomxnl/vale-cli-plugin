package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.InsertPathAction;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

public class ValePluginSettingsComponent {

    private final JPanel myMainPanel;
    private final JBTextField extensionsTextField = new JBTextField();
    private final JBLabel valeVersion = new JBLabel();
    private final TextFieldWithBrowseButton valePath = createPathBrowseField();

    public ValePluginSettingsComponent() {
        JButton locateValeButton = createButton();
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Enter vale executable location"), valePath, 1, false)
                .addLabeledComponent(new JBLabel("Vale version"), valeVersion, 2, false)
                .addLabeledComponent(new JBLabel("Auto detect"), locateValeButton, 3, false)
                .addLabeledComponent(new JBLabel(
                        "<html><body>File extensions to check.<br/>Default:adoc,md,rst <br/>Examples: adoc,md,rst,py,rs,java</body></html>"), extensionsTextField, 4, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private JButton createButton() {
        JButton result = new JButton("Auto Detect");
        result.addActionListener(e -> ApplicationManager.getApplication().executeOnPooledThread(() -> {
            valePath.setText(OSUtils.findValeBinaryPath());
            valeVersion.setText(OSUtils.valeVersion(valePath.getText()));
        }));
        return result;
    }

    private TextFieldWithBrowseButton createPathBrowseField() {
        FileChooserDescriptor fileChooserDescriptor =
                FileChooserDescriptorFactory.createSingleFileDescriptor()
                        .withTitle("Provide Vale Binary Location")
                        .withDescription("Locate your vale binary");
        TextFieldWithBrowseButton textField = new TextFieldWithBrowseButton();
        textField.addBrowseFolderListener(new TextBrowseFolderListener(fileChooserDescriptor));
        textField.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent documentEvent) {
                ApplicationManager.getApplication().executeOnPooledThread(() -> valeVersion.setText(OSUtils.valeVersion(textField.getText())));
            }
        });
        InsertPathAction.addTo(textField.getTextField(), fileChooserDescriptor);

        return textField;
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

    public void setValeVersion(String newVersion) {
        valeVersion.setText(newVersion);
    }

    public String getValeVersionText() {
        return valeVersion.getText();
    }
}
