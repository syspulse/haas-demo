#!/bin/bash

COIN=${1:-uniswap}
PAIR=${2:-usd}

curl -S -s -D /dev/stderr -H 'accept: application/json' "https://api.coingecko.com/api/v3/coins/${COIN}/market_chart?vs_currency=${PAIR}&days=1"
