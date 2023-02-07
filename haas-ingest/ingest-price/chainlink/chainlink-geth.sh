#!/bin/bash

FROM=${FROM:-0x0}

curl -H "Content-Type: application/json" geth.hacken.cloud:8545 -X POST --data '{"jsonrpc":"2.0","method":"eth_call","params":[{"from": "0x0000000000000000000000000000000000000000", "to": "0x5f4ec3df9cbd43714fe2740f5e3616155c5b8419", "data": "0x50d25bcd0000000000000000000000000000000000000000000000000000000000000000"}, "latest"],"id":1}'
