#!/bin/bash

USER_ID=${1:-00000000-0000-0000-1000-000000000001}

SERVICE_URI=${SERVICE_URI:-http://127.0.0.1:8080/api/v1/intercept}
ACCESS_TOKEN=${ACCESS_TOKEN-`cat ACCESS_TOKEN`}

curl -S -s -D /dev/stderr -s -X GET -H 'Content-Type: application/json' -H "Authorization: Bearer $ACCESS_TOKEN" $SERVICE_URI/user/${USER_ID}
