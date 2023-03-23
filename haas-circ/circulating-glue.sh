#!/bin/bash

DIR=${1:-./}

FILE=${2:-supply.json}

rm $FILE

for d in `find $DIR -name "circulating_supply.json"`; do echo "File: ${d}"; cat $d >>$FILE; echo "" >>FILE ; done

echo "Supply: $FILE"
 