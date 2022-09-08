#!/bin/bash
#
# OUTPUT: kafka/localhost:9092

ETH_RPC=${ETH_RPC:-http://api.infura.io}

START_BLOCK=${1:-latest}
OUTPUT=${2}

if [ "$START_BLOCK" != "latest" ]; then
  rm -f last_synced_block.txt
  START_BLOCK_ARG="--start-block $START_BLOCK"
else
  rm -f last_synced_block.txt
  latest=`curl -s POST --data '{"jsonrpc":"2.0","method":"eth_getBlockByNumber","params":["latest", false],"id":1}' -H "Content-Type: application/json" ${ETH_RPC} |jq -r .result.number | xargs printf "%d"`
  START_BLOCK_ARG="--start-block $latest"
fi

case "$OUTPUT" in 
   "") 
      ;;
   "stdout") 
      ;;
   *)
      OUTPUT="--log-file $OUTPUT"
      ;;      
esac

#echo "Block: $START_BLOCK_ARG" >&2

export PYTHONUNBUFFERED="1"
#ethereumetl stream -e log $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 
ethereumetl stream -e token_transfer $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 
#ethereumetl stream -e transaction $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 
#ethereumetl stream -e transaction $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 2>/dev/null
#ethereumetl stream -e transaction $START_BLOCK_ARG --provider-uri $ETH_RPC
