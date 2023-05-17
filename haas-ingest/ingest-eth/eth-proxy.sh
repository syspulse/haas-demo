#!/bin/bash
#
# OUTPUT: kafka/localhost:9092

#ETH_RPC=${ETH_RPC:-http://geth:8545}
ETH_RPC=${ETH_RPC:-http://geth.demo.hacken.cloud:8545}

#START_BLOCK=${1:-14747950}
OUTPUT=${1}
START_BLOCK=${2:-latest}
#ENTITY=${ENTITY:-token_transfer}
ENTITY=${ENTITY:-transaction}

DOCKER_IMG=${DOCKER_IMG:-syspulse/ethereum-etl:2.0.3.1}
DOCKER_AWS=${DOCKER_AWS:-649502643044.dkr.ecr.eu-west-1.amazonaws.com}

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
      OUTPUT="-o $OUTPUT"
      ;;      
esac

#echo "Block: $START_BLOCK_ARG" >&2

export PYTHONUNBUFFERED="1"
#ethereumetl stream -e block,transaction $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 
#ethereumetl stream -e token_transfer $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 
#ethereumetl stream -e transaction $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 
#ethereumetl stream -e transaction $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 2>/dev/null
#ethereumetl stream -e transaction $START_BLOCK_ARG --provider-uri $ETH_RPC

if [ "$DOCKER" != "" ]; then
   case "$DOCKER" in
     "aws")
         docker run --rm --name eth-proxy ${DOCKER_AWS}/${DOCKER_IMG} stream -e "${ENTITY}" $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT
         ;;
     "default")
         docker run --rm --name eth-proxy ${DOCKER_AWS}/${DOCKER_IMG} stream -e "${ENTITY}" $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT
         ;;
      *)
         docker run --rm --name eth-proxy $DOCKER stream -e "${ENTITY}" $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT
         ;;
   esac
else
   ethereumetl stream -e "$ENTITY" $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT 
fi
