#!/bin/bash

SERVICE_URI=${SERVICE_URI:-http://emr.demo.hacken.cloud:8998}

curl -X GET -H "Content-Type: application/json" ${SERVICE_URI}/sessions

