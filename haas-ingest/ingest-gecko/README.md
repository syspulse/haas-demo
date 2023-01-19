# ingest-gecko

Ingest Coingecko metadata

Ingest supports all standard ```-f (--feed)``` and ```-o (--output)``` urls 

1. Ingest all Coins ids and print

```
./run-gecko.sh ingest -e coins -f coingecko:// -o stdout://
```

Insgest coins from previous raw feed (json) into CSV 

```
./run-gecko.sh ingest -e coin -f dir://feed/ -o fs3://csv/gecko.csv
```

Insgest coin from specific file 

__NOTE__: must be single line (csv or `one json per line`)

```
./run-gecko.sh ingest -e coin -f file://store/uniswap.json
```


Alternatively with direct URL
```
./run-gecko.sh ingest -e coins -f https://api.coingecko.com/api/v3/coins/list -o stdout://
```

Against local sim:
```
./run-gecko.sh ingest -e coins -f http://localhost:8100/coins -o stdout://
```

2. Ingest from raw dump into CVS on S3

```
./run-gecko.sh ingest -e coin -f dir:///mnt/share/data/haas/gecko/tokens/ -o 'fs3:///mnt/s3/data/dev/gecko/csv/all-{YYYY-MM-dd}_{HH_mm_ss}.csv'
```

3. Ingest specific Tokens into Elastic index *token*

```
./run-gecko.sh ingest -e coin -t chainlink,helium -f coingecko:// -o elastic://localhost:9200/token
```

Ingest from file with a list with 1tps throttle

```
./run-gecko.sh ingest -e coin -t file://COINS.txt -f coingecko:// -o COINS.json --throttle.source=1000
```