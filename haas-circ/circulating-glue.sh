#!/bin/bash

DIR=${1:-./}

FILE=supply.json

for d in `ls $DIR`; do echo "dir: ${d}"; cat $d/circulating_supply.json >>$FILE; echo "" >>FILE ; done

echo "Supply: `wc -l $FILE`"
 
