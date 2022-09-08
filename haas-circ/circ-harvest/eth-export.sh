#!/bin/bash
#
# OUTPUT: kafka/localhost:9092

ETH_RPC=${ETH_RPC:-http://api.infura.io}

START_BLOCK=${1:-latest}
END_BLOCK=${2:-latest}
OUTPUT=${3}
ENTITY=${ENTITY:-blocks}
EXTRA=${EXTRA}

if [ "$START_BLOCK" != "latest" ]; then
  rm -f last_synced_block.txt
  START_BLOCK_ARG="-s $START_BLOCK"
else
  rm -f last_synced_block.txt
  latest=`curl -s POST --data '{"jsonrpc":"2.0","method":"eth_getBlockByNumber","params":["latest", false],"id":1}' -H "Content-Type: application/json" ${ETH_RPC} |jq -r .result.number | xargs printf "%d"`
  START_BLOCK_ARG="-s $latest"
fi

if [ "$END_BLOCK" != "latest" ]; then
  rm -f last_synced_block.txt
  END_BLOCK_ARG="-e $END_BLOCK"
else
  rm -f last_synced_block.txt
  latest=`curl -s POST --data '{"jsonrpc":"2.0","method":"eth_getBlockByNumber","params":["latest", false],"id":1}' -H "Content-Type: application/json" ${ETH_RPC} |jq -r .result.number | xargs printf "%d"`
  END_BLOCK_ARG="-e $latest"
fi

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

ethereumetl $ENTITY $START_BLOCK_ARG $END_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT $OUTPUT_FILE $EXTRA
