## ZkSync

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




