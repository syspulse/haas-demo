#!/bin/bash

ID=${1:-aeroware}
SYMBOL=${2:-AERO}
NAME=${3:-Aeroware}
CONTRACT=${3:-0x1111111111111111111111111111111111111111}

ACCESS_TOKEN=${ACCESS_TOKEN-`cat ACCESS_TOKEN`}

SERVICE_URI=${SERVICE_URI:-http://localhost:8080/api/v1/token}

DATA_JSON="{\"id\":\"$ID\",\"name\":\"$NAME\",\"symbol\":\"$SYMBOL\",\"contracts\":{\"ethereum\":\"$CONTRACT\"}}"

2> echo $DATA_JSON
curl -S -s -D /dev/stderr -X POST --data "$DATA_JSON" -H 'Content-Type: application/json' -H "Authorization: Bearer $ACCESS_TOKEN" $SERVICE_URI/
