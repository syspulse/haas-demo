#!/bin/bash

FROM=${1}
SIZE=${2}

PARAMS="?"

if [ "$FROM" != "" ]; then
   PARAMS=$PARAMS"from=${FROM}&"
fi
if [ "$SIZE" != "" ]; then
   PARAMS=$PARAMS"size=${SIZE}&"
fi


SERVICE_URI=${SERVICE_URI:-http://127.0.0.1:8080/api/v1/circ}
ACCESS_TOKEN=${ACCESS_TOKEN-`cat ACCESS_TOKEN`}

curl -S -s -D /dev/stderr -X GET -H 'Content-Type: application/json' -H "Authorization: Bearer $ACCESS_TOKEN" $SERVICE_URI/last${PARAMS}
