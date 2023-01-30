#!/bin/bash                                                                                                                                                                                            

echo "ETH_RPC: $ETH_RPC"

export ENTITY="log"

./eth-stream.sh 2>/dev/null | ./run-intercept.sh $@ -e event
