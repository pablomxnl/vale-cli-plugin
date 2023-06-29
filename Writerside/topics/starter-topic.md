# Vale CLI Plugin Documentation

Plugin for JetBrains IDE's that uses [Vale CLI](https://vale.sh) to lint markdown,asciidoc and other documentation formats

## Installation

> **NOTE:**
>
>Make sure to have the [pre-requisites](pre-requisites.md) installed.
>
{style="note"}

<tabs>
    <tab title="From Jetbrains Plugin Marketplace">
    <kbd>Settings</kbd> (or <kbd>Preferences</kbd> if using mac) -> <kbd>Plugins</kbd> -> <kbd>Marketplace</kbd> Search for Vale CLI -> Click <control>Install</control>
    <img src="install_plugin.png" border-effect="line" alt="Install plugin" /> 

</tab>
    <tab title="From Gitlab Releases">
To install, grab a zip file from 
<a href="https://gitlab.com/pablomxnl/vale-cli-plugin/-/releases">gitlab releases</a> and then install it by going to
<kbd>Settings</kbd> (or <kbd>Preferences</kbd> if using mac) -> <kbd>Plugins</kbd> -> <kbd>⚙</kbd> -> <kbd>Install Plugin from Disk...</kbd>
<img src="install_plugin_from_disk.png" border-effect="line" alt="Install plugin from disk" /> 
Then select the zip file just downloaded from gitlab releases.
    </tab>
</tabs>



## Configuration

The plugin has 3 setings
To setup this up go to 

<kbd>Settings</kbd> (or <kbd>Preferences</kbd> if using mac) -> <kbd>Tools</kbd> -> <kbd>Vale CLI</kbd> 

![Plugin settings](plugin_settings.png){border-effect="line"}

1. Vale full absolute path: Only required when the plugin fails to find it's location by executing `which vale` or `where vale.exe` commands.
2. Vale settings file location (`.vale.ini` or `_vale.ini`): required when want to use a configuration file located in a path different from [where the binary looks for it in it's search process](https://vale.sh/docs/topics/config/#search-process).  
3. File extensions to lint, by default set to `adoc,md,rst` ; requires at least one extension. 

Alternatively, whenever the plugin is invoked with incomplete configuration, the settings can be entered by clicking on the notification link:

![Plugin settings from notification](plugin_settings_when_not_configured.png){border-effect="line"}


## Usage

To lint documentation files, do one of the following:

* Using the global tools menu action: <kbd>Tools</kbd> -> <kbd>Vale CLI Check</kbd>
  ![Tools menu action](usage_tools_menu.png){ border-effect="line" }
* Using the popup action in the editor window, right click on the editor and select `Vale CLI Check`
  ![Editor popup action](usage_editor_context_menu.png){ border-effect="line" }
* Using the popup action on the project view by selecting one or more configured files 
  ![Project view popup action](usage_project_context_menu.png){border-effect="line"}

## Results

The Vale CLI alerts are displayed in 

* The plugin tool window as a summary of the types of alerts found. Only displayed when using the Global Tools Menu Action.
* On the problem view
* As annotations on the editor of a supported file.

### Results in plugin tool window

<img src="results_toolwindow.png" alt="Results in plugin window and editor annotator" border-effect="line" />

The plugin tool window will show a summary of the number of alerts per type the Vale CLI founnd.

### Results in problem view

<img src="results_problemview_annotator.png" alt="Results in problem view" border-effect="line" />

The Vale CLI alerts can also be visualized on the problem view clicking on them will navigate to the line 
where Vale CLI found the problem.

## Feedback 
Please report any issues or feature requests at
[Gitlab Issues](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues) (a gitlab.com account is required).
