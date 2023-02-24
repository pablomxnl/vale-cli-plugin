package org.ideplugins.plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static  java.util.AbstractMap.SimpleEntry;
import static  java.util.Map.Entry;


@State(
        name = "org.ideplugins.vale_cli_plugin.settings",
        storages = {@Storage("valeCliSettings.xml")}
)
public class ValePluginSettingsState implements PersistentStateComponent<ValePluginSettingsState> {

    public String valePath;

    public String valeSettingsPath = "";
    public String extensions = "md,adoc,rst";

    @Nullable
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

    @Override
    public void initializeComponent() {
        if (valePath == null){
            valePath = OSUtils.findValeBinaryPath();
        }
        PersistentStateComponent.super.initializeComponent();
    }

    public Entry<Boolean,String> areSettingsValid(){
        StringBuilder errors = new StringBuilder("Invalid Vale CLI plugin configuration, " +
                "please click on the Notification link to configure it. \n" +
                "Or check it on Settings(Preferences on Mac) -> Tools -> Vale CLI\nError list: \n");
        boolean validation = true;
        if (StringUtils.isBlank(valePath)) {
            errors.append("\n* Vale path couldn't be detected automatically, please set it up");
            validation = false;
        }
        if (StringUtils.isNotBlank(valeSettingsPath)) {
            File file = new File(valeSettingsPath);
            if (!file.exists()) {
                errors.append("\n* Vale settings file doesn't exist");
                validation = false;
            }
        }
        if (StringUtils.isBlank(extensions)) {
            errors.append("\n* Vale extensions to check is not set.");
            validation = false;
        }
        return new SimpleEntry<>(validation, errors.toString());
    }
}
