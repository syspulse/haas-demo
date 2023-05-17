#!/bin/bash
#
# OUTPUT: kafka/localhost:9092

#ETH_RPC=${ETH_RPC:-http://api.infura.io}
ETH_RPC=${ETH_RPC:-http://geth.demo.hacken.cloud:8545}

START_BLOCK=${1:-latest}
END_BLOCK=${2:-latest}
OUTPUT=${3}
ENTITY=${ENTITY:-export_token_transfers}
EXTRA=${EXTRA}

DOCKER=649502643044.dkr.ecr.eu-west-1.amazonaws.com/syspulse/ethereum-etl:2.0.3.1

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

docker run --rm --name eth-export $DOCKER $ENTITY $START_BLOCK_ARG $END_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT $OUTPUT_FILE $EXTRA

export PYTHONUNBUFFERED="1"
#ethereumetl $ENTITY $START_BLOCK_ARG $END_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT $OUTPUT_FILE $EXTRA
