#!/bin/bash
# abort bash on error
set -e

if [ -z "$ROOT_ELEXIS_CORE" ]
then
  export ROOT_ELEXIS_CORE=/srv/download.elexis.info/elexis.3.core
fi

if [ ! -d "$ROOT_ELEXIS_CORE" ]
then
  echo "ROOT_ELEXIS_CORE (actually defined as $ROOT_ELEXIS_CORE) must exist"
  exit 1
fi

# set some default values
export parent=`dirname $0`
if [ -z "$VARIANT" ]
then
  export VARIANT=snapshot
fi
if [ -z "$path_to_eclipse_4_2" ]
then
  export path_to_eclipse_4_2=/home/srv/p2Helpers/eclipse/eclipse
fi

# Maven must have prepared a repo.properties file under ch.medelexis.p2site
# If such a file exists in the destination directory, we get the version for the zip file from there
# else the zip_version will be the actual date/time
export act_version_file=${PWD}/ch.elexis.core.p2site/repo.properties
if [ ! -f $act_version_file ]
then
  echo "File ${act_version_file} must exist!"
  exit 1
fi
export backup_root=${ROOT_ELEXIS_CORE}/backup/$VARIANT

echo $0: ROOT_ELEXIS_CORE is $ROOT_ELEXIS_CORE and VARIANT is $VARIANT.

# Check whether we have to backup the old version of the repository
export old_version_file=${ROOT_ELEXIS_CORE}/${VARIANT}/repo.version
if [ -f ${old_version_file}  ]
then
  source ${old_version_file}
  if [ ! -d $backup_root/$version-$qualifier ]
  then
    echo "Backup of version found under $ROOT_ELEXIS_CORE/$VARIANT necessary"
    mkdir -p $backup_root
    mv -v $ROOT_ELEXIS_CORE/$VARIANT $backup_root/$version-$qualifier
  else
    echo Skipping backup as  $backup_root/$version-$qualifier already present
  fi
fi

rm -rf ${ROOT_ELEXIS_CORE}/$VARIANT
cp -rpu *p2site/target/repository/ ${ROOT_ELEXIS_CORE}/$VARIANT
cp -rpu *p2site/target/products ${ROOT_ELEXIS_CORE}/$VARIANT
cp -rpvu *p2site/repo.properties ${ROOT_ELEXIS_CORE}/$VARIANT/repo.version
export title="Elexis-Application P2-repository ($VARIANT)"
echo "Creating repository $ROOT_ELEXIS_CORE/$VARIANT/index.html"
tee  ${ROOT_ELEXIS_CORE}/$VARIANT/index.html <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<html>
  <head><title>$title</title></head>
  <body>
    <h1>$title</h1>
    <ul>
      <li><a href="products">ZIP files for Elexis-Application (OS-specific)</a></li>
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
