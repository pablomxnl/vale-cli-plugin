What's new
=====
This document provides an overview of the changes by release.

0.0.29
------

- Fixes [\#52](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/52) java.io.IOException: The pipe has been ended.
- Fixes [\#53](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/53) IllegalStateException: This method is forbidden on EDT

0.0.28
------

- Implements [\#28](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/28) Allow different vale config per project
- Implements [\#50](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/50) Add vale sync action
- Implements [\#51](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/51) Use stdin to lint files
- Closes [\#49](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/49) Error when no vale configuration file is found

---

**Removed**

- Tools menu action to lint all supported files in project
- Project view and editor popup actions to lint current file or multiple selected files
- Tool view popup action to lint all files in one project directory

0.0.27
------

- Updates to support `2025.3`. This is the last version supporting IDE versions `2024.*`

0.0.26
------

- Fixes [\#45](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/45) Ignore vale process execution error output

0.0.25
------

- Fixes [\#40](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/40) File linting fails when there are spaces in path
- Fixes [\#43](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/43) Linting multiple files from the project view only lints 1 file
- Implements [\#42](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/42) Add vale linting for source code

0.0.24
------

- Fixes [\#37](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/37) Diagnostic Freeze error report
- Fixes [\#38](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/38) Settings always show "Revert changes" with no changes
- Fixes [\#39](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/39) Run vale after document change doesn't work on windows

0.0.23
------

Implements [\#34](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/34) Add popup action to lint a folder

0.0.22
------

- Added toolbar with feedback buttons to plugin tool window
- Fixes [\#32](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/32) TimeoutException when executing ValePopupAction on single asciidoc file

0.0.21
------

- Update to support EAP 251

0.0.20
------

- Fixes [\#31](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/31) NPE reported by plugin user through sentry error reporter in ValePluginSettingsConfigurable


0.0.19
------

- Update to support EAP 243
- Fixes [\#30](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/30) menu vale action doesn't work on Windows 10
- Add file filter to vale configuration file chooser ( `vale.ini` | `.vale.ini` | `.ini` )

0.0.18
------

- Updates to support 2024.2 IDE versions

0.0.17
------

- Update pluginUntilBuild to support 2024.x IDE versions


0.0.16
------

- Implements [\#26](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/26) Show issues in Project Errors
- Fixes [\#27](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/27) JsonSyntaxException
- Fixes [\#29](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/29) NPE

0.0.15
------

- Fixes [\#23](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/23) asciidoctor installed - but plugin raises E100
  Runtime error asciidoctor not found
- Fixes [\#24](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/24) Forward `System.getenv()` as environment to
  Vale

0.0.14
------

- Fixes [\#21](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/21) PluginException 2264 ms to call on EDT
  ValePopupAction#update@GoToAction
- Update pluginUntilBuild to support 233 EAP

0.0.13
------

- Fixes [\#20](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/20) NPE reported by plugin user to sentry in
  TypedHandler

0.0.12
------

- [Run the Vale plugin on save](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/7)
- [Add fix intention action](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/19)

0.0.11
------

- Fixes
  [\#18](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/18) NPE
  reported by plugin user in sentry in writeTextToConsole

- Fixes
  [\#17](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/18) NPE
  reported by plugin user in sentry when executing Tools Menu Action

0.0.10
------

- Fix plugin exception in external annotator

0.0.9
-----

- Add plugin update notification

- Add error reporter

- Fix minor issue where vale error reported on last line of a file was
  not annotated

0.0.8
-----

- Fixes
  [\#15](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/15)
  exception in external annotator when editing a file

- Fixes
  [\#16](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/16) On
  windows, popup action doesn’t show results in problem view

0.0.7
-----

- Execute vale binary for project as a background task, allowing to
  cancel

- Limit one vale execution per project at a time

0.0.6
-----

- fix [\#14](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/14)
  NPE when no annotations are returned for a file

- Add cmd+shift+0 shortcut to execute vale on project, cmd+shift+1 to
  execute vale in current file

0.0.5
-----

- Reparse currently opened files so the external annotators reflects
  the problem view faster

- Added notification action to show the settings screen when can’t
  setup Vale correctly

0.0.4
-----

- Report results in problem view

- Fixed issue when switching projects

0.0.3
-----

- Fixed issue in chrome os where vale binary wasn’t autodetected in
  system path

- Vale configuration file now optional, let the binary do it’s magic
  to find the configuration

0.0.2
-----

- Autodetect if Vale CLI is in system path

0.0.1 Initial version
---------------------

- Check current file

- Check all files in project

- Check multiple files selected in project tree (that have an
  extension matching the configured files)
