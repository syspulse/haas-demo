# Export

## Blocks

```
778482  = Dec-31-2015 11:59:52 PM +UTC
2912406 = Dec-31-2016 11:59:31 PM +UTC
4832685 = Dec-31-2017 11:59:47 PM +UTC
6988614 = Dec-31-2018 11:59:42 PM +UTC
9193265 = Dec-31-2019 11:59:45 PM +UTC
11565018 = Dec-31-2020 11:59:57 PM +UTC
13916165 = Dec-31-2021 11:59:49 PM +UTC
16308189 = Dec-31-2022 11:59:59 PM +UTC

16730071 = Feb-28-2023 11:59:59 PM +UTC

10966873 = Sep-30-2020 11:59:45 PM +UTC (2020/09/15)

2015 = 0 - 778482
2016 = 778483   - 2912406
2017 = 2912407  - 4832685
2018 = 4832686  - 6988614 
2019 = 6988615  - 9193265
2020 = 9193266  - 11565018
2021 = 11565019 - 13916165
2022 = 13916166 - 16308189
2023 = 16308190 - 16730071
```

----
/mnt/s3/data/dev/ethereum/raw/csv/transfers/2020/09/14:

```
7665213 Mar 22 14:57 transfer-18.csv
```

```
head -1 transfer-19.csv 
1600110056000,10861903,0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2,0x7a250d5630b4cf539739df2c5dacb4c659f2488d,0xd9280d19271e26299cfa6b1ca093e557c146ff92,5000000000000000000,0x240a1fe57b6847071d313a782eb654ceebf3ef610cd74270c0b5559510e2d199,1
```


----
Requires customized ethereum-etl:

Export to stdout is by default in __CSV__ !

Export token transfers to `stdout`

```
ENTITY="token_transfers" ./eth-export.sh
```

Export All (`block`,`tx`,`log`,`token_transfer`) to Hive directory:
```
ENTITY=all OUTPUT=output ./eth-export.sh 0 1
```

Files will have `\r\n`, processing requires delimiter:
```
./run-ingest-eth.sh -e block -f dirs://output/blocks --delimiter='\r\n
```

## Export to S3

```
ETH_RPC=http://geth2.hacken.cloud:8545 DOCKER=aws ./S3-export-blocks.sh 10861674 10861674
```

```
ETH_RPC=http://geth2.hacken.cloud:8545 DOCKER=aws ./S3-export-logs.sh 10861674 10861674
```