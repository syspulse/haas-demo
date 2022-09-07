#!/bin/bash
#
# OUTPUT: kafka/localhost:9092

ETH_RPC=${ETH_RPC:-http://api.infura.io}

START_BLOCK=${1:-14747950}
END_BLOCK=${2:-latest}

OUTPUT=${3}

if [ "$END_BLOCK" != "latest" ]; then
  rm -f last_synced_block.txt
  END_BLOCK_ARG="--end-block $END_BLOCK"
else
  rm -f last_synced_block.txt
  latest=`curl -s POST --data '{"jsonrpc":"2.0","method":"eth_getBlockByNumber","params":["latest", false],"id":1}' -H "Content-Type: application/json" ${ETH_RPC} |jq -r .result.number | xargs printf "%d"`
  END_BLOCK_ARG="--end-block $latest"
fi

START_BLOCK_ARG="--start-block $START_BLOCK"

case "$OUTPUT" in 
   "") 
      ;;
   "stdout") 
      ;;
   *)
      OUTPUT="-o $OUTPUT"
      ;;      
esac

#echo "Block: $START_BLOCK_ARG" >&2

export PYTHONUNBUFFERED="1"
ethereumetl export_token_transfers $START_BLOCK_ARG $END_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 
#ethereumetl stream -e transaction $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 
#ethereumetl stream -e transaction $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 2>/dev/null
#ethereumetl stream -e transaction $START_BLOCK_ARG --provider-uri $ETH_RPC
