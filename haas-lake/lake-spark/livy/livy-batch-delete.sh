#!/bin/bash

NAME=${1:-HAAS-JOB}

SERVICE_URI=${SERVICE_URI:-http://emr.hacken.cloud:8998}

>&2 echo "SERVICE_URI: ${SERVICE_URI}"

curl -X DELETE -H "Content-Type: application/json" ${SERVICE_URI}/batches/${NAME}


