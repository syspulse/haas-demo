## ZkSync v2

__Testnet__: https://zksync2-testnet.zksync.dev

__Mainnet__: https://zksync2-mainnet.zksync.io

Latest block:
```
echo '{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":83}' | http POST https://zksync2-testnet.zksync.dev
```

Block:
```
echo '{"jsonrpc":"2.0","method":"eth_getBlockByNumber","params":["0x12e0a9", true],"id":1}' | http POST https://zksync2-testnet.zksync.dev
```

WS:
```
wscat --connect wss://zksync2-testnet.zksync.dev/ws
Connected (press CTRL+C to quit)
> {"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":83}
< {"jsonrpc":"2.0","result":"0x12e1af","id":83}
```

New Block Headers (not Blocks):
```
{"jsonrpc":"2.0", "id": 1, "method": "eth_subscribe", "params": ["newHeads"]}
```

Stream of pending Transactions (A LOT!)
```
{"jsonrpc":"2.0", "id": 1, "method": "eth_subscribe", "params": ["newPendingTransactions"]}
```


----
## ZkSync v1

API: https://docs.zksync.io/apiv02-docs/

### Blocks

Latest block

```
http --json 'https://api.zksync.io/api/v0.2/blocks?from=latest&limit=1&direction=older
```

Last committed:
```
http --json 'https://api.zksync.io/api/v0.2/blocks/lastCommitted'
```

### Transactions

```
http --json 'https://api.zksync.io/api/v0.2/blocks/lastCommitted/transactions?from=latest&limit=100&direction=older'
```


### Tokens

```
http --json 'https://api.zksync.io/api/v0.2/tokens?from=0&limit=10&direction=newer'
```




