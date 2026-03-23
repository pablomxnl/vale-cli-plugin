package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
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
import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

public class ValePluginSettingsComponent {

    private final JPanel myMainPanel;
    private final JBLabel valeVersion = new JBLabel(ValeVersion.UNKNOWN_VERSION_NAME);
    private final TextFieldWithBrowseButton valePath = createPathBrowseField();
    private final AtomicLong versionLookupCounter = new AtomicLong();

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
        result.addActionListener(e -> {
            result.setEnabled(false);
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                String detectedPath = "";
                try {
                    detectedPath = OSUtils.findValeBinaryPath();
                } finally {
                    String finalDetectedPath = detectedPath;
                    ApplicationManager.getApplication().invokeLater(
                            () -> {
                                valePath.setText(finalDetectedPath);
                                result.setEnabled(true);
                            },
                            ModalityState.any()
                    );
                }
            });
        });
        return result;
    }

    private TextFieldWithBrowseButton createPathBrowseField() {
        FileChooserDescriptor fileChooserDescriptor =
                FileChooserDescriptorFactory.singleFile()
                        .withTitle("Provide Vale Binary Location")
                        .withDescription("Locate your vale binary");
        TextFieldWithBrowseButton textField = new TextFieldWithBrowseButton();
        textField.addBrowseFolderListener(new TextBrowseFolderListener(fileChooserDescriptor));
        textField.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent documentEvent) {
                requestValeVersionUpdate(textField.getTextField(), textField.getText());
            }
        });
        InsertPathAction.addTo(textField.getTextField(), fileChooserDescriptor);

        return textField;
    }

    private void requestValeVersionUpdate(@NotNull JTextField pathField, @NotNull String candidatePath) {
        long lookupId = versionLookupCounter.incrementAndGet();
        String normalizedPath = candidatePath.trim();
        if (normalizedPath.isBlank()) {
            setValeVersion(ValeVersion.UNKNOWN_VERSION_NAME);
            return;
        }
        ApplicationManager.getApplication().executeOnPooledThread(() -> {

             File executablePath = new File(normalizedPath);
             if (!executablePath.exists() || !executablePath.canExecute()) {
                 setValeVersion(ValeVersion.UNKNOWN_VERSION_NAME);
                 return;
             }

            String rawVersion = OSUtils.valeVersion(normalizedPath);
            String parsedVersion = ValeVersion.parse(rawVersion).toString();
            ApplicationManager.getApplication().invokeLater(
                    () -> {
                        String currentPath = pathField.getText().trim();
                        boolean isLatestLookup = lookupId == versionLookupCounter.get();
                        if (isLatestLookup && normalizedPath.equals(currentPath)) {
                            setValeVersion(parsedVersion);
                        }
                    },
                    ModalityState.any()
            );
        });
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
        var app = ApplicationManager.getApplication();
        if (app.isDispatchThread()) {
            valeVersion.setText(newVersion);
            return;
        }
        app.invokeLater(() -> valeVersion.setText(newVersion), ModalityState.any());
    }

    @NotNull
    public ValeVersion getValeVersion() {
        return ValeVersion.parse(valeVersion.getText());
    }
}
