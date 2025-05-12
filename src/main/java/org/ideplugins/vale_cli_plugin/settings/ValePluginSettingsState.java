package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static java.util.AbstractMap.SimpleEntry;
import static java.util.Map.Entry;


@State(
        name = "org.ideplugins.vale_cli_plugin.settings",
        storages = {@Storage("valeCliSettings.xml")}
)
@Service(Service.Level.APP)
public final class ValePluginSettingsState implements PersistentStateComponent<ValePluginSettingsState> {

    private static final Logger LOG = Logger.getInstance(ValePluginSettingsState.class);
    @Override
    public void initializeComponent() {
        if (valePath.isEmpty() ){
            valePath = OSUtils.findValeBinaryPath();
            valeVersion = OSUtils.valeVersion(valePath);
            LOG.info( String.format("Found vale version:%s  executable:%s" ,  valeVersion, valePath));
        }
    }

    public String valePath = "";
    public String valeSettingsPath = "";
    public String extensions = "md,adoc,rst";
    public String valeVersion = "";

    @Override
    public ValePluginSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ValePluginSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static ValePluginSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(ValePluginSettingsState.class);
    }

    public Entry<Boolean,String> areSettingsValid(){
        StringBuilder errors = new StringBuilder("""
                Invalid Vale CLI plugin configuration, please click on the Notification link to configure it.\s
                Or check it on Settings(Preferences on Mac) -> Tools -> Vale CLI
                Error list:\s
                """);
        boolean validationResult = true;
        if (StringUtils.isBlank(valePath)) {
            errors.append("\n* Vale path couldn't be detected automatically, please set it up");
            validationResult = false;
        }
        if (StringUtils.isNotBlank(valeSettingsPath)) {
            File file = new File(valeSettingsPath);
            if (!file.exists()) {
                errors.append("\n* Vale settings file ").append(file.getAbsolutePath()).append(" doesn't exist");
                validationResult = false;
            }
        }
        if (StringUtils.isBlank(extensions)) {
            errors.append("\n* Vale extensions to check is not set.");
            validationResult = false;
        }
        return new SimpleEntry<>(validationResult, errors.toString());
    }

    public List<String> extensionsAsList(){
        return Arrays.asList(extensions.split(","));
    }

}
