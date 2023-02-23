#!/bin/bash

ADDR=${1:-0xdc24316b9ae028f1497c275eb9192a3ea0f67022}
URL=${URL:-https://etherscan.io/address}

>&2 echo "${ADDR} -> ${URL}"

label=`curl $URL/${ADDR} | grep -A 1 \<title| tail -1 | cut -d '|' -f 1`

echo $label
