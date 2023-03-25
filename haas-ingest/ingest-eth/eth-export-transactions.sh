#!/bin/bash

export ENTITY=blocks_and_transactions
export OUTPUT_FILE="--blocks-output /dev/null --transactions-output -"
export EXTRA=""
export OUTPUT="stdout"

./eth-export.sh $@

