#!/bin/bash
# Squash mulitple files into one

CIRC_FILE="circulating_supply.json"

DIR=${1:-./}
OUTPUT=${2:-$CIRC_FILE}

rm -f $OUTPUT

for d in `find $DIR -name "${CIRC_FILE}"`; do 
    echo "File: ${d}"
    cat $d >>$OUTPUT
    echo "" >>OUTPUT ; 
done

2>&1 echo "Supply: $OUTPUT"
 