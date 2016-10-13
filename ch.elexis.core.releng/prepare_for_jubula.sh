#!/bin/bash -v
# Copyright 2016 by Niklaus Giger <niklaus.giger@member.fsf.org>
#
# A simple helper script to create a Jubula enabled Elexis
# * Must be run after after "mvn clean install"
# * Asssumes a installed installation of Jubula under /opt/jubula_8.3.0.122
#
rm -rf work
mkdir work
cd work
unzip ../ch.elexis.core.p2site/target/products/ch.elexis.core.application.ElexisApp-linux.gtk.x86_64.zip
cd plugins
unzip /opt/jubula_8.3.0.122/development/rcp-support.zip
cd ../..
cp work/configuration/config.ini work/configuration/config.org
awk -i inplace '{sub(/osgi.bundles=/,"osgi.bundles=reference\\\:file\\\:org.eclipse.jubula.rc.rcp_4.0.0.201607281404.jar@3\\\:start,")}; 1' work/configuration/config.ini
