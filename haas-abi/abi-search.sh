#!/bin/bash

TAGS=${1}
FROM=${2}
SIZE=${3}

ENTITY=${ENTITY:-}

SERVICE_URI=${SERVICE_URI:-http://127.0.0.1:8080/api/v1/abi}
ACCESS_TOKEN=${ACCESS_TOKEN-`cat ACCESS_TOKEN`}

if [ "$ENTITY" != "" ] || [ "$FROM" != "" ] || [ "$SIZE" != "" ]; then
   PREFIX="?"
fi
if [ "$ENTITY" != "" ]; then
   PREFIX=$PREFIX"entity=${ENTITY}&"
fi
if [ "$FROM" != "" ]; then
   PREFIX=$PREFIX"from=${FROM}&"
fi
if [ "$SIZE" != "" ]; then
   PREFIX=$PREFIX"size=${SIZE}"
fi


curl -S -s -D /dev/stderr -X GET -H 'Content-Type: application/json' -H "Authorization: Bearer $ACCESS_TOKEN" $SERVICE_URI/search/${TAGS}${PREFIX}
