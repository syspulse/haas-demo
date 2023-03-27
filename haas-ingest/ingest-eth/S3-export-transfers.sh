#!/bin/bash

START=${1:-10861674}

# 10966873 = Sep-30-2020 11:59:45 PM +UTC (2020/09/15)
# 11565018 = Dec-31-2020 11:59:57 PM +UTC
# 11565019 = 2021
# 13916165 = Dec-31-2021 11:59:49 PM +UTC
# 13916166 = 2022
# 16308189 = Dec-31-2022 11:59:59 PM +UTC
# 16308190 = 2023
# 16730071 = Feb-28-2023 11:59:59 PM +UTC

# 2021 = 11565019 - 13916165
# 2022 = 13916166 - 16308189
# 2023 = 16308190 - 16730071

END=${2:-10861675}

#./eth-export-transfers.sh $START $END | ./run-ingest-eth.sh -e transfer -o "fs3:///mnt/s3/data/dev/{yyyy}/{MM}/{dd}/token-{HH_mm_ss}.csv" --limit=10000
#./eth-export-transfers.sh $START $END | ./run-ingest-eth.sh -e transfer -o "fs3:///output/data/dev/ethereum/raw/csv/tokens/{yyyy}/{MM}/{dd}/token-{HH_mm_ss}.csv" --limit=10000
#./eth-export-transfers.sh $START $END | ./run-ingest-eth.sh -e transfer stdout:// --limit=10000

#./eth-export-transfers.sh $START $END | ./run-ingest-eth.sh -e transfer -o "fs3:///mnt/s3/data/dev/ethereum/raw/csv/transfers/{yyyy}/{MM}/{dd}/transfer-{HH}.csv"
#./eth-export-transfers.sh $START $END | ./run-ingest-eth.sh -e transfer -o "fs3:///mnt/s4/data/dev/ethereum/raw/csv/transfers/{yyyy}/{MM}/{dd}/transfer-{HH}.csv"
#./eth-export-transfers.sh $START $END | ./run-ingest-eth.sh -e transfer -o "fs3:///data/transfers/{yyyy}/{MM}/{dd}/transfer-{HH}.csv"

./eth-export-transfers.sh $START $END | ./run-ingest-eth.sh -e transfer