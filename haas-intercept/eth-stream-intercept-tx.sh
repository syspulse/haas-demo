#!/bin/bash                                                                                                                                                                                            

echo "ETH_RPC: $ETH_RPC"

export ENTITY="tx"

./eth-stream.sh 2>/dev/null | ./run-intercept.sh intercept -e tx -f stdin:// $@