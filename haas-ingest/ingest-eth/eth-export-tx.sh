#!/bin/bash
#

export ENTITY=export_blocks_and_transactions
#export OUTPUT_FILE="--blocks-output /dev/stdout --transactions-output /dev/stdout"
#export OUTPUT_FILE="--blocks-output /dev/null --transactions-output -"
export OUTPUT_FILE="--blocks-output /dev/null --transactions-output -"
#export OUTPUT_FILE="--blocks-output TX.json --transactions-output TX.json"
export EXTRA="--batch-size 10"
export OUTPUT="stdout"

./eth-export.sh $@

