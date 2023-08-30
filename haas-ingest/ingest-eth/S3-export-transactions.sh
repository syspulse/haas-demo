#!/bin/bash

S3_BUCKET=${S3_BUCKET:-haas-dev-data}

START=${1:-10861674}
END=${2:-10861675}

./eth-export-transactions.sh $START $END | ./run-ingest-eth.sh -e tx -o "fs3:///mnt/s3/data/dev/ethereum/raw/csv/transactions/{yyyy}/{MM}/{dd}/tx-{HH}.csv" --delimiter='\r\n'
#./eth-export-transactions.sh $START $END | ./run-ingest-eth.sh -e tx -o "fs3:///mnt/s4/data/dev/ethereum/raw/csv/transactions/{yyyy}/{MM}/{dd}/tx-{HH}.csv" --delimiter='\r\n'
#./eth-export-transactions.sh $START $END | ./run-ingest-eth.sh -e tx -o "fs3:///data/transactions/{yyyy}/{MM}/{dd}/tx-{HH}.csv" --delimiter='\r\n'

#./eth-export-transactions.sh $START $END | ./run-ingest-eth.sh -e tx --delimiter='\r\n'

