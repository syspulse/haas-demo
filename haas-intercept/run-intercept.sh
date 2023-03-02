#!/bin/bash                                                                                                                                                                                            
CWD=`echo $(dirname $(readlink -f $0))`
cd $CWD
export APP_HOME=`pwd`

t=`pwd`;
APP=`basename "$t"`
CONF=`echo $APP | awk -F"-" '{print $2}'`

export SITE=${SITE:-$CONF}

# SCRIPT=${SCRIPT:-file://scripts/script-1.js}

MAIN=io.syspulse.haas.intercept.App

DOCKER_DEF=${DOCKER_DEF:-}
DOCKER_AWS=${DOCKER_AWS:-649502643044.dkr.ecr.eu-west-1.amazonaws.com}
S3_BUCKET=haas-data-dev

>&2 echo "app: $APP"
>&2 echo "site: $SITE"
>&2 echo "main: $MAIN"
# >&2 echo "script: $SCRIPT"
>&2 echo "ETH_RPC: $ETH_RPC"

if [ "$DOCKER" != "" ] && [ "$DOCKER" != "none" ]; then
  >&2 echo "DOCKER: $DOCKER"
  case "$DOCKER" in
     "aws")
        docker run --rm -i -v `pwd`/output:/output -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY -e S3_BUCKET=$S3_BUCKET --privileged ${DOCKER_AWS}/syspulse/$APP:latest $@
        ;;
     "local"|"default")
        docker run --rm -i -v `pwd`/output:/output -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY -e S3_BUCKET=$S3_BUCKET --privileged ${DOCKER_DEF}syspulse/$APP:latest $@
        #docker run --rm -i --user 1000 -v `pwd`/output:/output ${DOCKER_AWS}/syspulse/$APP:latest $@
        #docker run --rm -i -v `pwd`/output:/output ${DOCKER_AWS}/syspulse/$APP:latest $@
        ;;
     *)
        docker run --rm -i -v `pwd`/output:/output -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY -e S3_BUCKET=$S3_BUCKET --privileged $DOCKER $@
        ;;
  esac
   
else
   exec ../run-app.sh $APP $MAIN $@
fi

