#!/bin/bash

TXT=${1}
FROM=${2}
SIZE=${3}

SERVICE_URI=${SERVICE_URI:-http://127.0.0.1:8080/api/v1/token}
ACCESS_TOKEN=${ACCESS_TOKEN-`cat ACCESS_TOKEN`}

if [ "$FROM" != "" ] || [ "$SIZE" != "" ]; then
   PARAMS="?"
fi
if [ "$FROM" != "" ]; then
   PARAMS=$PARAMS"from=${FROM}&"
fi
if [ "$SIZE" != "" ]; then
   PARAMS=$PARAMS"size=${SIZE}"
fi


curl -S -s -D /dev/stderr -X GET -H 'Content-Type: application/json' -H "Authorization: Bearer $ACCESS_TOKEN" $SERVICE_URI/typing/${TXT}${PARAMS}
