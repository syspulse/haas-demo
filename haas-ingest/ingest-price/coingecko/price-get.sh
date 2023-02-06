#!/bin/bash

COINS=${1:-uniswap,ribbon-finance}
PAIR=${2:-usd}

curl -S -s -D /dev/stderr -H 'accept: application/json' "https://api.coingecko.com/api/v3/simple/price?ids=${COINS}&vs_currencies=${PAIR}"
