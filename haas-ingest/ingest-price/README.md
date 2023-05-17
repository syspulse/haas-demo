
# Price feeds

## Coinbase

https://api.coinbase.com/v2/prices/ETH-${currencyCode}/spot

Example:

```
curl https://api.coinbase.com/v2/prices/ETH-USD/spot
{"data":{"base":"ETH","currency":"USD","amount":"1577.3"}}
```

## Chainlink 

API: https://docs.chain.link/data-feeds/price-feeds/api-reference



### Oracle

UNI/USD: 0x553303d460ee0afb37edff9be42922d8ff63220e
ETH/USD: 0x5f4ec3df9cbd43714fe2740f5e3616155c5b8419
BTC/USD: 0xf4030086522a5beea4988f8ca5b36dbc97bee88c
AAVE/USD: 0x547a514d5e3769680ce22b2361c10ea13619e8a9
MATIC/USD: 0x7bac85a8a13a4bcd8abb3eb7d6b4d632c5a57676
STETH/USD: 0xcfe54b5cd566ab89272946f602d76ea879cab4a8


__ETH__

https://data.chain.link/ethereum/mainnet/crypto-usd/eth-usd

Contract: [0x5f4ec3df9cbd43714fe2740f5e3616155c5b8419](https://etherscan.io/address/0x5f4ec3df9cbd43714fe2740f5e3616155c5b8419)

```
export ETH_RPC_URL=http://geth.demo.hacken.cloud:8545
```

1. Get last price

```
cast call 0x5f4ec3df9cbd43714fe2740f5e3616155c5b8419 'latestAnswer()(int256)'
```

in USD:
```
price=`cast call 0x5f4ec3df9cbd43714fe2740f5e3616155c5b8419 'latestAnswer()(int256)'`
echo "$price / 100000000.0" | bc
```

2. Get last roundId

```
cast call 0x5f4ec3df9cbd43714fe2740f5e3616155c5b8419 'latestRound()(uint256)'
```

## CryptoCompare

__ATTENTION__: IDs are tickers: `UNI` 

```
curl -X GET 'https://min-api.cryptocompare.com/data/pricemultifull?fsyms=ETH&tsyms=USD' -H 'accept: application/json'| jq .
```

### Ingester:

__WARNING__: Delimiter must be empty !

```
./run-ingest-price.sh -f cryptocomp:// -o stdout:// --delimiter=
 ```

Ingest specific Token set from file:

`tokens.set`:
```
AAVE,LINK,NODE,ETH,ENS,FOAM,GMX,HNT,LPT,MATIC,NEAR,NOIA,OP,PCN,SOL,UNI,RBN
```

```
./run-ingest-price.sh -e cryptocomp -f cryptocomp:// -o stdout://  --tokens=file://tokens.set --delimiter=
```

### Ingest with Resolver 

Resolver from Coingecko token ids:

```
./run-ingest-price.sh ingest -e cryptocomp -t uniswap --resolver=file://cryptocompare/ALL.csv -f 'cryptocomp://' --delimiter=
```

## Coingecko

__ATTENTION__: IDs are keys: `uniswap` 

```
./run-ingest-price.sh -e coingecko -f coingecko:// -o stdout://  --tokens=uniswap,ribbon-finance --delimiter=
```


## Squash

```
amm squash-price.sc \
    --dir /mnt/s4/data/dev/coingecko/raw/json/price \
    --output /mnt/s4/data/dev/coingecko/price \
    --name token
```
