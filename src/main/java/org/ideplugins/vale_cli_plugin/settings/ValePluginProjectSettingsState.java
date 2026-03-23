package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.PROJECT)
@State(name="org.ideplugins.vale_cli_plugin_project.settings", storages = {@Storage(value = "vale-cli.xml", roamingType = RoamingType.DEFAULT)})
public final class ValePluginProjectSettingsState implements PersistentStateComponent<ValePluginProjectSettingsState.State> {

    private final @NotNull Project project;

    public static class State {
        public String valeSettingsPath = "";
        public boolean runSyncOnStartup;
        public boolean restrictChecksToConfiguredExtensions = true;
        public String extensions = "md,adoc,rst";
        public String rootIni = "";
    }
    
    public ValePluginProjectSettingsState(@NotNull Project theProject){
        project = theProject;
    }

    private State state = new State();

    public static ValePluginProjectSettingsState getInstance(@NotNull Project project){
       return project.getService(ValePluginProjectSettingsState.class);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }


    public void setValeSettingsPath(String path){
        state.valeSettingsPath = FileUtil.toSystemIndependentName(path);
    }

    public String getValeSettingsPath(){
        return state.valeSettingsPath;
    }

    public void setExtensions(String extensions){
        state.extensions = extensions;
    }

    public String getExtensions(){
        return state.extensions;
    }

    public boolean getRunSyncOnStartup(){
        return state.runSyncOnStartup;
    }

    public void setRunSyncOnStartup(boolean b){
        state.runSyncOnStartup = b;
    }

    public boolean getRestrictChecksToConfiguredExtensions() {
        return state.restrictChecksToConfiguredExtensions;
    }

    public void setRestrictChecksToConfiguredExtensions(boolean value) {
        state.restrictChecksToConfiguredExtensions = value;
    }

    public String getRootIni(){
        return state.rootIni;
    }

    public void setRootIni(String rootIni){
        state.rootIni = FileUtil.toSystemIndependentName(rootIni);
    }
}
