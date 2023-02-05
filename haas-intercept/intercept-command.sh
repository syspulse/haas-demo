#!/bin/bash

ID=${1}
CMD=${2:-stop}

ACCESS_TOKEN=${ACCESS_TOKEN-`cat ACCESS_TOKEN`}

SERVICE_URI=${SERVICE_URI:-http://localhost:8080/api/v1/intercept}

#DATA_JSON="{\"name\":\"$NAME\",\"script\":\"$SCRIPT\",\"uid\":\"$USER_ID\",\"alarm\":\"$ALARM\"}"
DATA_JSON="{\"id\":\"$ID\",\"command\":\"$CMD\"}"

2> echo $DATA_JSON
curl -S -s -D /dev/stderr -X POST --data "$DATA_JSON" -H 'Content-Type: application/json' -H "Authorization: Bearer $ACCESS_TOKEN" $SERVICE_URI/$ID
