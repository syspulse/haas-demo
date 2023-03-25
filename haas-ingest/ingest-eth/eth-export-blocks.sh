#!/bin/bash

export ENTITY=blocks_and_transactions
export OUTPUT_FILE="--blocks-output -"
export EXTRA=""
export OUTPUT=NONE

./eth-export.sh $@

