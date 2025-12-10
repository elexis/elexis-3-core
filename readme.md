# Elexis 3 Core Repository

[![Build P2 Site](https://github.com/elexis/elexis-3-core/actions/workflows/build-p2-site.yaml/badge.svg)](https://github.com/elexis/elexis-3-core/actions/workflows/build-p2-site.yaml)

Containing the core files of the Elexis Application.

## Installation

- Use precompiled binaries
- Use an Eclipse IDE. We recommend to follow the steps outlined in this [Readme](./ch.elexis.sdk/readme.md).
- Build from source 

### Binaries

The binaries of our latest stable version (3.12) you will find under

- [Windows](http://download.elexis.info/elexis/3.12/products/Elexis3-win32.win32.x86_64.zip)
- [Apple OS X](http://download.elexis.info/elexis/3.12/products/Elexis3-macosx.cocoa.x86_64.tar.gz)
- [Linux](http://download.elexis.info/elexis/3.12/products/Elexis3-linux.gtk.x86_64.tar.gz)

The binaries of our main development branch  (master) you will find under

- [Windows](http://download.elexis.info/elexis/master/products/Elexis3-win32.win32.x86_64.zip)
- [Apple OS X](http://download.elexis.info/elexis/master/products/Elexis3-macosx.cocoa.x86_64.tar.gz)
- [Linux](http://download.elexis.info/elexis/master/products/Elexis3-linux.gtk.x86_64.tar.gz)

### From source

- If you have maven and OpenJDK 21 installed, the following command should work `mvn -V -T 1C clean verify -B -Dmaterialize-products`
- A correct setup of a build machine includes settings like the `de_CH` locale and other.
- Under .github/workflows/ you find the github actions used to build the P2 und products (see [Download](https://download.elexis.info/elexis/master/))

### Feedback

You are welcomed to give feeedback. We suggest that you

- Start a discussion [here](https://github.com/orgs/elexis/discussions)
- Document a bug/requirement [here](https://github.com/elexis/elexis-3-core/issues)
- Or even fix a problem by yourself and submit a patch by creating a [Pull Request](https://github.com/elexis/elexis-3-core/pulls)

### More info

The following sites might be relevant to you, too.

- https://www.elexis.info/
- https://elexis.ch/
- https://elexis.ch/ungrad/ (A fork maintained by Gerry Weirich, the original developper of Elexis )

Professional support is offered by the [Medelexis AG](https://medelexis.ch/)
