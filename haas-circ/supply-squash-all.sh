#!/bin/bash
# Squash mulitple files into one
CWD=`echo $(dirname $(readlink -f $0))`
CIRC_FILE="circulating_supply.json"

YEARS=${YEARS:-2015 2016 2017 2018 2019 2020 2021 2022 2023}

DIR=${1:-./}

for y in $YEARS; do 
    >&2 echo -ne "Looking for: ${DIR}/${y}..."
    stat ${DIR}/${y} 2>/dev/null >/dev/null
    r=$?
    if [ "$r" == "0" ]; then
        >&2 echo "processing"
                
        #$CWD/supply-squash.sh ${DIR}/${y} ${DIR}/${CIRC_FILE}.${y}
        #mv ${CIRC_FILE} ${CIRC_FILE}.$y
        $CWD/supply-squash.sh ${DIR}/${y} ${DIR}/${CIRC_FILE}

    else
        >&2 echo "NOT FOUND"
    fi
done
 