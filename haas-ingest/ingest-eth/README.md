# ingest-eth

## Ingest Ethereum Tx and Blocks

Ingest transactions from file:
```
./run-ingest-eth.sh ingest -e tx -f feed/tx-1734.log -o stdout://
```

Ingest blocks from file:
```
./run-ingest-eth.sh ingest -e block -f feed/blocks-1.log -o stdout://
```

Stream: geth -> ethereum-etl -> eth-ingest

```
ethereumetl stream -e transaction --start-block 14747950 --provider-uri $ETH_RPC | ./run-ingest-eth.sh ingest -e tx -f stdin://
```

```
rm last_synced_block.txt; ethereumetl stream -e transaction --start-block `eth-last-block.sh` --provider-uri $ETH_RPC | ./run-ingest-eth.sh ingest -e tx -f stdin://
```

## via Kafka

Start Kafka:

[../../infra/docker/kafka](../../infra/docker/kafka)

Run ETL:
```
ETH_RPC=http://geth:8545 ./eth-proxy.sh kafka/localhost:9092 latest
```

Run ingest and Intercept 

__NOTE__: Use different Consumer Groups or Kafka will load balance

```
./run-ingest-eth.sh ingest -e transaction -f kafka://localhost:9092/transactions/group-1 -o stdout://
./run-ingest-eth.sh intercept -e transaction -f kafka://localhost:9092/transactions/group-2 -o stdout:// -s file://scripts/script-1.js
```


## Intercept

Run script which shows transactions >10ETH value:
```
./run-ingest-eth.sh intercept -f file://feed/tx-1734.log -s file://scripts/script-1.js
```

Run from live transactions: [__geth__] -> [__ethereum-etl__] -> [__eth-ingest__]
```
rm last_synced_block.txt; ethereumetl stream -e transaction --start-block `eth-last-block.sh` --provider-uri $ETH_RPC 2>/dev/null | ./run-ingest-eth.sh intercept -f stdin:// -s file://scripts/script-1.js
```
