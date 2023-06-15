#!/bin/bash
# Squash mulitple files into one
CWD=`echo $(dirname $(readlink -f $0))`

export SQUASH_FILE="holders.csv"
export FILTER="address,quantity"

$CWD/squash-all.sh "$@"
