
FILE=${1:-ALL.json}
cat $FILE | jq -r '.[] | .id +","+.symbol'
