#!/bin/bash

START=${1:-10861674}
END=${2:-10861675}

#./eth-export-blocks.sh $START $END 2>/dev/null | ./run-ingest-eth.sh -e block -o "fs3:///mnt/s3/data/dev/ethereum/raw/csv/blocks/{yyyy}/{MM}/{dd}/block-{HH}.csv" --delimiter='\r\n'
#./eth-export-blocks.sh $START $END 2>/dev/null | ./run-ingest-eth.sh -e block -o "fs3:///mnt/s4/data/dev/ethereum/raw/csv/blocks/{yyyy}/{MM}/{dd}/block-{HH}.csv" --delimiter='\r\n'
#./eth-export-blocks.sh $START $END 2>/dev/null | ./run-ingest-eth.sh -e block -o "fs3:///data/blocks/{yyyy}/{MM}/{dd}/block-{HH}.csv" --delimiter='\r\n'

./eth-export-blocks.sh $START $END 2>/dev/null | ./run-ingest-eth.sh -e block --delimiter='\r\n'
