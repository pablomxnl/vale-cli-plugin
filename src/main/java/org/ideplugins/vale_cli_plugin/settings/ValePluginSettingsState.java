package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static  java.util.AbstractMap.SimpleEntry;
import static  java.util.Map.Entry;


@State(
        name = "org.ideplugins.vale_cli_plugin.settings",
        storages = {@Storage("valeCliSettings.xml")}
)
@Service(Service.Level.APP)
final public class ValePluginSettingsState implements PersistentStateComponent<ValePluginSettingsState> {

    @Override
    public void initializeComponent() {
        String path = OSUtils.findValeBinaryPath();
        if (path.isEmpty() && !OSUtils.valeVersion().isEmpty()){
            valePath = SystemInfo.isWindows? "vale.exe" : "vale";
        } else {
            valePath = path;
        }
    }

    public String valePath = "";

    public String valeSettingsPath = "";
    public String extensions = "md,adoc,rst";

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
        StringBuilder errors = new StringBuilder("Invalid Vale CLI plugin configuration, " +
                "please click on the Notification link to configure it. \n" +
                "Or check it on Settings(Preferences on Mac) -> Tools -> Vale CLI\nError list: \n");
        boolean validationResult = true;
        if (StringUtils.isBlank(valePath)) {
            errors.append("\n* Vale path couldn't be detected automatically, please set it up");
            validationResult = false;
        }
        if (StringUtils.isNotBlank(valeSettingsPath)) {
            File file = new File(valeSettingsPath);
            if (!file.exists()) {
                errors.append("\n* Vale settings file doesn't exist");
                validationResult = false;
            }
        }
        if (StringUtils.isBlank(extensions)) {
            errors.append("\n* Vale extensions to check is not set.");
            validationResult = false;
        }
        return new SimpleEntry<>(validationResult, errors.toString());
    }
}
