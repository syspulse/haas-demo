#!/bin/bash
#
# OUTPUT: kafka/localhost:9092

export ENTITY=export_token_transfers
export OUTPUT_FILE=--output TOKENS.csv

./eth-export.sh $@

