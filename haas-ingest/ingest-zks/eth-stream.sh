#!/bin/bash
#
# OUTPUT: kafka/localhost:9092

#ETH_RPC=${ETH_RPC:-http://geth.hacken.dev:8545}
ETH_RPC=${ETH_RPC:-https://zksync2-testnet.zksync.dev}

START_BLOCK=${1:-latest}
OUTPUT=${2}
#ENTITY=${ENTITY:-token_transfer}
ENTITY=${ENTITY:-transaction}

DOCKER=${DOCKER:-649502643044.dkr.ecr.eu-west-1.amazonaws.com/syspulse/ethereum-etl:2.0.3.1}

LATEST_BLOCK=${LATEST_BLOCK:-last_synced_block.txt}

if [ "$START_BLOCK" != "latest" ]; then
  if [ "$START_BLOCK" == "sync" ]; then
    START_BLOCK=`cat $LATEST_BLOCK`
    START_BLOCK_ARG="--start-block $START_BLOCK"
  else
    rm -f $LATEST_BLOCK
    START_BLOCK_ARG="--start-block $START_BLOCK"
  fi
else
  rm -f $LATEST_BLOCK
  START_BLOCK=`curl -s POST --data '{"jsonrpc":"2.0","method":"eth_getBlockByNumber","params":["latest", false],"id":1}' -H "Content-Type: application/json" ${ETH_RPC} |jq -r .result.number | xargs printf "%d"`
  START_BLOCK_ARG="--start-block $START_BLOCK"
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
#ethereumetl stream -e token_transfer $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 
#ethereumetl stream -e transaction $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 
#ethereumetl stream -e transaction $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 2>/dev/null
#ethereumetl stream -e transaction $START_BLOCK_ARG --provider-uri $ETH_RPC

>&2 echo "START_BLOCK: $START_BLOCK"

if [ "$DOCKER" != "" ] && [ "$DOCKER" != "none" ] ; then
   >&2 echo "DOCKER: ${DOCKER}"
   docker run --rm --name eth-stream $DOCKER stream -e "${ENTITY}" $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT    
else
   ethereumetl stream -e "${ENTITY}" $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 
fi
 
