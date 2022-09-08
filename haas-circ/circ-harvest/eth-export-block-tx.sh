#!/bin/bash
#
# OUTPUT: kafka/localhost:9092

export ENTITY=export_blocks_and_transactions
export OUTPUT_FILE="--blocks-output raw/block/block.csv --transactions-output raw/tx/tx.csv"
export EXTRA="--batch-size 10"

./eth-export.sh $@

