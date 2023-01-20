#!/bin/bash

FROM=${1:-0}
SIZE=${2:-5}
SERVICE_URI=${SERVICE_URI:-http://127.0.0.1:8080/api/v1/token}
TOKEN=${TOKEN-`cat ACCESS_TOKEN`}

curl -s -X GET -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" "$SERVICE_URI/?from=${FROM}&size=${SIZE}"
