#!/bin/bash
CWD=`echo $(dirname $(readlink -f $0))`
cd $CWD

# Note: Delimiter must be empty !

./run-ingest-price.sh --ingest.cron=3600 -e chainlink -f chainlink:// --tokens=file://default-tokens.csv --delimiter= -o fs3:///mnt/s4/data/dev/chainlink/raw/csv/price/{yyyy}/{MM}/{dd}/price-{HH}_{mm}_{ss}.csv
