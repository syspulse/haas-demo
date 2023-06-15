#!/bin/bash
# Squash mulitple files into one
CWD=`echo $(dirname $(readlink -f $0))`

export SQUASH_FILE="circulating_supply.json"

$CWD/squah.sh $@
