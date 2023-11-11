#!/bin/bash                                                                                                                                                                                            

echo "ETH_RPC: $ETH_RPC"

export ENTITY="transaction"

./eth-stream.sh 2>/dev/null | ./run-intercept.sh intercept --alarms='script-Contract-Deploy.js=none://' -e tx -f stdin:// $@
