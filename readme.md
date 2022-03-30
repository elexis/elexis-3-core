# Elexis 3 Core Repository

[Build status of master branch](https://gitlab.medelexis.ch/elexis/elexis-3-core/badges/master/pipeline.svg)

## Install latest build

Go to [Jenkins-CI
Builds](https://download.elexis.info/elexis/master/products).

Download and unzip the zip file for your machine. An JRE 11 is bundled with the product.

Start the Elexis3 application.

Use Help..Install New Software to add the desired features from the
elexis-3-base P2 repository.

## Building

### Maven based build

You need Java 1.11. Maven \>= 3.6. On Debian 10 (buster) install adoptopenjdk-11-hotspot.
Then you should be able to generate the application and p2 update site using the following calls:


`git clone https://github.com/elexis/elexis-3-core`\
`mvn -V clean verify  -Dtycho.localArtifacts=ignore -DskipTests`

Explanation of the used options

-   -V: emits version of Java, Maven, GUI-Toolkit, Architecture. Handy
    when you ask a problem
-   clean: Build everything from scratch
-   verify: Compile, test and build a P2-site including products. But
    does NOT install maven artefacts
-   -Dtycho.localArtifacts=ignore: Do not use any locally built maven
    artefacts
-   -DskipTests: Skip unit tests (Use it only if you want to speed up
    the build)
-   ---quiet: Used for travis builds to generate Quiet output - only
    show errors. Without it the log would bee too long and make the
    travis-build fail.
-   If the environment variable MATERIALIZE_PRODUCTS is set to false, no
    zip files for all products will be produced, which speeds up the
    build

It will be built for your OS/Window-System/Architecture combination. You
will find the executables under
`ch.elexis.core.p2site/target/products/ch.elexis.core.application.ElexisApp/*/*/*`

Note: This will fail in MacOSX. Build instructions for Mac:

`Install Homebrew`\
`brew install homebrew/versions/maven30`\
`git clone https://github.com/elexis/elexis-3-core`\
`mvn clean install -DskipTests`

### Continuos Intergration builds

Medelexis sponsored a gitlab runner for the continuos integrations build. 
The status can be seen under 
[Build status of master branch](https://gitlab.medelexis.ch/elexis/elexis-3-core/badges/master/pipeline.svg)

The downloads produced by these runs can found under <http://download.elexis.info/elexis/>

### Building via Eclipse IDE

If you want a complete development environment, try
[Oomph](https://github.com/elexis/elexis-3-core/tree/master/ch.elexis.sdk)

This is the recommended (and the only supported) setup of an development environment
for Elexis developers.

### Eclipse IDE-Preferences

We recommend (and if you want to submit patches, you have to respect)
the following settings in the Eclipse IDE, reachabel via
Window..Preferences:

-   General..Workspace
    -   Select UTF-8 as "Textfile encoding"
    -   Select UNIX as "New text file line delimiter"
-   Java..Editor..Save Action
    -   Select: Perform the selected actions on save
    -   Do NOT select: Format source code
    -   Select: Organize imports
    -   Do NOT select: Additional actions
-   Maven..Errors/Warning
    -   Set "Plugin execution not covered by lifecycle configuration" to
        "Ignore"

### Code formatting

In March 2022 we decided to drop our custom `3lexisFormatterProfile` in favor
of the Eclipse built-in. This works better for newer Java features like annotations.
Also the line length increases from 100 to 120 chars, as we assume that few of
our developers still use VT-80 terminals.

To ensure a consistent look, we use the [this pre-commit framework](https://pre-commit.com/).
For details have look at the [.pre-commit-config.yaml](https:.pre-commit-config.yaml)

To use it, install the pre-commit framework, either via your distribution or via `pip install pre-commit`. Under Debian run `apt install pre-commit`. The call once `pre-commit install` to activate the pre-commit trigger for git.

It needs flatpak and an installed flatpak org.eclipse.jdt.core.Java app. Under Debian run the following commands

* `sudo apt install flatpak`
* `flatpak remote-add --user --if-not-exists flathub https://flathub.org/repo/flathub.flatpakrepo`
* `flatpak --assumeyes --user --noninteractive install flathub org.eclipse.Java`

The different parts can be run on the command line, eg. 

* For all files `pre-commit run --all-files file-contents-sorter`
* For a single file `pre-commit run file-contents-sorter --files 'bundles/ch.elexis.core/src/ch/elexis/core/model/ch/messages_it.properties`

You will see and output like

```
File Contents Sorter.....................................................Failed
- hook id: file-contents-sorter
- exit code: 1
- files were modified by this hook

Sorting bundles/ch.elexis.core/src/ch/elexis/core/model/ch/messages_it.properties
```

At the moment we use the following hooks:
* check-xml
* check-yaml
* file-contents-sorter
* trailing-whitespace
* mixed-line-ending
* end-of-file-fixer
* enforce-eclipse-format

As the enforce-eclipse-format takes a long time (over 60 seconds on my machine) it is only called manually and as a github action. To run in use `pre-commit run --hook-stage manual enforce-eclipse-format`

### Submitting patches

To submit patches you have to

-   Fork our github repository to your personal account
-   Clone this repository on your devevelopment machine
-   Create a new branch (eg. `git checkout -b my_branch`)
-   Commit your change (eg. `git commmit -m "My cool changes")
    * Push it to your account (eg. `git push ---set-upstream origin
    my_branch@)
-   Go to your github repository and create a pull requests by clicking
    on the greeen button "Compare & pull request"
-   Review your changes. There should be only the lines that contribute
    to the problem. If you have too many changes look at the remarks
    about Eclipse IDE-Preferences

## Developer Overview

This repository hosts the core Elexis 3. It consists of the following
plug-ins and features:

-   `ch.rgw.utility` Utilities required to drive Elexis.
-   `ch.elexis.core` Core concepts and interface definitions.
-   `ch.elexis.core.data` Core persistence and functionality packages
    and classes.
-   `ch.elexis.core.console.application` Headless Elexis application.
-   `ch.elexis.core.ui` User Interface dependent portions. Dependent on
    Eclipse RCP 3.x.
-   `ch.elexis.core.application` Core UI Elexis Application.

```{=html}
<!-- -->
```
-   `ch.elexis.core.releng` Release Engineering specific parts (Build
    Target, 3rd party ...)

```{=html}
<!-- -->
```
-   `ch.elexis.core.logging` Plug-In for starting ch.qos.logback logging
    (via slf4j interface)
-   `ch.elexis.core.logging.feature` Feature for logging and Felix-gogo
    console
-   `ch.elexis.core.logging.default_configuration` Default logging
    configuration (logback.xml)

```{=html}
<!-- -->
```
-   `ch.elexis.core.ui.icons` Plug-In for central icon management.
-   `ch.elexis.core.ui.contacts` Plug-In for contact management.
-   `ch.elexis.core.ui.laboratory` Plug-In for laboratory related tasks.
-   `ch.elexis.core.ui.p2` Plug-In to realize client side p2 update
    tasks
-   `ch.elexis.core.common.feature` Headless Core Feature.
-   `ch.elexis.core.ui.feature` Core UI Feature.
-   `ch.elexis.core.application.feature` Core Application Feature.

For details about the resp. plug-ins/features switch to the respective
directory. The plug-ins and features\
are contained and inter-dependent as follows:

![](FeatureStructure.png)

### Guidelines for developing a new plugin/feature

New plugins and features should follow the E4 (eclipse 4) guideline. A
good tutorial is from [Lars
Vogel](https://www.vogella.com/tutorials/EclipseRCP/article.html)

The ch.elexis.core.ui.tasks follows new e4 implementation rules. Analyse
its working and adapt it to your problem.

### Generate Javadoc

Elexis uses Javadoc to documents its API interface. The Eclipse IDE
offers built-in support for reading the javadoc for a given method or
class and provides also helper to generate it.

This is accomplished by calling
`mvn -DforceContextQualifier=javadoc javadoc:javadoc`. It generates a
complete javadoc. The generated output can be search at
[target/site/apidocs/index.html](target/site/apidocs/index.html).

Even when theses javadoc are not used often, they provide a convenient
way to get an overview over methods, packages, etc.

### Check whether Javadocs are correctly generated

We use checkstyle to generate Javadoc. Use the following command line:

`mvn checkstyle:checkstyle-aggregate site:site`

This generates an browsable HTML file
[target/site/index.html](target/site/index.html). The links to the
modules do not work, unless you call afterwards
`ch.elexis.core.releng/cleanup_after_checkstyle.rb`, a small ruby script
which generates a few missing links (not supported on Windows).

It uses the rules defined in `ch.elexis.core.releng/checkstyle.xml` to
generate warnings for missing Javadoc. For each project, there exists an
error report. E.g
[target/site/ch.rgw.utility/checkstyle.html](./target/site/ch.rgw.utility/checkstyle.html).
An aggregated, overall statistics can be found under
[target/site/checkstyle-aggregate.html](target/site/checkstyle-aggregate.html)

If you want to add even more checks, look at
ch.elexis.core.releng/checkstyle_full.xml. Adding new checks should be
discussed on the developers mailing list.

## Updating all versions for a newer ID

We do this for all new major version, eg. 3.3 -\> 3.4. See also
http://www.mojohaus.org/versions-maven-plugin/usage.html

Steps to follow are:

-   Update the version in master pom.xml
-   Run
    `mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=3.9.0-SNAPSHOT`
-   Test and check whether the old version number is still present in
    other files.

## Localisation / translation

We are in a transition phase to use trema and support french and italien
versions of elexis. For details see
[l10n.md](bundles/ch.elexis.core.l10n/doc/l10n.md)
