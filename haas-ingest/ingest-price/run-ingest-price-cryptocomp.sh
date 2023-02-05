#!/bin/bash
CWD=`echo $(dirname $(readlink -f $0))`
cd $CWD

# Note: Delimiter must be empty !

# ./run-ingest-price.sh --ingest.cron=360 -e cryptocomp -f cryptocomp:// --tokens=file://token-set-1.conf --delimiter= -o fs3:///mnt/s3/data/dev/cryptocomp/raw/csv/price/{yyyy}/{MM}/{dd}/price-{HH_mm_ss}.csv
#./run-ingest-price.sh --ingest.cron=1 -e cryptocomp -f http://localhost:8300 --tokens=file://token-set-1.conf --delimiter= -o fs3:///mnt/s4/data/dev/cryptocomp/raw/csv/price/{yyyy}/{MM}/{dd}/price-{HH}_{mm}_{ss}.csv
./run-ingest-price.sh --ingest.cron=360 -e cryptocomp -f cryptocomp --tokens=file://token-set-1.conf --delimiter= -o fs3:///mnt/s4/data/dev/cryptocomp/raw/csv/price/{yyyy}/{MM}/{dd}/price-{HH}_{mm}_{ss}.csv