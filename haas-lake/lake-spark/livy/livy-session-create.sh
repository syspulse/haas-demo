#!/bin/bash

NAME=${1:-HAAS-SESSION}
SERVICE_URI=${SERVICE_URI:-http://emr.hacken.cloud:8998}

DATA_1="{\"kind\": \"pyspark\", \"name\":\"${NAME}\"}"

r1=`curl -X POST --data "$DATA_1" -H "Content-Type: application/json" ${SERVICE_URI}/sessions`

if [ "$?" != "0" ]; then
   echo $r1
   exit $?
fi

>&2 echo $r1 | jq .