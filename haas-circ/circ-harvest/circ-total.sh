#!/bin/bash
PWD=`echo $(dirname $(readlink -f $0))`

INPUT=${1:-UNI-1000.csv}

export JAVA_OPTS=-Xmx2500M
ammonite --predef $PWD/circ-imports.sc $PWD/circ-total.sc --input $INPUT 
#ammonite --predef $PWD/circ-imports.sc $PWD/circ-holders.sc --input $INPUT 2>/dev/null
