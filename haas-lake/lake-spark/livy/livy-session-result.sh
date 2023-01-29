#!/bin/bash

R=${1:-}
NAME=${2:-HAAS-SESSION}

SERVICE_URI=${SERVICE_URI:-http://emr.hacken.cloud:8998}

if [ "$R" != "" ]; then
   curl -X GET -H "Content-Type:application/json" -d "$DATA_2" ${SERVICE_URI}/sessions/${NAME}/statements/$R
else
   curl -X GET -H "Content-Type:application/json" -d "$DATA_2" ${SERVICE_URI}/sessions/${NAME}/statements
fi

# ./livy-session-result.sh 4 | jq '.output.data."text/plain"' | xargs echo -en
