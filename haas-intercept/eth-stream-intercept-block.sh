#!/bin/bash                                                                                                                                                                                            

echo "ETH_RPC: $ETH_RPC"

export ENTITY="block"

./eth-stream.sh 2>/dev/null | ./run-intercept.sh $@ -e block -f stdin://
