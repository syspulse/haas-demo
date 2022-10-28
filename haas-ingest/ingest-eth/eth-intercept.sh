#!/bin/bash                                                                                                                                                                                            

echo "ETH_RPC: $ETH_RPC"

./eth-stream.sh 2>/dev/null | ./run-intercept-eth.sh -f stdin:// $@
