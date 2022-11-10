#!/bin/bash                                                                                                                                                                                            

SCRIPT=${SCRIPT:-file://scripts/script-1.js}

echo "script: $SCRIPT"
echo "ETH_RPC: $ETH_RPC"

./run-ingest-eth.sh intercept $@
