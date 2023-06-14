#!/bin/bash                                                                                                                                                                                            
CWD=`echo $(dirname $(readlink -f $0))`

export LD_LIBRARY_PATH=$CWD

HOST=${1:-broker}
PORT=${2:-9092}

wait() {
    while true; do
    if ! $CWD/kafkacat -b $1:$2 -L
    then
        echo "$1 not available, retrying..."
        sleep 1
    else
        echo "$1 is available"
        sleep 1
        break;
    fi
    done;
}

wait $HOST $PORT
