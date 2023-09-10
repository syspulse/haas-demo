# Export

# Ethereum 

## Blocks

```
2015 = 778482  = Dec-31-2015 11:59:52 PM +UTC
2016 = 2912406 = Dec-31-2016 11:59:31 PM +UTC
2017 = 4832685 = Dec-31-2017 11:59:47 PM +UTC
2018 = 6988614 = Dec-31-2018 11:59:42 PM +UTC
2019 = 9193265 = Dec-31-2019 11:59:45 PM +UTC
2020 = 11565018 = Dec-31-2020 11:59:57 PM +UTC
2021 = 13916165 = Dec-31-2021 11:59:49 PM +UTC
2022 = 16308189 = Dec-31-2022 11:59:59 PM +UTC
2023 = 16730071 = Feb-28-2023 11:59:59 PM +UTC
     
     = 10966873 = Sep-30-2020 11:59:45 PM +UTC (2020/09/15)
```

## Recover incorrect re-org ingested data:

```
ls /mnt/s3/data/dev/ethereum/raw/csv/transactions/2022/10/
12  13  14  15  16  17  18  19  20  21  22  23  24  25  26  27  28  29  30  31
/mnt/s3/data/dev/ethereum/raw/csv/transfers/2022/10/12
```
head -1 transfer-00.csv 
1665532811000,15728307

__Year__

```
2015 = 0 - 778482
2016 = 778483   - 2912406
2017 = 2912407  - 4832685
2018 = 4832686  - 6988614 
2019 = 6988615  - 9193265
2020 = 9193266  - 11565018
2021 = 11565019 - 13916165
2022 = 13916166 - 16308189
2023 = 16308190 - 16730071  (Feb-28-2023)
```

__2023__

Mar = 16730072 - 16950602
Apr = 16950603 - 
Jun = .....    - 17595509 (Jun-30-2023 11:59:59 PM +UTC)
Jul = 17595510

Collector: 18013004 -> 


----

# BSC

## Blocks

```
3593317 = Dec-31-2020 11:59:59 PM +UTC
```

__Year__
```
2020 = 0 - 3593317
2021 = 3593318 - 13969660
2022 = 13969661 - 24393651
2023 = 24393652 - 27814324 (Apr-30-2023 11:59:57 PM +UTC)
       27814325 - 
```


----

# Polygon

Node: [https://wiki.polygon.technology/docs/operate/full-node-docker]

Snapshots: [https://snapshots.polygon.technology/]

## Blocks


__Year__
```
2020 = 0 - 9013758
2021 = 9013759 - 23201013
2022 = 23201014 - 37520355
2023 = 37520356 - 40997394 (Mar-31-2023 11:59:58 PM +UTC)
```


----
# Gaps

```
ls -l /mnt/s3/data/dev/ethereum/raw/csv/logs/2023/04/16/
```

```
-rw-r----- 1 ubuntu ubuntu 35255585 Apr 16 11:09 event-11.csv                                          
-rw-r----- 1 ubuntu ubuntu 20369405 Apr 26 15:38 log-13.csv 
```

```
-rw-r----- 1 ubuntu ubuntu 35255585 Apr 16 11:09 event-11.csv
-rw-r----- 1 ubuntu ubuntu 20369405 Apr 26 15:38 log-13.csv
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
ETH_RPC=http://geth2.demo.hacken.cloud:8545 DOCKER=aws ./S3-export-blocks.sh 10861674 10861674
```

```
ETH_RPC=http://geth2.demo.hacken.cloud:8545 DOCKER=aws ./S3-export-logs.sh 10861674 10861674
```