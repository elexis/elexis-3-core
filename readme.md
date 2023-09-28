# Elexis 3 Core Repository
Containing the core files of the Elexis Application.
## Installation
- Use precompiled binaries
- Build from source
### Binaries
- [Windows](http://download.elexis.info/elexis/3.10/products/Elexis3-win32.win32.x86_64.zip)
- [Apple OS X](http://download.elexis.info/elexis/3.10/products/Elexis3-macosx.cocoa.x86_64.zip)
- [Linux](http://download.elexis.info/elexis/3.10/products/Elexis3-linux.gtk.x86_64.zip)

### From source
Install Java JDK 17 and maven. Then execute the following commands:

`git clone https://github.com/elexis/elexis-3-core`
`mvn -V clean verify  -Dtycho.localArtifacts=ignore -DskipTests`



