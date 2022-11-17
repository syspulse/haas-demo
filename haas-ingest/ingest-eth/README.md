# ingest-eth

## Ingest Ethereum Tx and Blocks


### Stream 

Ingest transactions from file:
```
./run-ingest-eth.sh ingest -e tx -f feed/tx-1734.log -o stdout://
```

Ingest blocks from file:
```
./run-ingest-eth.sh ingest -e block -f feed/blocks-1.log -o stdout://
```

Stream: geth -> ethereum-etl -> eth-ingest from last PoW block 15537393

```
ENTITY="transaction" ./eth-stream.sh 15537393 | ./run-ingest-eth.sh -e tx
```

Stream from latest block

```
ENTITY="transaction" ./eth-stream.sh | ./run-ingest-eth.sh -e tx
```

Stream blocks into S3 compatible mount (no append).

Limit (`--limit=10`) is important when file is rolled over:

```
ENTITY=block ./eth-ingest.sh -e block -o fs3://output/blk-{HH_mm_ss}.log --limit=10
```

Stream Tokens to S3 compatible mount:

```
ENTITY=token_transfer ./eth-ingest.sh -e token -o fs3://output/token-{HH_mm_ss}.log --limit=100
```

Stream from latest block into Hive

```
ENTITY="transaction" ./eth-stream.sh | ./run-ingest-eth.sh -e tx -o 'hive://output/{yyyy}/{MM}/{dd}/transactions'
```

### Import

Export transactions into directory structure for Hive/Spark processing:

```
./eth-export-tx.sh 1000000 1000001 | ./run-ingest-eth.sh -e tx -o 'hive://output/{yyyy}/{MM}/{dd}/transactions
.log'
```

Export Token transfers (with filter by token address)

```
./eth-export-tokens.sh 10000000 10000001 | ./run-ingest-eth.sh ingest -e token --filter 0x2b591e99afe9f32eaa6214f7b7629768c40eeb39 -f stdin://
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

Intercept tx > 10ETH from captured file
```
./run-ingest-eth.sh intercept -f file://feed/tx-1734.log -s file://scripts/script-1.js
```

Run from live transactions: [__geth__] -> [__ethereum-etl__] -> [__eth-ingest__]
```
rm last_synced_block.txt; ethereumetl stream -e transaction --start-block `eth-last-block.sh` --provider-uri $ETH_RPC 2>/dev/null | ./run-ingest-eth.sh intercept -f stdin:// -s file://scripts/script-1.js
```

Run the same pipeline in a simple way

```
./run-intercept-eth.sh -s file://scripts/script-1.js
```

Run intercept with Email notifications

```
./env-dev-aws.sh

./run-intercept-eth.sh -f feed/tx-4000.log --throttle=5 --alarms.throttle=10000 --alarms="stdout://,email://user-1@mail.org"
```

Run intercept with specific script to notification:

```
./run-intercept-eth.sh -f feed/tx-4000.log --throttle=5 --alarms.throttle=1000 --alarms="SCRIPT-file://scripts/script-1.js=stdout://;email://"
```
