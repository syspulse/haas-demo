#!/bin/bash

WEB3=${1:-infura}

source web3-provider-keys.sh

case "$WEB3" in
  "haas")
    ETH_RPC=http://geth.demo.hacken.cloud:8545
    ;;
  "infura")
    ETH_RPC=https://mainnet.infura.io/v3/$INFURA_KEY
    ;;
  "zeeve")
    ETH_RPC=https://app.zeeve.io/shared-api/bsc/$ZEEVE_KEY
    ;;
  "qn"|"quicknode")
    ETH_RPC=https://capable-distinguished-uranium.bsc.discover.quiknode.pro/$QN_KEY
    ;;    
  "vc"|"validationcloud")
    #ETH_RPC=https://mainnet.bsc.validationcloud.io/v1/IvlXDbzM-2em7n8cHkUdNgzh8qpm2ytj9OsN1n0zmyM
    ETH_RPC=https://mainnet.ethereum.validationcloud.io/v1/$VC_KEY
    ;;

esac

>&2 echo "ETH_RPC=${ETH_RPC}"

curl $ETH_RPC \
  -X POST \
  -H "Content-Type: application/json" \
  -d '[{"jsonrpc": "2.0", "id": 1, "method": "eth_blockNumber", "params": []},
{"jsonrpc": "2.0", "id": 4, "method": "eth_blockNumber", "params": []}]'
