#!/bin/bash
# Download all Tokens as files into directory
CWD=`echo $(dirname $(readlink -f $0))`

INPUT=${1:-/mnt/s4/data/dev/abi/4byte/event/raw/2023/02/06}
OUTPUT=${2:-/mnt/s4/data/dev/abi/4byte/event/json/2023/02/06}
FAT=${FAT:-}

if [ "$FAT" != "" ]; then
  OUTPUT=${OUTPUT}/${FAT}
else
  mkdir -p $OUTPUT
fi

for f in $INPUT/*; do
  [ -e "$f" ] || continue

  >&2 echo "Input: $f (`stat -c %s $f`)"

  if [ "$FAT" == "" ]; then
    f2=$OUTPUT/${f}
    $CWD/4byte-convert.sh $f >$f2

    >&2 echo "Output: $f2 (`stat -c %s $f2`)"

  else
    f2=$OUTPUT
    $CWD/4byte-convert.sh $f >>$f2

    >&2 echo "Output: $f2 (`stat -c %s $f2`)"
  fi
done
