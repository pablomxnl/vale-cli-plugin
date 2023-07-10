# Pre-requisites

The plugin requires at least the Vale CLI tool installed, optional dependency for asciidoctor and docutils if planning to lint asciidoctor or ReStructured text files.


> **Make sure these dependencies are on your system path**
>
> For the Vale CLI, the plugin will try to find it's location, if it fails, there is still the option to specify it's location on the [plugin settings](starter-topic.md#configuration). For asciidoctor or docutils, these need to be included in the <code>$PATH</code> environment variable, as the Vale CLI binary will try to invoke them.
>
{style="note"}


## Vale CLI installation (required) and styles configuration

### Vale CLI binary installation
  <tabs>
      <tab title="Linux">
      1. Using snap
        <code-block>
        sudo snap install vale --edge
        </code-block>
      2. Using brew
        <code-block>
          brew install vale
        </code-block>
      3. Download a pre-compiled binary from <a href="https://github.com/errata-ai/vale/releases">Vale Github Releases</a> and put it on your system <code>$PATH</code> environment variable.
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

### Vale config file generation
Once the Vale CLI binary has been installed, a configuration file has to be created.

This a minimal configuration to get started, place this into a file named either `.vale.ini` or `_vale.ini` at the root of the project, or on the user's home directory (for Linux/Mac usually contained in `$HOME` environment variable, for Windows usually contained in `%\USERPROFILE%` environment variable).

```
StylesPath = styles
MinAlertLevel = suggestion
Packages = write-good
[*]
BasedOnStyles = Vale, write-good
```

A more complete configuration file can be created using the [Vale CLI Config Generator](https://vale.sh/generator/).


Once this file exists run the following command from the path where the file has been created:

```
vale sync
```

This command will download the style files according to the configuration.

## Asciidoctor installation (optional)
Asciidoctor is required only if planning to lint asciidoc files, they asciidoctor binary must be in the system PATH for the vale binary to execute.
<tabs>
      <tab title="Linux">
      1. Using gem 
        <code-block>
        gem install asciidoctor
        </code-block>
      2. Using brew
        <code-block>
          brew install asciidoctor
        </code-block>
      3. Using apt or the linux distribution package manager
        <code-block>
            sudo apt-get install -y asciidoctor
        </code-block>
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

## Docutils installation (optional)
Docutils (rst2html in particular) is required only if planning to lint ReStructured text files (with extension rst)
<tabs>
      <tab title="Linux">
      1. Using apt or the linux distribution package manager  
        <code-block>
            apt-get install python-docutils
        </code-block>
      2. Using brew
        <code-block>
          brew install docutils
        </code-block>
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
