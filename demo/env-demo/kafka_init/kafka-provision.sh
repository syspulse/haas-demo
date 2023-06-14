#!/bin/bash
CWD=`echo $(dirname $(readlink -f $0))`

export LD_LIBRARY_PATH=$CWD

BROKER=${1:-broker:9092}

ETHEREUM_MAINNET="ethereum.mainnet.transactions ethereum.mainnet.blocks ethereum.mainnet.logs ethereum.mainnet.transfers ethereum.mainnet.mempool"
ZKSYNC_TESTNET="zksync.testnet.transactions zksync.testnet.blocks zksync.testnet.logs zksync.testnet.transfers zksync.testnet.mempool"
ZKSYNC_MAINNET="zksync.mainnet.transactions zksync.mainnet.blocks zksync.mainnet.logs zksync.mainnet.transfers zksync.mainnet.mempool"
OPTIMISM_MAINNET="optimism.mainnet.transactions optimism.mainnet.blocks optimism.mainnet.logs optimism.mainnet.transfers optimism.mainnet.mempool"

BSC_MAINNET="bsc.mainnet.transactions bsc.mainnet.blocks bsc.mainnet.logs bsc.mainnet.transfers bsc.mainnet.mempool"
AVALANCHE_MAINNET="avalanche.mainnet.transactions avalanche.mainnet.blocks avalanche.mainnet.logs avalanche.mainnet.transfers avalanche.mainnet.mempool"
POLYGON_MAINNET="polygon.mainnet.transactions polygon.mainnet.blocks polygon.mainnet.logs polygon.mainnet.transfers polygon.mainnet.mempool"
ARBITRUM_MAINNET="arbitrum.mainnet.transactions arbitrum.mainnet.blocks arbitrum.mainnet.logs arbitrum.mainnet.transfers arbitrum.mainnet.mempool"

SYS="sys.notify"

provision() {  
  for topic in $1; do
    >&2 echo "Topic: ${topic} -> ${BROKER}"
    
    echo "\n" | $CWD/kafkacat -b ${BROKER} -P -t $topic

    # which kafka-topics.sh >/dev/null
    # if [ "$?" == "0" ]; then
    #   # provision on MSK
    #   kafka-topics.sh --bootstrap-server $BROKER --create --topic $topic
    # fi 
  
  done
}


provision "$ETHEREUM_MAINNET"