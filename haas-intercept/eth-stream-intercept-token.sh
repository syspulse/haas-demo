#!/bin/bash                                                                                                                                                                                            

echo "ETH_RPC: $ETH_RPC"

export ENTITY="token_transfer"

./eth-stream.sh 2>/dev/null | ./run-intercept.sh $@ -e token -f stdin://
