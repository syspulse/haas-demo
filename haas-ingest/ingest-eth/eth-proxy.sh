#!/bin/bash

ETH_RPC=http://192.168.1.13:8545

#START_BLOCK=${1:-14747950}
START_BLOCK=${1:-latest}

if [ "$START_BLOCK" != "latest" ]; then
  rm -f last_synced_block.txt
  START_BLOCK_ARG="--start-block $START_BLOCK"
else
  rm -f last_synced_block.txt
  latest=`curl -s POST --data '{"jsonrpc":"2.0","method":"eth_getBlockByNumber","params":["latest", false],"id":1}' -H "Content-Type: application/json" ${ETH_RPC} |jq -r .result.number | xargs printf "%d"`
  START_BLOCK_ARG="--start-block $latest"
fi

#echo "Block: $START_BLOCK_ARG"

export PYTHONUNBUFFERED="1"
ethereumetl stream -e transaction $START_BLOCK_ARG --provider-uri $ETH_RPC 2>/dev/null
#ethereumetl stream -e transaction $START_BLOCK_ARG --provider-uri $ETH_RPC