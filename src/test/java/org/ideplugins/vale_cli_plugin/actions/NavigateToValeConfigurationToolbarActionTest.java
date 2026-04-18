package org.ideplugins.vale_cli_plugin.actions;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.ideplugins.vale_cli_plugin.settings.ValePluginProjectSettingsState;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NavigateToValeConfigurationToolbarActionTest {

    @Test
    public void testDoActionPerformedWithValidPath() {
        Project project = mock(Project.class);
        Application application = mock(Application.class);
        ValePluginProjectSettingsState settings = new ValePluginProjectSettingsState(project);
        settings.setValeSettingsPath("/my/path/.vale.ini");
        
        LocalFileSystem localFileSystem = mock(LocalFileSystem.class);
        VirtualFile virtualFile = mock(VirtualFile.class);
        when(localFileSystem.refreshAndFindFileByPath("/my/path/.vale.ini")).thenReturn(virtualFile);
        
        FileEditorManager fileEditorManager = mock(FileEditorManager.class);
        
        NavigateToValeConfigurationToolbarAction action = new NavigateToValeConfigurationToolbarAction();
        action.doActionPerformed(project, application, settings, localFileSystem, fileEditorManager);
        
        // Capture the runnable given to invokeLater
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(application).invokeLater(runnableCaptor.capture());
        
        // Execute the runnable
        runnableCaptor.getValue().run();
        
        // Ensure FileEditorManager was called correctly
        verify(fileEditorManager).openFile(virtualFile, true);
    }
    
    @Test
    public void testDoActionPerformedWithRootIni() {
        Project project = mock(Project.class);
        Application application = mock(Application.class);
        ValePluginProjectSettingsState settings = new ValePluginProjectSettingsState(project);
        settings.setValeSettingsPath("");
        settings.setRootIni("/root/.vale.ini");
        
        LocalFileSystem localFileSystem = mock(LocalFileSystem.class);
        VirtualFile virtualFile = mock(VirtualFile.class);
        when(localFileSystem.refreshAndFindFileByPath("/root/.vale.ini")).thenReturn(virtualFile);
        
        FileEditorManager fileEditorManager = mock(FileEditorManager.class);
        
        NavigateToValeConfigurationToolbarAction action = new NavigateToValeConfigurationToolbarAction();
        action.doActionPerformed(project, application, settings, localFileSystem, fileEditorManager);
        
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(application).invokeLater(runnableCaptor.capture());
        
        runnableCaptor.getValue().run();
        verify(fileEditorManager).openFile(virtualFile, true);
    }
    
    @Test
    public void testDoActionPerformedWithEmptyPath() {
        Project project = mock(Project.class);
        Application application = mock(Application.class);
        ValePluginProjectSettingsState settings = new ValePluginProjectSettingsState(project);
        settings.setValeSettingsPath("");
        settings.setRootIni("");
        
        LocalFileSystem localFileSystem = mock(LocalFileSystem.class);
        FileEditorManager fileEditorManager = mock(FileEditorManager.class);
        
        NavigateToValeConfigurationToolbarAction action = new NavigateToValeConfigurationToolbarAction();
        action.doActionPerformed(project, application, settings, localFileSystem, fileEditorManager);
        
        // Ensure invokeLater and refreshAndFindFileByPath were never called
        verify(application, never()).invokeLater(any());
        verify(localFileSystem, never()).refreshAndFindFileByPath(any());
    }
    
    @Test
    public void testDoActionPerformedWithFileNotFound() {
        Project project = mock(Project.class);
        Application application = mock(Application.class);
        ValePluginProjectSettingsState settings = new ValePluginProjectSettingsState(project);
        settings.setValeSettingsPath("/invalid/path/.vale.ini");
        
        LocalFileSystem localFileSystem = mock(LocalFileSystem.class);
        // file not found -> returns null
        when(localFileSystem.refreshAndFindFileByPath("/invalid/path/.vale.ini")).thenReturn(null);
        
        FileEditorManager fileEditorManager = mock(FileEditorManager.class);
        
        NavigateToValeConfigurationToolbarAction action = new NavigateToValeConfigurationToolbarAction();
        action.doActionPerformed(project, application, settings, localFileSystem, fileEditorManager);
        
        verify(application, never()).invokeLater(any());
    }
}
