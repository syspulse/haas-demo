while [ 1 ]; do curl -s http://geth1.demo.hacken.cloud:8545 -POST -H "Content-Type: application/json" -d '{"jsonrpc":"2.0","method":"eth_getBlockByNumber","params":["latest",false],"id":1}' | jq -r '.result.number+" "+.result.hash' | awk '{printf("%s %d %s\n",strftime("%T"),strtonum($1),$2)}'; sleep 1; done

