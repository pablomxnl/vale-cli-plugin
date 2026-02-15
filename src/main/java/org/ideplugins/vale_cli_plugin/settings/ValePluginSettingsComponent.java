package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.InsertPathAction;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

public class ValePluginSettingsComponent {

    private final JPanel myMainPanel;
    private final JBLabel valeVersion = new JBLabel();
    private final TextFieldWithBrowseButton valePath = createPathBrowseField();

    public ValePluginSettingsComponent() {
        JButton locateValeButton = createButton();
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Enter vale executable location"), valePath, 1, false)
                .addLabeledComponent(new JBLabel("Vale version"), valeVersion, 2, false)
                .addLabeledComponent(new JBLabel("Auto detect"), locateValeButton, 3, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private JButton createButton() {
        JButton result = new JButton("Auto Detect");
        result.addActionListener(e -> ApplicationManager.getApplication().executeOnPooledThread(() -> {
            valePath.setText(OSUtils.findValeBinaryPath());
            String rawVersion = OSUtils.valeVersion(valePath.getText());
            valeVersion.setText(ValeVersion.parse(rawVersion).toString());
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
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    String rawVersion = OSUtils.valeVersion(textField.getText());
                    valeVersion.setText(ValeVersion.parse(rawVersion).toString());
                });
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

    public void setValeVersion(String newVersion) {
        valeVersion.setText(newVersion);
    }

    @NotNull
    public ValeVersion getValeVersion() {
        return ValeVersion.parse(valeVersion.getText());
    }
}
