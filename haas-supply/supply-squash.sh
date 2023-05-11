#!/bin/bash
# Squash mulitple files into one

CIRC_FILE="supply.json"

DIR=${1:-./}
OUTPUT=${2:-$CIRC_FILE}

#rm -f $OUTPUT

>&2 echo "Processing: ${DIR} -> ${OUTPUT}"

for d in `find $DIR -name "${CIRC_FILE}"`; do 
    >&2 echo "File: ${d}"
    
    cat $d >>$OUTPUT
    echo "" >>$OUTPUT
done

>&2 echo "Supply: $OUTPUT"
 