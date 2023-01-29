#!/bin/bash

NAME=${1:-HAAS-SESSION}
SERVICE_URI=${SERVICE_URI:-http://emr.hacken.cloud:8998}

# params can be read with Python:
# par1 = spark.conf.get("spark.job.param1")
# par2 = spark.conf.get("spark.job.param2")


DATA_1="{\"kind\": \"pyspark\", \"name\":\"${NAME}\", \"conf\":{\"spark.job.param1\": 100,\"spark.job.param2\": \"UNI\"}}"

r1=`curl -X POST --data "$DATA_1" -H "Content-Type: application/json" ${SERVICE_URI}/sessions`

if [ "$?" != "0" ]; then
   echo $r1
   exit $?
fi

>&2 echo $r1 | jq .