#!/bin/bash

NOTEBOOK_DIR=${NOTEBOOK_DIR:-/tmp/zep/notebook}
LOG_DIR=${LOG_DIR:-/tmp/zep/logs}

mkdir -p $NOTEBOOK_DIR
mkdir -p $LOG_DIR

#docker run -p 9080:8080 --rm --name zeppelin apache/zeppelin:0.10.0

docker run -u $(id -u) -p 9080:8080 --rm -v $LOG_DIR:/logs -v $NOTEBOOK_DIR:/notebook \
           -e ZEPPELIN_LOG_DIR='/logs' -e ZEPPELIN_NOTEBOOK_DIR='/notebook' -e ZEPPELIN_LIVY_HOST_URL=http://emr.hacken.cloud:8998 \
           --name zeppelin apache/zeppelin:0.10.0


