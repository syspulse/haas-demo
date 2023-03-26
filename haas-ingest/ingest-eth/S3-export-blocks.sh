#!/bin/bash

# 778482  = Dec-31-2015 11:59:52 PM +UTC
# 2912406 = Dec-31-2016 11:59:31 PM +UTC
# 4832685 = Dec-31-2017 11:59:47 PM +UTC
# 6988614 = Dec-31-2018 11:59:42 PM +UTC
# 9193265 = Dec-31-2019 11:59:45 PM +UTC
# 11565018 = Dec-31-2020 11:59:57 PM +UTC
# 13916165 = Dec-31-2021 11:59:49 PM +UTC
# 16308189 = Dec-31-2022 11:59:59 PM +UTC

# 16730071 = Feb-28-2023 11:59:59 PM +UTC

# 10966873 = Sep-30-2020 11:59:45 PM +UTC (2020/09/15)

# 2015 = 0 - 778482
# 2016 = 778483   - 2912406
# 2017 = 2912407  - 4832685
# 2018 = 4832686  - 6988614 
# 2019 = 6988615  - 9193265
# 2020 = 9193266  - 11565018
# 2021 = 11565019 - 13916165
# 2022 = 13916166 - 16308189
# 2023 = 16308190 - 16730071

START=${1:-10861674}
END=${2:-10861675}

#./eth-export-blocks.sh $START $END | ./run-ingest-eth.sh -e block -o "fs3:///mnt/s3/data/dev/ethereum/raw/csv/blocks/{yyyy}/{MM}/{dd}/block-{HH}.csv" --delimiter='\r\n'
#./eth-export-blocks.sh $START $END | ./run-ingest-eth.sh -e block -o "fs3:///mnt/s4/data/dev/ethereum/raw/csv/blocks/{yyyy}/{MM}/{dd}/block-{HH}.csv" --delimiter='\r\n'
#./eth-export-blocks.sh $START $END | ./run-ingest-eth.sh -e block -o "fs3:///data/blocks/{yyyy}/{MM}/{dd}/block-{HH}.csv" --delimiter='\r\n'

./eth-export-blocks.sh $START $END | ./run-ingest-eth.sh -e block --delimiter='\r\n'
