#!/bin/bash

TOKEN=${1:-0x1f9840a85d5af5bf1d1762f925bdaddc4201f984}
TS0=${2}
TS1=${3}
FROM=${4}
SIZE=${5}

SERVICE_URI=${SERVICE_URI:-http://127.0.0.1:8080/api/v1/holders}
ACCESS_TOKEN=${ACCESS_TOKEN-`cat ACCESS_TOKEN`}

PARAMS="?"

if [ "$FROM" != "" ]; then
   PARAMS=$PARAMS"from=${FROM}&"
fi
if [ "$SIZE" != "" ]; then
   PARAMS=$PARAMS"size=${SIZE}&"
fi
if [ "$LIMIT" != "" ]; then
   PARAMS=$PARAMS"limit=${LIMIT}&"
fi
if [ "$LIMIT" != "" ]; then
   PARAMS=$PARAMS"limit=${LIMIT}&"
fi
if [ "$TS0" != "" ]; then
   PARAMS=$PARAMS"ts0=${TS0}&"
fi
if [ "$TS1" != "" ]; then
   PARAMS=$PARAMS"ts1=${TS1}&"
fi


curl -S -s -D /dev/stderr -X GET -H 'Content-Type: application/json' -H "Authorization: Bearer $ACCESS_TOKEN" $SERVICE_URI/${TOKEN}${PARAMS}
