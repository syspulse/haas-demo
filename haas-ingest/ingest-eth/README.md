# ingest-eth


## Stream 

### From ethereum_etl

Ingest transactions from file:
```
./run-ingest-eth.sh ingest -e tx -f feed/tx-1734.log -o stdout://
```

Ingest blocks from file:
```
./run-ingest-eth.sh ingest -e block -f feed/blocks-1.log -o stdout://
```

Ingest blocks from stored S3 file on the Lake:
```
./run-ingest-eth.sh ingest -e block.lake -f /mnt/s3/feed/blocks-1.csv
```

Stream: geth -> ethereum-etl -> eth-ingest from last PoW block 15537393

```
ENTITY="transaction" ./eth-stream.sh 15537393 | ./run-ingest-eth.sh -e tx
```

With EVM RPC:
```
ETH_RPC=http://geth:8545 ENTITY="transaction" ./eth-stream.sh 15537393 | ./run-ingest-eth.sh -e tx
```


Stream from latest block

```
ENTITY="transaction" ./eth-stream.sh | ./run-ingest-eth.sh -e tx
```

Clean stream:
```
ENTITY="transaction" ./eth-stream.sh 2>/dev/null | ./run-ingest-eth.sh -e tx
```

Stream blocks into S3 compatible mount (no append).

Limit (`--limit=10`) is important when file is rolled over:

```
ENTITY=block ./eth-ingest.sh -e block -o fs3://output/blk-{HH_mm_ss}.log --limit=10
```

Stream Token Transfers to S3 compatible mount:

```
ENTITY=token_transfer ./eth-ingest.sh -e transfer -o fs3://output/transfer-{HH_mm_ss}.log --limit=100
```

Stream from latest block into Hive

```
ENTITY="transaction" ./eth-stream.sh | ./run-ingest-eth.sh -e tx -o 'hive://output/{yyyy}/{MM}/{dd}/transactions'
```

### From RPC Node

__NOTE__: use empty delimiter !

```
./run-ingest-eth.sh -e tx.rpc -f http://geth1:8545 --delimiter=
```


### Import

Export transactions into directory structure for Hive/Spark processing:

```
./eth-export-tx.sh 1000000 1000001 | ./run-ingest-eth.sh -e tx -o 'hive://output/{yyyy}/{MM}/{dd}/transactions
.log'
```

Export Token transfers (with filter by token address)

```
./eth-export-transfers.sh 10000000 10000001 | ./run-ingest-eth.sh ingest -e transfer --filter 0x2b591e99afe9f32eaa6214f7b7629768c40eeb39 -f stdin://
```

## via Kafka

Start Kafka:

[../../infra/docker/kafka](../../infra/docker/kafka)

Run ETL:
```
ETH_RPC=http://geth:8545 ./eth-proxy.sh kafka/localhost:9092 latest
```

Run ingest and Intercept 

__NOTE__: Use different Consumer Groups otherwise Kafka will load balance

```
./run-ingest-eth.sh ingest -e transaction -f kafka://localhost:9092/transactions/group-1 -o stdout://
./run-ingest-eth.sh intercept -e transaction -f kafka://localhost:9092/transactions/group-2 -o stdout:// -s file://scripts/script-1.js
```


## Intercept

Intercept moved to [haas-intercept](../../haas-intercept/README.md)