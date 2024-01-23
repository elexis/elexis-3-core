#!/bin/bash
# Sign the Elexis3 executables in a windows product zip using https://ebourg.github.io/jsign/
KEYSTORE_FILE=mykeystore.jks
KEYSTORE_PASS=keines
WINDOWS_ZIPPED_PRODUCT=Elexis3-win32.win32.x86_64.zip

#
# Begin script
#

# Extract unsigned files
unzip -d . ${WINDOWS_ZIPPED_PRODUCT} Elexis3.exe
unzip -d . ${WINDOWS_ZIPPED_PRODUCT} Elexis3c.exe

# Sign with Java keystore
DOCKER_JSIGN="docker run -it --rm -v "$(pwd)":/usr/src/mymaven  -w /usr/src/mymaven gitlab.medelexis.ch:4567/elexis/docker-build:2023-03-java17 /jsign "
${DOCKER_JSIGN} --keystore ${KEYSTORE_FILE} --storetype JKS --storepass ${KEYSTORE_PASS} --tsaurl http://timestamp.sectigo.com Elexis3.exe
${DOCKER_JSIGN} --keystore ${KEYSTORE_FILE} --storetype JKS --storepass ${KEYSTORE_PASS} --tsaurl http://timestamp.sectigo.com Elexis3c.exe

# Update with signed files
zip ${WINDOWS_ZIPPED_PRODUCT} Elexis3.exe
zip ${WINDOWS_ZIPPED_PRODUCT} Elexis3c.exe

rm Elexis3.exe
rm Elexis3c.exe