# Mempool

## geth

### Content (Large file)

```
curl -i -H "Content-Type: application/json" -X POST http://geth2.hacken.cloud:8545 --data '{"jsonrpc": "2.0", "method": "txpool_content", "params": [], "id": 0}'
```

### Inspect (brief summary)

```
curl -i -H "Content-Type: application/json" -X POST http://geth2.hacken.cloud:8545 --data '{"jsonrpc": "2.0", "method": "txpool_inspect", "params": [], "id": 0}'
```

## Ingester

### Ingest from EVM node:

__WARNING__: default format is `json` !

```
./run-ingest-mempool.sh ingest -e evm -f data/mempool-3.json -o txpool.json
```

```
./run-ingest-mempool.sh ingest -e evm -f http://geth2.hacken.cloud:8545 --ingest.cron=60
```

### Ingest from EVM txpool (`json` interchage)

Default is `csv` 

```
./run-ingest-mempool.sh ingest -e mempool -f txpool.json -o file://mempool.csv
```

