#!/bin/bash
# Squash mulitple files into one
CWD=`echo $(dirname $(readlink -f $0))`

YEARS=${YEARS:-2015 2016 2017 2018 2019 2020 2021 2022 2023}

DIR=${1:-./}
OUTPUT=${2:./output}

SQUASH_FILE=${SQUASH_FILE}

>&2 echo "Squash File: ${SQUASH_FILE}"

for y in $YEARS; do 
    >&2 echo -ne "Looking for: ${DIR}/${y}..."
    stat ${DIR}/${y} 2>/dev/null >/dev/null
    r=$?
    if [ "$r" == "0" ]; then
        >&2 echo "processing"
                
        #$CWD/squash.sh ${DIR}/${y} ${DIR}/${SQUASH_FILE}.${y}
        #mv ${SQUASH_FILE} ${SQUASH_FILE}.$y
        $CWD/squash.sh ${DIR}/${y} ${OUTPUT} ${SQUASH_FILE}

    else
        >&2 echo "NOT FOUND"
    fi
done
 