#!/bin/bash

PY=${1:-}
NAME=${2:-HAAS-JOB}

SERVICE_URI=${SERVICE_URI:-http://emr.hacken.cloud:8998}

scp -i key $PY hadoop@emr.hacken.cloud:/tmp/

>&2 echo "SERVICE_URI: ${SERVICE_URI}"
>&2 echo "Python: ${PY}"

DATA_JSON="{\"file\": \"local:///tmp/${PY}\", \"name\":\"${NAME}\", \"args\":[\"UNI\"]}"

curl -X POST --data "$DATA_JSON" -H "Content-Type: application/json" ${SERVICE_URI}/batches


