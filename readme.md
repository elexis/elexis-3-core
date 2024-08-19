# Elexis 3 Core Repository
[![Build P2 Site](https://github.com/elexis/elexis-3-core/actions/workflows/build-p2-site.yaml/badge.svg)](https://github.com/elexis/elexis-3-core/actions/workflows/build-p2-site.yaml)

Containing the core files of the Elexis Application.
## Installation
- Use precompiled binaries
- Build from source
### Binaries
- [Windows](http://download.elexis.info/elexis/3.10/products/Elexis3-win32.win32.x86_64.zip)
- [Apple OS X](http://download.elexis.info/elexis/3.10/products/Elexis3-macosx.cocoa.x86_64.zip)
- [Linux](http://download.elexis.info/elexis/3.10/products/Elexis3-linux.gtk.x86_64.zip)

### From source

A correct setup of a build machine includes settings like the `de_CH` locale and other.
To ease this process, a docker build image is provided in 
[gitlab.medelexis.ch](https://gitlab.medelexis.ch/elexis/docker-build/container_registry "gitlab.medelexis.ch") 
Have a look at the file `.gitlab-ci.yml` on how to use this image to generate your own build.
