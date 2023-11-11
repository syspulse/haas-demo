#!/bin/bash                                                                                                                                                                                            

echo "ETH_RPC: $ETH_RPC"

export ENTITY="transaction"

./eth-stream.sh 2>/dev/null | ./run-intercept.sh intercept -e transaction -f stdin:// $@
