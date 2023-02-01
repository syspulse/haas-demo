#!/bin/bash

CONTRACT=0xdac17f958d2ee523a2206206994597c13d831ec7 ABI=abi-simple-ERC20.json ./intercept-create.sh Event-1 'ref://script-Event.js' 'stdout://' event
