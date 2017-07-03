#!/bin/bash
# abort bash on error
set -e

NAME=`basename $PWD`
export GIT_BRANCH=`git rev-parse --abbrev-ref HEAD`
if [ -z "$GIT_BRANCH" ]; then
  echo no GIT_BRANCH defined
  exit 1
fi

if [ -z "$P2_ROOT" ]
then
  export P2_ROOT=/home/jenkins/downloads/p2
fi
if [ ! -d "$P2_ROOT" ]
then
  echo P2_ROOT $P2_ROOT does not exit. Aborting
  exit 1
fi


TARGETDIRECTORY=${P2_ROOT}/${GIT_BRANCH}/${NAME}
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
export backup_root=${TARGETDIRECTORY}/backup/$GIT_BRANCH

echo $0: TARGETDIRECTORY is $TARGETDIRECTORY and GIT_BRANCH is $GIT_BRANCH.

rm -rf ${TARGETDIRECTORY}/$GIT_BRANCH
mkdir -p  ${TARGETDIRECTORY}/$GIT_BRANCH/products
cp -rpu *product/target/products/*.zip   ${TARGETDIRECTORY}/$GIT_BRANCH/products
cp -rpu *p2site/target/repository ${TARGETDIRECTORY}/$GIT_BRANCH
cp -rpvu *p2site/repo.properties ${TARGETDIRECTORY}/$GIT_BRANCH/repo.version
export title="Elexis-Application P2-repository ($GIT_BRANCH)"
echo "Creating repository $TARGETDIRECTORY/$GIT_BRANCH/index.html"
tee  ${TARGETDIRECTORY}/$GIT_BRANCH/index.html <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<html>
  <head><title>$title</title></head>
  <body>
    <h1>$title</h1>
    <ul>
      <li><a href="products">ZIP files for Elexis-Application (OS-specific)  $GIT_BRANCH</a></li>
      <li><a href="repository/binary">binary</a></li>
      <li><a href="repository/plugins">plugins</a></li>
      <li><a href="repository/features">features</a></li>
    </ul>
    </p>
    <p>Installed `date`
    </p>
  </body>
</html>
EOF
