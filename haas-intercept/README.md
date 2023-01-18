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
ETH_RPC=http://geth2.hacken.cloud:8545 ./eth-stream-intercept-token.sh intercept --alarms='script-token-3.js=token=stdout://
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

Run Intercept as Server

```
OPT=-Dgod ./run-ingest-eth.sh server 
```

Run Intercept as Server and Emulate stream

```
OPT=-Dgod ./run-ingest-eth.sh server -f feed/tx-4000.log --throttle=250
./intercept-create.sh
```
