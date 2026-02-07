package org.ideplugins.vale_cli_plugin.settings;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.PROJECT)
@State(name="org.ideplugins.vale_cli_plugin_project.settings", storages = {@Storage(StoragePathMacros.WORKSPACE_FILE)})
public final class ValePluginProjectSettingsState implements PersistentStateComponent<ValePluginProjectSettingsState.State> {

    private final @NotNull Project project;

    public static class State {
        public String valeSettingsPath = "";
        public boolean runSyncOnStartup;
        public String extensions = "md,adoc,rst";
        public String guessedConfig = "";
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

    @Override
    public void initializeComponent() {
        if (state.valeSettingsPath.isEmpty()){
            String temp = OSUtils.findValeBinaryPath();
        }
    }

    public void setValeSettingsPath(String path){
        state.valeSettingsPath = path;
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

}
