# Feedback

This project track issues in [Gitlab Issues](https://gitlab.com/pablomxnl/vale-cli-plugin/-/issues)
(You must have a gitlab.com account to create an issue).

There are several ways to give feedback.

Bug reports submitted through the plugin toolwindow toolbar bug, 
given it gathers versioning information needed for troubleshooting.

## Feature request and Bug report plugin toolwindow toolbar actions

<img src="toolbar_feedback_actions.png" alt="Toolwindow toolbar actions" />

Since version 0.0.22 the plugin tool window has a toolbar with a bug report and a feature request button.

Each one of these buttons opens your default browser to create a Gitlab issue with a predefined template, for the bug report it gathers automatically information about your specific JetBrains IDE such as name, version, java JDK version used, operating system and version and version of the plugin installed (you must have a Gitlab account).

## Automatic error reporting

Sometimes fatal errors like the pesky NPE or other runtime exceptions ship with the plugin. When this happens there is a little blinking notification with a link "See details and submit report".

<img src="error_handler_notification.png" alt="Notification of a fatal internal error" />

If clicked the submit error report screen it's shown as follows

<img src="error_handler_report.png" alt="Notification of a fatal internal error" />

Similar to the bug report it gathers automatically information about the IDE, Operating System,JDK and plugin version installed and submits this information to Sentry. The author then gets a notification and possibly later you will see a new gitlab issue with `sentry` label.

Except, if the plugin is outdated, the error it is ignored and a notification like the following appears after submitting the error report:

<img src="error_handler_report_outdated_version.png" alt="Notification of error report ignored due to outdated plugin version" />

## Information sent by the error reporter
The following is an example of the information being submitted:

<img src="sentry_error_report_1.png" alt="Sentry error report 1" />

<img src="sentry_error_report_2.png" alt="Sentry error report 2" />