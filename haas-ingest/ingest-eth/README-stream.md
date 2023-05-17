# Streaming

Requires customized ethereum-etl:

1) Adds timestamp to token_transfer stream

## Limitations

1. *trace,token,contract* streaming is __NOT__ supported with geth
2. openethereum (tracing support in stream) is deprecated !
3. exporting logs is not possible without pre-exporting transactions (like in stream)

## Stream 

Streaming is by default in __JSON__ !

Preconfigure RPC endpoint

```
export ETH_RPC=http://geth:8545
```

Stream blocks:
```
ENTITY=block ./eth-stream.sh
```

Stream transactions:
```
ENTITY=transaction ./eth-stream.sh
```

Stream token transfer:
```
ENTITY=token_transfer ./eth-stream.sh
```

Stream event logs:
```
ENTITY=log ./eth-stream.sh
```

Stream ALL (__requires openethereum__ !):

```
ENTITY="block,transaction,log,token_transfer,trace,contract,token" ./eth-stream.sh
```

Stream ALL (no tracing required):
```
ENTITY="block,transaction,log,token_transfer" ./eth-stream.sh
```

Stream with docker:

```
ETH_RPC=http://geth2.demo.hacken.cloud:8545 DOCKER=aws ENTITY=log ./eth-stream.sh
```
