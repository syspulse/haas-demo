#!/bin/bash

ID=${1:-aeroware}
SYMBOL=${2}
NAME=${3}
CONTRACT=${3:-0x22222222222222222222222222222222222222}

ACCESS_TOKEN=${ACCESS_TOKEN-`cat ACCESS_TOKEN`}

SERVICE_URI=${SERVICE_URI:-http://localhost:8080/api/v1/token}

DATA_JSON=""

if [ "$SYMBOL" != "" ]; then
   if [ "$DATA_JSON" != "" ]; then
      DATA_JSON="$DATA_JSON,"   
   fi
   DATA_JSON="$DATA_JSON\"symbol\":\"$SYMBOL\""
fi

if [ "$NAME" != "" ]; then
   if [ "$DATA_JSON" != "" ]; then
      DATA_JSON="$DATA_JSON,"   
   fi
   DATA_JSON="$DATA_JSON\"name\":\"$NAME\""
fi

if [ "$CONTRACT" != "" ]; then
   if [ "$DATA_JSON" != "" ]; then
      DATA_JSON="$DATA_JSON,"   
   fi
   DATA_JSON="$DATA_JSON\"contracts\":{\"ethereum\":\"$CONTRACT\"}"
fi

DATA_JSON="{$DATA_JSON}"

>&2 echo $DATA_JSON

curl -S -s -D /dev/stderr -X PUT --data "$DATA_JSON" -H 'Content-Type: application/json' -H "Authorization: Bearer $ACCESS_TOKEN" "$SERVICE_URI/${ID}"
