#!/bin/bash

FROM=${1:-0}
SIZE=${2:-5}

SERVICE_URI=${SERVICE_URI:-http://127.0.0.1:8080/api/v1/holders}
ACCESS_TOKEN=${ACCESS_TOKEN-`cat ACCESS_TOKEN`}

curl -S -s -D /dev/stderr -X GET -H 'Content-Type: application/json' -H "Authorization: Bearer $ACCESS_TOKEN" "$SERVICE_URI/?from=${FROM}&size=${SIZE}"
