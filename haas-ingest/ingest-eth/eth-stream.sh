#!/bin/bash
#
# OUTPUT: kafka/localhost:9092

#ETH_RPC=${ETH_RPC:-http://geth.hacken.cloud:8545}
#ETH_RPC=${ETH_RPC:-http://geth.hacken.dev:8545}
#ETH_RPC=${ETH_RPC:-https://zksync2-testnet.zksync.dev}
ETH_RPC=${ETH_RPC:-https://optimism-mainnet.infura.io/v3/5619930ed1f84c27aca9231c2a72c279}

START_BLOCK=${1:-latest}
OUTPUT=${2}
#ENTITY=${ENTITY:-token_transfer}
#ENTITY=${ENTITY:-transaction}
ENTITY=${ENTITY:-transaction}

#DOCKER=${DOCKER:-649502643044.dkr.ecr.eu-west-1.amazonaws.com/syspulse/ethereum-etl:2.0.3.1}
DOCKER_AWS=${DOCKER_AWS:-649502643044.dkr.ecr.eu-west-1.amazonaws.com/syspulse/ethereum-etl:2.1.2.1}
DOCKE=DOCKER=${DOCKER:-none}
DOCKER_STATE=${DOCKER_STATE:-state/}

LAST_BLOCK=${LAST_BLOCK:-last_synced_block.txt}
BATCH_SIZE=${BATCH_SIZE:-100}

case "$START_BLOCK" in
   "sync")
      START_BLOCK=`cat $LAST_BLOCK`
      rm -f $LAST_BLOCK
      START_BLOCK_ARG="--start-block $START_BLOCK"
      ;;
   "file" | "last")
      START_BLOCK_ARG=""
      ;;
   "latest")
      rm -f $LAST_BLOCK
      START_BLOCK=`curl -s POST --data '{"jsonrpc":"2.0","method":"eth_getBlockByNumber","params":["latest", false],"id":1}' -H "Content-Type: application/json" ${ETH_RPC} |jq -r .result.number | xargs printf "%d"`
      START_BLOCK_ARG="--start-block $START_BLOCK"
      ;;
   *)
      rm -f $LAST_BLOCK
      START_BLOCK_ARG="--start-block $START_BLOCK"
      ;;
esac

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

if [ "$DOCKER" != "" ]; then
   >&2 echo "DOCKER: ${DOCKER}"

   case "$DOCKER" in
     "aws")
        docker run --rm --name eth-stream \
            -v `pwd`/$DOCKER_STATE:/$DOCKER_STATE \
            $DOCKER_AWS \
            stream -e "${ENTITY}" $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT -l /$DOCKER_STATE/$LAST_BLOCK -b ${BATCH_SIZE}
        ;;     
     *)
        docker run --rm --name eth-stream \
            -v `pwd`/$DOCKER_STATE:/$DOCKER_STATE \
            $DOCKER \
            stream -e "${ENTITY}" $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT -l /$DOCKER_STATE/$LAST_BLOCK -b ${BATCH_SIZE}
        ;;
   esac
   
else
   ethereumetl stream -e "${ENTITY}" $START_BLOCK_ARG --provider-uri $ETH_RPC $OUTPUT -l $LAST_BLOCK -b ${BATCH_SIZE}
fi
 
