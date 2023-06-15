#!/bin/bash
# Squash mulitple files into one

SQUASH_FILE=${SQUASH_FILE:-"circulating_supply.json"}

DIR=${1:-./}
OUTPUT=${2:-$SQUASH_FILE}
FILTER=${FILTER}

#rm -f $OUTPUT

>&2 echo "Squash File: ${SQUASH_FILE}"
>&2 echo "Processing: ${DIR} -> ${OUTPUT}"

for d in `find $DIR -name "${SQUASH_FILE}"`; do 
    >&2 echo "File: ${d}"
    
    if [ "$FILTER" != "" ]; then
        cat $d | sed 's/\r//' | sed "s/${FILTER}//" | sed '/^[[:space:]]*$/d'  >>$OUTPUT
    else
        cat $d | sed 's/\r//' | sed '/^[[:space:]]*$/d'  >>$OUTPUT
    fi

    if [ "$DELIMITER" != "" ]; then
        #echo "" >>$OUTPUT
        printf $DELIMITER >>$OUTPUT
    fi
done

>&2 echo "Output: $OUTPUT"
 