#!/bin/bash

S3_BUCKET=${S3_BUCKET:-haas-dev-data}

START=${1:-10861674}
END=${2:-10861675}

#./eth-export-transfers.sh $START $END | ./run-ingest-eth.sh -e transfer -o "fs3:///mnt/s3/data/dev/{yyyy}/{MM}/{dd}/token-{HH_mm_ss}.csv" --limit=10000
#./eth-export-transfers.sh $START $END | ./run-ingest-eth.sh -e transfer -o "fs3:///output/data/dev/ethereum/raw/csv/tokens/{yyyy}/{MM}/{dd}/token-{HH_mm_ss}.csv" --limit=10000
#./eth-export-transfers.sh $START $END | ./run-ingest-eth.sh -e transfer stdout:// --limit=10000

./eth-export-transfers.sh $START $END 2>/dev/null | ./run-ingest-eth.sh -e transfer -o "fs3:///mnt/s3/data/dev/ethereum/raw/csv/transfers/{yyyy}/{MM}/{dd}/transfer-{HH}.csv"
#./eth-export-transfers.sh $START $END | ./run-ingest-eth.sh -e transfer -o "fs3:///mnt/s4/data/dev/ethereum/raw/csv/transfers/{yyyy}/{MM}/{dd}/transfer-{HH}.csv"
#./eth-export-transfers.sh $START $END | ./run-ingest-eth.sh -e transfer -o "fs3:///data/transfers/{yyyy}/{MM}/{dd}/transfer-{HH}.csv"

#./eth-export-transfers.sh $START $END | ./run-ingest-eth.sh -e transfer
