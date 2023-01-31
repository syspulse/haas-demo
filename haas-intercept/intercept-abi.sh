#!/bin/bash

ID=${1}
AID=${2:-0xdac17f958d2ee523a2206206994597c13d831ec7}

SERVICE_URI=${SERVICE_URI:-http://127.0.0.1:8080/api/v1/intercept}
TOKEN=${TOKEN-`cat ACCESS_TOKEN`}

curl -s -X GET -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" $SERVICE_URI/${ID}/abi/${AID}
