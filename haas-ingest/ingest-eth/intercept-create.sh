#!/bin/bash

NAME=${1:-name-1}
SCRIPT=${2:-id://script-1.js}
#ALARM=${3:-stdout://}
ALARM=${3:-ws://alarm}
USER_ID=${4:-00000000-0000-0000-1000-000000000001}

TOKEN=${TOKEN-`cat ACCESS_TOKEN`}

SERVICE_URI=${SERVICE_URI:-http://localhost:8080/api/v1/intercept}

#DATA_JSON="{\"name\":\"$NAME\",\"script\":\"$SCRIPT\",\"uid\":\"$USER_ID\",\"alarm\":\"$ALARM\"}"
DATA_JSON="{\"name\":\"$NAME\",\"script\":\"$SCRIPT\",\"alarm\":[\"$ALARM\"]}"

2> echo $DATA_JSON
curl -X POST --data "$DATA_JSON" -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" $SERVICE_URI/
