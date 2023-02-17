#!/bin/bash

ENTITY=${1:-signatures}
#ENTITY=${ENTITY:-event-signatures}

PAGE=${2:-1}
OUT=${3:-raw}
DELAY=${DELAY:-60}
NEXT_URL="https://www.4byte.directory/api/v1/$ENTITY/?page=${PAGE}"

DELAY_ERR=${DELAY_ERR:-300}

mkdir -p $OUT/

while [ "$NEXT_URL" != "" ]; do

  file=$OUT/$ENTITY-${PAGE}.json
  >&2 echo "-> $file"

  curl -H "Content-Type: application/json" "$NEXT_URL" >$file
  #rsp=`cat event-1.json`

  #echo $rsp >$file
 
  count=`cat $file | jq .count`
  err=$?

  if [ "$err" == "0" ]; then
    NEXT_URL=`cat $file | jq -r .next`

    >&2 echo "count: $count"
    >&2 echo "next: $NEXT_URL"

    sleep $DELAY

    end=$((PAGE * 100))
    
    PAGE=$((PAGE + 1))
    
    if ((end > count)); then 
      >&2 "Finished: ${end}"
      NEXT_URL=""
    fi

  else
    sleep $DELAY_ERR
  fi

  
done

