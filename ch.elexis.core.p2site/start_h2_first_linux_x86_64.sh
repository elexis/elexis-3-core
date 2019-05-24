#!/bin/bash -v
echo running: $0 $1
APP=$1
if [ -z "$APP" ]; then
    echo "You must pass the full name of the elexs executable as first parameter!"
    exit 4
fi
if [ -x "$APP" ]; then
    echo "$APP is executable."
else
    echo "$APP ist not executable!"
    exit 4
fi

# see https://coderwall.com/p/q-ovnw/killing-all-child-processes-in-a-shell-script
shutdown() {
  # Get our process group id
  PGID=$(ps -o pgid= $$ | grep -o [0-9]*)

  # Kill it in a new new process group
  setsid kill -- -$PGID
  exit 0
}
trap "shutdown" SIGINT SIGTERM

LOG_FILE=start_h2_first_linux_x86_64.log
rm -rfv $PWD/test_db.*.db
rm -rfv $HOME/elexis/demoDB
rm -rfv $HOMe/elexis/*lock
ls -l $APP
$APP  -consoleLog -nl en_US -vmargs \
-Duser.language=en -Duser.region=US \
-Dch.elexis.dbFlavor=h2 -Dch.elexis.dbSpec=jdbc:h2:$PWD/test_db -Dch.elexis.dbUser=sa -Dch.elexis.dbPw= \
-Dch.elexis.firstMandantName=mustermann -Dch.elexis.firstMandantPassword=elexisTest -Dch.elexis.firstMandantEmail=mmustermann@elexis.info \
-Dch.elexis.username=mustermann -Dch.elexis.password=elexisTest 2>&1 | tee $LOG_FILE &
MAX_WAIT=50
for counter in $(seq 1 $MAX_WAIT)
do
  sleep 1
  echo Checking $counter time
  nr_connections=`egrep -c 'ch.elexis.data.DBConnection.*(Connecting|Verbunden).*jdbc:h2.*test_db' $LOG_FILE`
  echo $counter/$MAX_WAIT: Elexis connected $nr_connections times to the Database
  if [  $nr_connections -eq 2 ]; then
    echo After $counter seconds Elexis connected $nr_connections times to the Database
    sleep 1 # Just to see the rest of the startup
    break
  fi
  if [ $counter -eq "$MAX_WAIT" ]; then
    echo FAILURE!! Why did connect Elexis  $nr_connections times to the Database? We expected 2 successfull connections
    exit $counter
  fi
done

rm -rfv $PWD/test_db.*.db

kill_descendent_pids() {
    pids=$(pgrep -P $1)
    for pid in $pids; do
        kill_descendent_pids $pid
        # echo "Killing descendant $pid (forcing termination to skip confirmation dialog)"
        kill -9 $pid
    done
}
kill_descendent_pids $$
exit 0
