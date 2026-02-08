# Vale CLI Plugin Documentation v %plugin_version%

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

### EAP versions

These versions are available either 

* when a new EAP is available (around 3 per year)
* or before releasing a new version of the plugin, a snapshot EAP version is available on the EAP channel.

To enable the EAP channel for this plugin copy the following URL

```
http://plugins.jetbrains.com/plugin/19613-vale-cli/versions/eap
``` 

and paste it on the IDE plugin repositories via <kbd>Plugins</kbd> -> <kbd>⚙</kbd> -> <kbd>Manage Plugin Repositories ...</kbd>

<img src="eap-vale-cli.png" alt="EAP Plugin repository" border-effect="line" />

## Configuration

The plugin has 2 configuration sections,

* To configure the vale binary path
* To configure the vale configuration file and if the plugin should sync vale styles on startup

### Vale CLI Settings

<kbd>Settings</kbd> (or <kbd>Preferences</kbd> if using mac) -> <kbd>Tools</kbd> -> <kbd>Vale CLI</kbd>  or use the Vale Global Settings toolbar button in the plugin toolwindow 

1. Vale executable location: Only required when the plugin fails to find it automatically by executing `which vale` or `where vale.exe` commands.

![Plugin settings](plugin_settings_vale_binary_location.webp){border-effect="line"}

Use the Auto Detect button if the field is empty or browse to locate the vale executable.

### Vale CLI Project Settings

![Plugin settings](project-settings-locate-vale-config-file.webp){border-effect="line"}

<kbd>Settings</kbd> (or <kbd>Preferences</kbd> if using mac) -> <kbd>Tools</kbd> -> <kbd>Vale CLI Project Settings</kbd>  or use the Project Settings toolbar button in the plugin toolwindow 
 
1. Vale settings file location (`.vale.ini` or `_vale.ini`): required when want to use a configuration file located in a path different from [where the binary looks for it in it's search process](https://vale.sh/docs/topics/config/#search-process).  
2. Synchronize vale styles on startup: If checked the plugin will execute `vale sync` when a project is opened. 
3. File extensions to lint, by default set to `adoc,md,rst` ; requires at least one extension. 



## Plugin tool window

The plugin tool window has a toolbar with the following buttons

* Feature request
* Bug report
* Vale Project Settings
* Vale Global Settings
* Sync Vale Styles action

The tool window will display any errors with the configuration of the plugin.

## Results

The Vale CLI alerts are displayed 

* On the problem view
* As markers on the editor of a supported file.

### Results in problem view

<img src="results_problemview_annotator.png" alt="Results in problem view" border-effect="line" />

The Vale CLI alerts can be visualized on the problem view. 
Clicking on them will navigate to the line where Vale CLI found the problem.  
Additionally, these alerts are also visible on the editor as markers.

## Quick fixes

When the Vale CLI suggests a fix,  the plugin will offer a Quick Fix action on the problem view and on the editor.

<img src="quick_fix_replace.png" alt="Quick fixes replace " border-effect="line" />

<img src="quick_fix_remove.webp" alt="Quick fixes remove" border-effect="line" />
