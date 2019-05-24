#!/bin/bash -v
echo running $0 in $PWD
APP=../ch.elexis.core.p2site/target/products/ch.elexis.core.application.ElexisApp/linux/gtk/x86_64/Elexis3
LOG_FILE=start_h2_first_linux_x86_64.log
rm -rfv $PWD/test_db.*.db
rm -rfv $HOME/elexis/demoDB
ls -l $APP
$APP  -consoleLog -nl en_US -vmargs \
-Duser.language=en -Duser.region=US \
-Dch.elexis.dbFlavor=h2 -Dch.elexis.dbSpec=jdbc:h2:$PWD/test_db -Dch.elexis.dbUser=sa -Dch.elexis.dbPw= \
-Dch.elexis.firstMandantName=mustermann -Dch.elexis.firstMandantPassword=elexisTest -Dch.elexis.firstMandantEmail=mmustermann@elexis.info \
-Dch.elexis.username=mustermann -Dch.elexis.password=elexisTest 2>&1 | tee $LOG_FILE &
sleep 5
# kill restarted Elexis, -9 is needed to suppress confirmation at exit
ps -ef | grep $APP | cut -c 10-15 | xargs kill -9
nr_connections=`egrep -c 'ch.elexis.data.DBConnection.*(Connecting|Verbunden).*jdbc:h2.*test_db' $LOG_FILE` 
echo Elexis connected $nr_connections times to the Database
if [  $nr_connections -ne 2 ]; then
  echo FAILURE!! Why did connect Elexis  $nr_connections times to the Database? We expected 2 successfull connections
  exit 3
else
  echo Elexis connected succesfully $nr_connections times to the Database
  rm -rfv $PWD/test_db.*.db
fi
