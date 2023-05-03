#!/bin/bash

START=${1:-10861674}
END=${2:-10861675}

#./eth-export-logs.sh $START $END | ./run-ingest-eth.sh -e log -o "fs3:///mnt/s3/data/dev/ethereum/raw/csv/logs/{yyyy}/{MM}/{dd}/log-{HH}.csv"
#./eth-export-logs.sh $START $END | ./run-ingest-eth.sh -e log -o "fs3:///mnt/s4/data/dev/ethereum/raw/csv/logs/{yyyy}/{MM}/{dd}/log-{HH}.csv"
#./eth-export-logs.sh $START $END | ./run-ingest-eth.sh -e log -o "fs3:///data/logs/{yyyy}/{MM}/{dd}/log-{HH}.csv"

./eth-export-logs.sh $START $END | ./run-ingest-eth.sh -e log
