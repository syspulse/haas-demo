#!/bin/bash
PWD=`echo $(dirname $(readlink -f $0))`

INPUT=${1:-UNI-1000.csv}

ammonite --watch --predef $PWD/circ-imports.sc $PWD/circ-example.sc --input $INPUT 
