#!/bin/bash

NAME=${1:-HAAS-SESSION}
SERVICE_URI=${SERVICE_URI:-http://emr.hacken.cloud:8998}

curl -X DELETE -H "Content-Type: application/json" ${SERVICE_URI}/sessions/$NAME
