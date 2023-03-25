#!/bin/bash
#

#ETH_RPC=${ETH_RPC:-http://api.infura.io}
#ETH_RPC=${ETH_RPC:-http://geth2.hacken.cloud:8545}
ETH_RPC=${ETH_RPC:-http://geth2.hacken.dev:8545}

START_BLOCK=${1:-latest}
END_BLOCK=${2:-latest}
OUTPUT=${OUTPUT:- -}
ENTITY=${ENTITY:-token_transfers}
EXTRA=${EXTRA}

BUCKET_DIR=${BUCKET_DIR:-/data}

DOCKER_AWS=${DOCKER_AWS:-649502643044.dkr.ecr.eu-west-1.amazonaws.com/syspulse/ethereum-etl:2.1.2.2}

DOCKER=${DOCKER:-aws}
#DOCKER=${DOCKER:-none}

WORKERS=${WORKERS:-1}
BATCH=${BATCH:-100}

case "$ENTITY" in 
   "token_transfers")
      BATCH_ARG="-b $BATCH"
      ;;
   "all")
      BATCH_ARG="-B $BATCH"
      ;;
   *)
      BATCH_ARG=""
      ;;
esac


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
   "NONE")
      OUTPUT=""
      ;;
   "stdout") 
      OUTPUT=""
      ;;
   *)
      OUTPUT="-o $OUTPUT"
      ;;      
esac

>&2 echo "Blocks: $START_BLOCK_ARG  $END_BLOCK_ARG"
>&2 echo "WORKERS: $WORKERS"
>&2 echo "BATCH: $BATCH"

export PYTHONUNBUFFERED="1"

if [ "$DOCKER" != "" ]; then   
   >&2 echo "DOCKER: $DOCKER"
   
   case "$DOCKER" in
     "aws")
        docker run --rm --name eth-export-${START_BLOCK} \
            -v $BUCKET_DIR:$BUCKET_DIR \
            ${DOCKER_AWS} \
            export_$ENTITY $START_BLOCK_ARG $END_BLOCK_ARG \
            -w $WORKERS \
            ${BATCH_ARG} \
            --provider-uri $ETH_RPC $OUTPUT $OUTPUT_FILE $EXTRA
        ;;     
     *)
        docker run --rm --name eth-export-${START_BLOCK} \
            -v $BUCKET_DIR:$BUCKET_DIR \
            ${DOCKER} \
            export_$ENTITY $START_BLOCK_ARG $END_BLOCK_ARG \
            -w $WORKERS \
            ${BATCH_ARG} \
            --provider-uri $ETH_RPC $OUTPUT $OUTPUT_FILE $EXTRA  
        ;;
   esac
else
  # requires patched ethereum-etl: https://github.com/syspulse/ethereum-etl/tree/feature/export-tokens-timestamp
  ethereumetl export_${ENTITY} $START_BLOCK_ARG $END_BLOCK_ARG \
   -w $WORKERS \
   ${BATCH_ARG} \
   --provider-uri $ETH_RPC $OUTPUT $OUTPUT_FILE $EXTRA
fi
