#!/bin/bash
#

export ENTITY=blocks_and_transactions
#export OUTPUT_FILE="--blocks-output /dev/stdout --transactions-output /dev/stdout"
export OUTPUT_FILE="--blocks-output - --transactions-output -"
#export OUTPUT_FILE="--blocks-output TX.json --transactions-output TX.json"
export EXTRA="--batch-size 10"

./eth-export.sh $@

