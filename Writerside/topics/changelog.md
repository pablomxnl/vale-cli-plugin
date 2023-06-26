What's new
=====
This document provides an overview of the changes by release.

0.0.11
------

-   Fixes
    [\#18](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/18) NPE
    reported by plugin user in sentry in writeTextToConsole

-   Fixes
    [\#17](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/18) NPE
    reported by plugin user in sentry when executing Tools Menu Action

0.0.10
------

-   Fix plugin exception in external annotator

0.0.9
-----

-   Add plugin update notification

-   Add error reporter

-   Fix minor issue where vale error reported on last line of a file was
    not annotated

0.0.8
-----

-   Fixes
    [\#15](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/15)
    exception in external annotator when editing a file

-   Fixes
    [\#16](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/16) On
    windows, popup action doesn’t show results in problem view

0.0.7
-----

-   Execute vale binary for project as a background task, allowing to
    cancel

-   Limit one vale execution per project at a time

0.0.6
-----

-   fix [\#14](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues/14)
    NPE when no annotations are returned for a file

-   Add cmd+shift+0 shortcut to execute vale on project, cmd+shift+1 to
    execute vale in current file

0.0.5
-----

-   Reparse currently opened files so the external annotators reflects
    the problem view faster

-   Added notification action to show the settings screen when can’t
    setup Vale correctly

0.0.4
-----

-   Report results in problem view

-   Fixed issue when switching projects

0.0.3
-----

-   Fixed issue in chrome os where vale binary wasn’t autodetected in
    system path

-   Vale configuration file now optional, let the binary do it’s magic
    to find the configuration

0.0.2
-----

-   Autodetect if Vale CLI is in system path

0.0.1 Initial version
---------------------

-   Check current file

-   Check all files in project

-   Check multiple files selected in project tree (that have an
    extension matching the configured files)