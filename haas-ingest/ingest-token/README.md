# ingest-gecko

Ingest Coingecko metadata

Ingest supports all standard ```-f (--feed)``` and ```-o (--output)``` urls 

1. Ingest all Coins ids and print

```
./run-ingest-token.sh ingest -e coingecko-coins -f coingecko:// -o stdout://
```

Insgest coins from previous raw feed (json) into CSV 

```
./run-ingest-token.sh ingest -e coingecko-coin -f dir://feed/ -o fs3://csv/gecko.csv
```

Insgest coin from specific file 

__NOTE__: must be single line (csv or `one json per line`)

If __prettified__ multilined json, use ``--delimiter=''` argument

```
./run-ingest-token.sh ingest -e coingecko-coin -f file://store/RSP-uniswap.json
```

Alternatively with direct URL
```
./run-ingest-token.sh ingest -e coingecko-coins -f https://api.coingecko.com/api/v3/coins/list -o stdout://
```

From local `Simulator`:
```
./run-ingest-token.sh ingest -e coingecko-coins -f http://localhost:8100/coins -o stdout://
```

2. Ingest from raw dump into CVS on S3

```
./run-ingest-token.sh ingest -e coingecko-coin -f dir:///mnt/share/data/haas/gecko/tokens/ -o 'fs3:///mnt/s3/data/dev/gecko/csv/all-{YYYY-MM-dd}_{HH_mm_ss}.csv'
```

3. Ingest specific Tokens into Elastic index *token*

```
./run-ingest-token.sh ingest -e coingecko-coin -t id://chainlink,helium -f coingecko:// -o elastic://localhost:9200/token
```

Ingest from file with a list with 1tps throttle

```
./run-ingest-token.sh ingest -e coingecko-coin -t file://COINS.csv -f coingecko:// -o COINS.json --throttle.source=1000
```

Ingest from previous `coingecko-coins` response:

```
./run-ingest-token.sh ingest -e coingecko-coin -t file://RSP-ALL.json -f coingecko:// -o COINS.json --throttle.source=1000
```

