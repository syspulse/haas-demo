#!/bin/bash
#
# OUTPUT: kafka/localhost:9092

export ENTITY=export_blocks_and_transactions
export OUTPUT_FILE="--blocks-output /dev/null --transactions-output /dev/stdout"
export EXTRA="--batch-size 10"

./eth-export.sh $@

