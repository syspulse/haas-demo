#!/bin/bash

SCRIPT=${1:-ref://script-1.js}
ENTITY=${2:-tx}
ALARM=${3:-stdout://}

NAME=${NAME:-name-1}
BID=${BID:-ethereum}
USER_ID=${USER_ID:-00000000-0000-0000-1000-000000000001}
ABI=${ABI:-}
CONTRACT=${CONTRACT:-}

ACCESS_TOKEN=${ACCESS_TOKEN-`cat ACCESS_TOKEN`}

SERVICE_URI=${SERVICE_URI:-http://localhost:8080/api/v1/intercept}

if [ -f "$ABI" ]; then
   ABI_JSON=`cat $ABI | jq -Rsa .`   
else
   ABI_JSON=""
fi

>&2 echo $ABI_JSON

if [ "$ABI_JSON" != "" ]; then
   DATA_JSON="{\"name\":\"$NAME\",\"script\":\"$SCRIPT\",\"bid\":\"$BID\",\"entity\":\"$ENTITY\",\"uid\":\"$USER_ID\",\"alarm\":[\"$ALARM\"], \"abi\": ${ABI_JSON}, \"contract\":\"${CONTRACT}\"}"
else
   DATA_JSON="{\"name\":\"$NAME\",\"script\":\"$SCRIPT\",\"bid\":\"$BID\",\"entity\":\"$ENTITY\",\"uid\":\"$USER_ID\",\"alarm\":[\"$ALARM\"] }"
fi

>&2 echo $DATA_JSON

curl -S -s -D /dev/stderr -X POST --data "$DATA_JSON" -H 'Content-Type: application/json' -H "Authorization: Bearer $ACCESS_TOKEN" $SERVICE_URI/
