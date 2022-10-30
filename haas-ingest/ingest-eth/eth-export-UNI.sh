#!/bin/bash

START=${1:-10861674}
END=${2:-10861675}

./eth-export-tokens.sh $START $END | ./run-ingest-eth.sh -e token -o "fs3:///mnt/s3/data/dev/{yyyy}/{MM}/{dd}/token-{HH_mm_ss}.csv" --limit=10000
#./eth-export-tokens.sh $START $END | ./run-ingest-eth.sh -e token -o "fs3:///output/data/dev/ethereum/raw/csv/tokens/{yyyy}/{MM}/{dd}/token-{HH_mm_ss}.csv" --limit=10000
#./eth-export-tokens.sh $START $END | ./run-ingest-eth.sh -e token -o "fs3:///mnt/s3/data/dev/ethereum/raw/csv/tokens/{yyyy}/{MM}/{dd}/token-{HH_mm_ss}.csv" --limit=10000
#./eth-export-tokens.sh $START $END | ./run-ingest-eth.sh -e token stdout:// --limit=10000