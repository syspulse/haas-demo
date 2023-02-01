#!/bin/bash

# Uniswap2 Router
#CONTRACT=0x7a250d5630b4cf539739df2c5dacb4c659f2488d ABI=abi-simple-Uniswap2-Router.json ./intercept-create.sh Function-1 'ref://script-Function.js' 'stdout://' function

# Uniswap3 Universal Routes
CONTRACT=0xef1c6e67703c7bd7107eed8303fbe6ec2554bf6b ABI=abi-UniswapUniversalRouter-0xef1c6e67703c7bd7107eed8303fbe6ec2554bf6b.json ./intercept-create.sh Function-Uniswap-3 'ref://script-Uniswap3.js' 'stdout://' function
