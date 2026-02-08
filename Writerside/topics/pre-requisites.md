# Pre-requisites

The plugin requires:

* Vale CLI tool installed and available in the system path.
* To support _AsciiDoc_ files, the plugin requires asciidoctor available in the system path. (optional)
* To support _reStructuredText_ files, the plugin requires docutils available in the system path. (optional)

> **Make sure these dependencies are available on your system path**
>
> For the Vale CLI executable, the plugin tries to guess it's location, if it fails, there is still the option to specify the location on the [plugin settings](starter-topic.md#configuration). For asciidoctor or docutils, these
> need to be included in the <code>$PATH</code> environment variable, as the Vale CLI binary invokes them from there.
>
{style="note"}

## Vale CLI installation (required) and styles configuration

### Vale CLI binary installation

  <tabs>
      <tab title="Linux">
        <p>Use one of the following to install vale on linux</p>
        <list style="bullet">
            <li>The preferred way to install vale on linux is to download a pre-compiled binary from <a href="https://github.com/errata-ai/vale/releases">Vale Github Releases</a> and put it on your system <code>$PATH</code> environment variable. 
            </li>
            <li>Using snap
                <code-block>
                sudo snap install vale --edge
                </code-block>
            </li>
      <li>Using brew
        <code-block>
          brew install vale
        </code-block>
      </li>
        </list> 
      </tab>
      <tab title="Mac OS">
        <code-block>
          brew install vale
        </code-block>
      </tab>
      <tab title="Windows">
      <code-block>
        choco install vale -y
      </code-block>
      </tab>
  </tabs>

### Vale configuration file generation

After installing the Vale CLI binary, create a configuration file.

This is a minimal configuration to get started, place it on a file named either `.vale.ini` or `_vale.ini` at the root of the project, or on the user's home directory (for Linux/Mac usually contained in `$HOME`
environment variable, for Windows usually contained in `%\USERPROFILE%` environment variable).

```
StylesPath = styles
MinAlertLevel = suggestion
Packages = write-good
[*]
BasedOnStyles = Vale, write-good
```

A more complete configuration file can be created using the [Vale CLI Config Generator](https://vale.sh/generator/).

## Styles Sync

After creating a vale configuration file one needs to fetch those styles and rules, using the command

<code-block>
vale sync
</code-block>

This command is also executed with the `Sync` button in plugin tool window toolbar, or on project loaded
if this is enabled in the vale project settings.

<img src="vale_sync_toolbar_action.webp" alt="Notification of error report ignored due to outdated plugin version" />

## Asciidoctor installation (optional)

Asciidoctor is required only if planning to lint `AsciiDoc` files, the asciidoctor binary must be in the system PATH for the vale binary to execute it.
<tabs>
      <tab title="Linux"><p>Use one of the following methods to install asciidoctor</p>
      <list style="bullet">
      <li>Using gem 
        <code-block>
        gem install asciidoctor
        </code-block>
      </li>
      <li>Using brew
        <code-block>
          brew install asciidoctor
        </code-block></li>
      <li>Using your linux distribution package manager (other flavors as yum, dcnf, pacman and others not listed for brevity)
        <code-block>
            sudo apt-get install -y asciidoctor
        </code-block></li></list>
      </tab>
      <tab title="Mac OS">
        <code-block>
          brew install asciidoctor
        </code-block>
      </tab>
      <tab title="Windows">
      <code-block>
        choco install ruby -y
        gem install asciidoctor
      </code-block>
      </tab>
  </tabs>

## `Docutils` installation (optional)

Docutils (rst2html in particular) is required only if planning to lint ReStructured text files (with extension rst)
<tabs>
      <tab title="Linux">
        <list style="bullet">
        <li>Using apt or the linux distribution package manager  
        <code-block>
            sudo apt-get -y install python3-docutils
        </code-block>
        </li>
        <li>
       Using brew
        <code-block>
          brew install docutils
        </code-block>
        </li>
        </list>
      </tab>
      <tab title="Mac OS">
        <code-block>
          brew install docutils
        </code-block>
      </tab>
      <tab title="Windows">
      <code-block>
        choco install sphinx -y
      </code-block>
      </tab>
  </tabs>
