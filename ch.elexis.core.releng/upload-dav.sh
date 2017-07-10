#!/bin/bash
# Upload build artifacts via WEBDAV
# Usage: upload-dav local_directory location remote_directory username password
# Author: Marco Descher <descher@medevit.at>

LOCAL_DIRECTORY=$1
LOCATION=$2
REMOTE_DIRECTORY=$3

CURL_CMD=$(which curl)
if [[ -z "$CURL_CMD" ]]; then
	echo "curl not found."
 	exit 0;
fi

echo "[DELETE] $LOCATION/$REMOTE_DIRECTORY/"
$CURL_CMD -s -u $4:$5 -X DELETE $LOCATION/$REMOTE_DIRECTORY/
echo "[MKCOL] $LOCATION/$REMOTE_DIRECTORY/"
$CURL_CMD -s -u $4:$5 -X MKCOL $LOCATION/$REMOTE_DIRECTORY/

cd $LOCAL_DIRECTORY

for line in $(find * -type d); do
	DEST=$LOCATION/$REMOTE_DIRECTORY/$line/
	echo "[MKCOL] $DEST" 
	$CURL_CMD -s -u $4:$5 -X MKCOL $DEST
done

for line in $(find * -type f); do
	DEST=$LOCATION/$REMOTE_DIRECTORY/$line
	echo "[PUT] $line -> $DEST"
	$CURL_CMD -s -u $4:$5 -T $line $LOCATION/$REMOTE_DIRECTORY/$line
done
