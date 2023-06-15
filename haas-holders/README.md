
__ATTENTION__: Don't forget to run in UTC timezone for correct time ranges:

```
TZ=UTC GOD=1 ./run-holders.sh server --datastore=dir:///mnt/s4/data/dev/ethereum/token
```

```
./holders-get.sh 0x1f9840a85d5af5bf1d1762f925bdaddc4201f984 2020-09-14 2020-09-15 | jq .
```