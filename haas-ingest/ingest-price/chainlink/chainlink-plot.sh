#!/bin/bash

DELAY=`echo "60 * 10" | bc`
F=

while [ 1 ]; do 
  data=`cast call 0x5f4ec3df9cbd43714fe2740f5e3616155c5b8419 'latestAnswer()(int256)'`
  price=`echo "$data / 100000000.0" | bc`
 
  echo $price
  sleep $DELAY;
#done | tee
done | ttyplot
