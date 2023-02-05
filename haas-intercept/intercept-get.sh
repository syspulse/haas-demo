#!/bin/bash

ID=${1}
SERVICE_URI=${SERVICE_URI:-http://127.0.0.1:8080/api/v1/intercept}
ACCESS_TOKEN=${ACCESS_TOKEN-`cat ACCESS_TOKEN`}
HISTORY=${HISTORY:-1}

curl -S -s -D /dev/stderr -s -X GET -H 'Content-Type: application/json' -H "Authorization: Bearer $ACCESS_TOKEN" $SERVICE_URI/${ID}?history=$HISTORY
