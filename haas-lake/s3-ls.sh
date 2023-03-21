#!/bin/bash

ENV=${ENV:-dev}

if [ "$1" == "-r" ]; then
  R="--recursive"
  shift
fi

DIR=${1}

aws s3 ls $R s3://haas-data-${ENV}/${DIR}
