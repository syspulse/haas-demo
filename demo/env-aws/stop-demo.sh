#!/bin/bash
CWD=`echo $(dirname $(readlink -f $0))`
cd $CWD

docker-compose down
