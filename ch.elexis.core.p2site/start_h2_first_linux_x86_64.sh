#!/bin/bash
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
ELEXIS_LOG=$HOME/elexis/logs/elexis-3.log
ls -l $ELEXIS_LOG
rm -rfv $PWD/test_db.*.db
rm -rfv $HOME/elexis/demoDB
rm -rfv $HOME/elexis/*lock.*
rm -rfv $ELEXIS_LOG
ls -l $APP
SEARCH_PATTERN="Bypassing LoginDialog with username mustermann"
SEARCH_PATTERN=Bypassing
COMMAND="${APP} -consoleLog -nl en_US -vmargs \
-Duser.language=en -Duser.region=US \
-Dch.elexis.dbFlavor=h2 -Dch.elexis.dbSpec=jdbc:h2:${PWD}/test_db -Dch.elexis.dbUser=sa -Dch.elexis.dbPw= \
-Dch.elexis.firstMandantName=mustermann -Dch.elexis.firstMandantPassword=elexisTest -Dch.elexis.firstMandantEmail=mmustermann@elexis.info \
-Dch.elexis.username=mustermann -Dch.elexis.password=elexisTest"

MAX_WAIT=50

wait_for_bypassing() {
  for counter in $(seq 1 $MAX_WAIT)
  do
    sleep 1
    echo Checking $counter time
    nr_connections=`egrep -c $SEARCH_PATTERN $ELEXIS_LOG`
    echo $counter/$MAX_WAIT: Checked $ELEXIS_LOG for $SEARCH_PATTERN. Found it $nr_connections times
    if [  $nr_connections -eq 1 ]; then
      echo After $counter seconds found $SEARCH_PATTERN $nr_connections times in $ELEXIS_LOG
      sleep 1 # Just to see the rest of the startup
      break
    fi
    if [ $counter -eq "$MAX_WAIT" ]; then
      echo FAILURE!! Why did connect Elexis to the Database with $SEARCH_PATTERN ?
      exit $counter
    fi
done
}

kill_descendent_pids() {
    pids=$(pgrep -P $1)
    for pid in $pids; do
        kill_descendent_pids $pid
        # echo "Killing descendant $pid (forcing termination to skip confirmation dialog)"
        kill $pid
        kill -9 $pid
    done
}

echo Will call:
echo ${COMMAND}
${COMMAND} 2>&1 | tee ${LOG_FILE}.1 &
wait_for_bypassing

# Kill Elexis and try to restart it
kill_descendent_pids $$
sleep 1
rm -rfv $ELEXIS_LOG

${COMMAND} 2>&1 | tee ${LOG_FILE}.2 &
wait_for_bypassing

# Kill Elexis and remove test_db
kill_descendent_pids $$
echo rm -rfv $PWD/test_db.*.db

exit 0
