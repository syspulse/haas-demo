## Intercept

Run tx tntercept > 10ETH from captured file 

```
./run-intercept.sh intercept -f file://feed/tx-1734.log --alarms='script-1.js=tx=stdout://'
```

Run interecept all blocks and show gas from captured file

```
./run-intercept.sh -e block intercept --alarms='script-block-1.js=block=stdout://' -f feed/blocks-1.log --throttle=1000
```

Run from EVM

Transactions:
```
ETH_RPC=http://geth:8545 ./eth-stream-intercept-tx.sh intercept --alarms='script-1.js=tx=stdout://'
```

Blocks:
```
ETH_RPC=http://geth:8545 ./eth-stream-intercept-block.sh intercept --alarms='script-block-1.js=block=stdout://'
```

Stable Coins (USDT,USDC) Transfers:
```
ETH_RPC=http://geth2.demo.hacken.cloud:8545 ./eth-stream-intercept-token.sh intercept --alarms='script-token-3.js=token=stdout://
```

Run Tx intercept with Alarms
```
./env-dev-aws.sh

./run-intercept-eth.sh -f feed/tx-4000.log --throttle=5 --alarms.throttle=10000 --alarms="script-2.js=ws://,script-1.js=email://user-1@mail.org"
```

Run Tx intercept with specific script to notification:

```
./run-intercept-eth.sh -f feed/tx-4000.log --throttle=5 --alarms.throttle=1000 --alarms="script-1.js=stdout://;email://"
```

## Run Intercept as Server

```
OPT=-Dgod ./run-ingest-eth.sh server 
```

Run Intercept as Server and Emulate stream

```
OPT=-Dgod ./run-ingest-eth.sh server -f feed/tx-4000.log --throttle=250
./intercept-create.sh
```

Run Server with 2 streams (tx and block)

```
OPT=-Dgod ./run-intercept.sh server --feed.tx=feed/tx-4000.log --feed.block=feed/blocks-1.log --throttle=100
```

Run Server with Event Logs from EthereumETL ignoring all other feeds

```
OPT=-Dgod ETH_RPC=http://geth2.demo.hacken.cloud:8545 ./eth-stream-intercept-event.sh server --feed.event=stdin:// --feed.block=null:// --feed.tx=null:// --feed.token=null://
```


## Multichain

```
OPT=-Dgod ETH_RPC=http://geth.demo.hacken.cloud:8545 DOCKER=none FEED_ETHEREUM_TX=stdin:// ./eth-stream-intercept-tx.sh server --bid=ethereum,zksync
```

```
OPT=-Dgod ETH_RPC=https://zksync2-testnet.zksync.dev DOCKER=none FEED_ETHEREUM_TX=stdin:// ./eth-stream-intercept-tx.sh server --bid=ethereum,zksync
```

Configuration file `application.conf`:

```
feed {
  ethereum {
    tx = "stdin://"
    block = "null://"
    token = "null://"
    event = "null://"
    func = "null://"
  }

  zksync {
    tx = "null://"
    block = "null://"
    token = "null://"
    event = "null://"
    func = "null://"
  }
}
```

## Test transaction

https://etherscan.io/tx/0x6a3d8584a6272a1d73ff297592b401fe10d3a90fd385efff55f68f32f29ecf61


## Scripts

### Transaction

Script can access transaction fields with direct varibale name (e.g `value + 10`).

If Script returns __non__ `null` value, it is used as String in the body of generated Alarm.
Json string can be returned for kafka or http webhook trigger. HTML string can be passed to email trigger as body.

If Script returns `null`, Transaction will not generate any alarm

The list of injected variables for __Transaction__:

`from_address` - Addres with prefix '0x'
`to_address`   - Can be __null__ !
`value`        - BigInt (number)
`gas`          - Max gas
`price`        - gwei price for gas
`input`        - Input data for Contract (0x0 for native transfer)
`block_number` - Block number
`hash`         - Transaction hash
`ts`           - Timestamp im __milliseconds__
`nonce`        - Nonce 
`max_fee`      - Base Max Fee
`max_tip`      - Miner Tip
`type`         - Tx type
`gas_used_cumulative` - Cumulative gas consumed
`gas_used`      - Gas used
`contract`      - Contract code (if contract is deployed)
`receipt_root`  - Receipt root hash
`status`        - Transaction status (0 - Failed, 1 - OK)
`price_effective` - Effective price

