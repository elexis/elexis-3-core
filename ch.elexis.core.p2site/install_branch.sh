#!/bin/bash
# abort bash on error
set -e

if [ -z "$GIT_BRANCH" ]; then
  echo no GIT_BRANCH defined
  exit 1
fi
PROJECT_NAME=`echo $GIT_BRANCH | cut -d '/' -f1`
PROJECT_BRANCH=`echo $GIT_BRANCH | cut -d '/' -f2`

if [ -z "$P2_ROOT" ]
then
  export P2_ROOT=/home/jenkins/downloads/p2
fi
if [ ! -d "$P2_ROOT" ]
then
  echo P2_ROOT $P2_ROOT does not exit. Aborting
  exit 1
fi


TARGETDIRECTORY=${P2_ROOT}/${PROJECT_BRANCH}/${PROJECT_NAME}
mkdir -p $TARGETDIRECTORY

# Maven must have prepared a repo.properties file under ch.medelexis.p2site
# If such a file exists in the destination directory, we get the version for the zip file from there
# else the zip_version will be the actual date/time
export act_version_file=${PWD}/ch.elexis.core.p2site/repo.properties
if [ ! -f $act_version_file ]
then
  echo "File ${act_version_file} must exist!"
  exit 1
fi
echo $0: TARGETDIRECTORY is $TARGETDIRECTORY and PROJECT_BRANCH is $PROJECT_BRANCH.

rm -rf ${TARGETDIRECTORY}
mkdir -p  ${TARGETDIRECTORY}/products
cp -rpu *product/target/products/*.zip   ${TARGETDIRECTORY}/products
cp -rpu *p2site/target/ ${TARGETDIRECTORY}
cp -rpvu *p2site/repo.properties ${TARGETDIRECTORY}/repo.version
export title="Elexis-Application P2-repository ($PROJECT_BRANCH)"
echo "Creating repository $TARGETDIRECTORY/index.html"
tee  ${TARGETDIRECTORY}/index.html <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<html>
  <head><title>$title</title></head>
  <body>
    <h1>$title</h1>
    <ul>
      <li><a href="products">ZIP files for Elexis-Application (OS-specific)  $PROJECT_BRANCH</a></li>
      <li><a href="binary">binary</a></li>
      <li><a href="plugins">plugins</a></li>
      <li><a href="features">features</a></li>
    </ul>
    </p>
    <p>Installed `date`
    </p>
  </body>
</html>
EOF
