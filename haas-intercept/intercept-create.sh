#!/bin/bash

NAME=${1:-name-1}
SCRIPT=${2:-id://script-1.js}
ALARM=${3:-stdout://}
ENTITY=${4:-tx}
USER_ID=${5:-00000000-0000-0000-1000-000000000001}
ABI=${ABI:-}
CONTRACT=${CONTRACT:-}

TOKEN=${TOKEN-`cat ACCESS_TOKEN`}

SERVICE_URI=${SERVICE_URI:-http://localhost:8080/api/v1/intercept}

if [ -f "$ABI" ]; then
   ABI_JSON=`cat $ABI | jq -Rsa .`   
else
   ABI_JSON=""
fi

>&2 echo $ABI_JSON

if [ "$ABI_JSON" != "" ]; then
   DATA_JSON="{\"name\":\"$NAME\",\"script\":\"$SCRIPT\",\"entity\":\"$ENTITY\",\"uid\":\"$USER_ID\",\"alarm\":[\"$ALARM\"], \"abi\": ${ABI_JSON}, \"contract\":\"${CONTRACT}\"}"
else
   DATA_JSON="{\"name\":\"$NAME\",\"script\":\"$SCRIPT\",\"entity\":\"$ENTITY\",\"uid\":\"$USER_ID\",\"alarm\":[\"$ALARM\"] }"
fi

>&2 echo $DATA_JSON

curl -X POST --data "$DATA_JSON" -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" $SERVICE_URI/
