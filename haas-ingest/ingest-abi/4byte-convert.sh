#!/bin/bash

F=${1}

cat $F | jq -c '.results[] | {hex_signature,text_signature}' | sed 's/hex_signature/hex/' | sed 's/text_signature/tex/'
