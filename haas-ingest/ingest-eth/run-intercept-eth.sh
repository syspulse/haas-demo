#!/bin/bash                                                                                                                                                                                            

SCRIPT=${1:-file://scripts/script-1.js}

echo "script: $SCRIPT"
echo "ETH_RPC: $ETH_RPC"

rm last_synced_block.txt; 
ethereumetl stream -e transaction --start-block `eth-last-block.sh` --provider-uri $ETH_RPC 2>/dev/null | \
   ./run-ingest-eth.sh intercept -f stdin:// -s file://scripts/script-1.js