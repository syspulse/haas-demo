#!/bin/bash
#
# OUTPUT: kafka/localhost:9092

export ENTITY=export_all
export OUTPUT_FILE="-o ./raw"
export EXTRA="-b 10"

./eth-export.sh $@

