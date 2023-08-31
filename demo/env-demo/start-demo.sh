#!/bin/bash
CWD=`echo $(dirname $(readlink -f $0))`
cd $CWD

source ./env-demo.sh

../../infra/aws/aws-ecr-login.sh

docker-compose up -d

docker-compose logs
