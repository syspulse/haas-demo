## Intercept

Intercept tx > 10ETH from captured file
```
./run-ingest-eth.sh intercept -f file://feed/tx-1734.log -s file://scripts/script-1.js
```

Run from live transactions: [__geth__] -> [__ethereum-etl__] -> [__eth-ingest__]
```
rm last_synced_block.txt; ethereumetl stream -e transaction --start-block `eth-last-block.sh` --provider-uri $ETH_RPC 2>/dev/null | ./run-ingest-eth.sh intercept -f stdin:// -a script-1=stdout://
```

Run the same pipeline in a simple way

```
./run-intercept-eth.sh -s dir://scripts/
```

Run intercept with Alarms

```
./env-dev-aws.sh

./run-intercept-eth.sh -f feed/tx-4000.log --throttle=5 --alarms.throttle=10000 --alarms="script-2.js=stdout://,script-1=email://user-1@mail.org"
```

Run intercept with specific script to notification:

```
./run-intercept-eth.sh -f feed/tx-4000.log --throttle=5 --alarms.throttle=1000 --alarms="script-1.js=stdout://;email://"
```
Run from Eth-Client:

```
./eth-intercept.sh --alarms.throttle=60000 --alarms="script-1.js=email://user@email.io"
```

Run Intercept as Server

```
OPT=-Dgod ./run-ingest-eth.sh server -f feed/tx-4000.log --throttle=250

./intercept-create.sh
```
