# Export

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