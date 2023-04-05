# Circulation Supply Product

Business Service on HaaS Platform

## Modules

- [circ-core](circ-core)            - Core Entities
- [circ-harvest](circ-harvest)      - Notebooks and Processing Pipelines!


----
## Architecture

<img src="doc/_.jpg" width="650" alt="TODO: Add Diagram">

----

## Squash

### Squash

Scans all subdirectories for `circulating_supply.json` and concatenating them into multi-line json

### Squash All

Scanning years (*2015* - *2023* ) 

```
./supply-squash-all.sh /mnt/s3/data/dev/ethereum/token/0x1f9840a85d5af5bf1d1762f925bdaddc4201f984/circulating-supply
mv circulating_supply.json /mnt/s3/data/dev/ethereum/supply/token/0x1f9840a85d5af5bf1d1762f925bdaddc4201f984/

```

Balancer = /mnt/s3/data/dev/ethereum/token/0xba100000625a3754423978a60c9317c58a424e3d 
