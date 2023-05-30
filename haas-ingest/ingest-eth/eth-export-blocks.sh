#!/bin/bash

export ENTITY=blocks_and_transactions
export OUTPUT_FILE="--blocks-output - --transactions-output /dev/null"
export EXTRA=""
export OUTPUT="stdout"

./eth-export.sh $@