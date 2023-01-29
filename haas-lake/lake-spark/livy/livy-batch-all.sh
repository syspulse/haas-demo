#!/bin/bash

ID=${1:-}

SERVICE_URI=${SERVICE_URI:-http://emr.hacken.cloud:8998}

>&2 echo "SERVICE_URI: ${SERVICE_URI}"

# DATA="{\"file\": \"${PY}\"}"
#curl -X POST -data $DATA -H "Content-Type: application/json" localhost:8998/batches/${ID}

curl -X GET -H "Content-Type: application/json" ${SERVICE_URI}/batches/ | jq .



