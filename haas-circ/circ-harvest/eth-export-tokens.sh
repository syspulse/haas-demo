#!/bin/bash
#
# use | grep -e '^0x1f9840a85d5af5bf1d1762f925bdaddc4201f984' to filter correctly contract address
# 
# People send tokens to ERC20 contracts

export ENTITY=export_token_transfers
export OUTPUT_FILE=--output TOKENS.csv

./eth-export.sh $@

