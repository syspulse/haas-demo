#!/bin/bash

F=${1:-test-0.py}
NAME=${2:-HAAS-SESSION}

SERVICE_URI=${SERVICE_URI:-http://emr.demo.hacken.cloud:8998}

CODE=`cat $F | jq -Rsa .`

echo $CODE

DATA_2="{\"code\": $CODE}"

r2=`curl -X POST  -H "Content-Type:application/json" -d "$DATA_2" ${SERVICE_URI}/sessions/${NAME}/statements`

if [ "$?" != "0" ]; then
   echo $r2
   exit $?
fi

echo $r2 | jq .
