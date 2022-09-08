# circ-harvest

Circulation Supply Harvesting

1. totalSupply
2. supply
3. holders

## totalSupply

From Contract (from Ethereum Nodes cluster)

### Prerequisites

- Contract Address (from Gecko metadata). __haas-token__
- Contract Address historical state index. __chifra__


## supply

1. From historical set of logs (if Contract emits it) or transactions (from Contract Deployment) with supply change function:

- transfer()
- transferFrom()
- mint()
- emission()
- unlock()

2. From historical balances of all addresses which were token holders

- chifra index for all accounts (need Archive node)
- sum of all account holders at each block (archive node)
- sum of all account holders at each block (self-index based on Logs/Tx)

### Prerequisites

- Contract Address (from Gecko metadata). __haas-token__


## holders

Repository of all Addresses which ever held Token. 
It makes sense to scan for only addresses which had transfers.
Contracts which emit tokens will automatically be added as _from (when emitting)

Just looking at transactions input data for __transfer__ and __transferFrom__ is not enough.

Contracts can execute ```transfer```/```transferFrom``` and will never be visible in transaction data (e.g. Uniswap Swap).

Most contracts implement Events and can be ingested as Logs. Contracts which do not emit Events are the worst and need Node tracing !

### Using Event Logs

1. Ingest all Logs from geth

Example: https://etherscan.io/tx/0xcaac85e63374b4e0d06cd52dc5aa8cd7c3733f5fe7bb639f551f31af4a688533#eventlog


```
{
  "type": "log",
  "log_index": 42,
  "transaction_hash": "0xcaac85e63374b4e0d06cd52dc5aa8cd7c3733f5fe7bb639f551f31af4a688533",
  "transaction_index": 18,
  "address": "0xdac17f958d2ee523a2206206994597c13d831ec7",
  "data": "0x0000000000000000000000000000000000000000000000000000000068e8ad63",
  "topics": [
    "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
    "0x0000000000000000000000005987775fe4c107ce9e3e709c67f8e9ce03a8b120",
    "0x00000000000000000000000031a36512d4903635b7dd6828a934c3915a5809be"
  ],
  "block_number": 15490185,
  "block_timestamp": 1662552059,
  "block_hash": "0x9961da86938764483cf5dbb26e41312b1590ea6f2c8dbe8b8e481f8faf64d303",
  "item_id": "log_0xcaac85e63374b4e0d06cd52dc5aa8cd7c3733f5fe7bb639f551f31af4a688533_42",
  "item_timestamp": "2022-09-07T12:00:59Z"
}

```
Etherscan parsing:

```
Address: 0xdac17f958d2ee523a2206206994597c13d831ec7
Name: Transfer (index_topic_1 address from, index_topic_2 address to, uint256 value)View Source

Topics:
0 0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef
1  0x5987775fe4c107ce9e3e709c67f8e9ce03a8b120
2  0x31a36512d4903635b7dd6828a934c3915a5809be

Data:
value : 1760079203
```

Signature of Transfer(address _from,address _to,uint256 _value) Event: ```0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef```

The same signature is used in majority of ERC20 Transfer events

2. Parse all logs for ```0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef``` and store addresses (_from,_to)

### Using ethereum-etl token transfer

Transfer even is automatically parsed into ->

Example (USDT transfer):

```
{
  "type": "token_transfer",
  "token_address": "0xdac17f958d2ee523a2206206994597c13d831ec7",
  "from_address": "0xffec0067f5a79cff07527f63d83dd5462ccf8ba4",
  "to_address": "0xb57de0657f580f5bab6903454516ad49d4c3b19d",
  "value": 24980559,
  "transaction_hash": "0x7952ecf424fd56b651d30598c3a8844bd892cf4523303ec612840618387b1507",
  "log_index": 36,
  "block_number": 15490222,
  "block_timestamp": 1662552636,
  "block_hash": "0xcbd42bb2e280b81c967475afad4efc0937a32d6da2f0c3daea1953bb369bfeac",
  "item_id": "token_transfer_0x7952ecf424fd56b651d30598c3a8844bd892cf4523303ec612840618387b1507_36",
  "item_timestamp": "2022-09-07T12:10:36Z"
}
```

### Testing on UNI token

UNI ERC20 Contract: ```0x1f9840a85d5af5bf1d1762f925bdaddc4201f984```

Deploy Tx: https://etherscan.io/tx/0x4b37d2f343608457ca3322accdab2811c707acf3eb07a40dd8d9567093ea5b82

Block: 10861674 

First holder: ```0x41653c7d61609d856f29355e404f310ec4142cfb```

(Log: https://etherscan.io/tx/0x4b37d2f343608457ca3322accdab2811c707acf3eb07a40dd8d9567093ea5b82#eventlog)

Example to see first Holder:

```
./eth-export.sh 10861674 10861675|grep 0x1f9840a85d5af5bf1d1762f925bdaddc4201f984

0x1f9840a85d5af5bf1d1762f925bdaddc4201f984,0x0000000000000000000000000000000000000000,0x41653c7d61609d856f29355e404f310ec4142cfb,1000000000000000000000000000,0x4b37d2f343608457ca3322accdab2811c707acf3eb07a40dd8d9567093ea5b82,23,10861674
```

```10861674``` - Block number

