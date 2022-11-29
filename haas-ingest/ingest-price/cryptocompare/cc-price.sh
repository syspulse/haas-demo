#!/bin/bash
TOKEN=${1:-UNI}
curl -X GET "https://min-api.cryptocompare.com/data/pricemulti?fsyms=${TOKEN}&tsyms=USD" -H 'accept: application/json'| jq . >price-$TOKEN.json

