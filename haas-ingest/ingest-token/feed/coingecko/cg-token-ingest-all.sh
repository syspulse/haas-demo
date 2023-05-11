#!/bin/bash
# Download all Tokens as files into directory
CWD=`echo $(dirname $(readlink -f $0))`

INPUT=${1:-ALL.csv}
OUTPUT=${2:-/mnt/share/data/haas/gecko}
SLEEP=${3:-10}

IDS_FILE=ALL-id.csv
cat $INPUT | awk -F, '{print $1}' >$IDS_FILE

function download() {
  id=$1
  file=$OUTPUT/${id}.json
  echo "Downloading $id..."
  $CWD/cg-token-info.sh $id >$file
  sleep $SLEEP
}


for id in `cat $IDS_FILE`; do
  echo "ID: $id"
  file=$OUTPUT/${id}.json
  # check for file
  if [ -e "$file" ]; then
    # check the size
    sz=`ls -l  $file |awk '{print $5}'`
    if [ "$sz" == "187" ] || [ "$sz" == "0" ]; then
      download $id
    fi 
  else 
    download $id
  fi
done

