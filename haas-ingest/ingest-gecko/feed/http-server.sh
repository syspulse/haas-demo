#!/bin/bash

RSP_FILE=${1:-RSP_OK.txt}

RSP=`cat $RSP_FILE`

while true; do { echo -e 'HTTP/1.1 200 OK\r\n'; echo $RSP; } | nc -l 8100 -q 1; done
