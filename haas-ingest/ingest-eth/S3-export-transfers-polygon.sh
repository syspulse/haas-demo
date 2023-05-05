#!/bin/bash
CWD=`echo $(dirname $(readlink -f $0))`

source $CWD/../../demo/env-aws/env-aws.sh

START=${1:-0}
END=${2:-1}

export BATCH=10

export ETH_RPC=http://34.76.39.105:8547

./eth-export-transfers.sh $START $END 2>/dev/null | ./run-ingest-eth.sh -e transfer -o "fs3:///mnt/s3/data/dev/polygon/mainnet/raw/csv/transfers/{yyyy}/{MM}/{dd}/transfer-{HH}.csv"
