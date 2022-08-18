#!/bin/bash

PIPE_NAME=${1:-PIPE}

rm -f $PIPE_NAME
mkfifo $PIPE_NAME

./eth-proxy.sh latest >$PIPE_NAME &

pid=$!

echo "Ingesting ($pid) -> $PIPE_NAME"

#tail -n1 -f $PIPE_NAME