#!/bin/bash                                                                                                                                                                                            
CWD=`echo $(dirname $(readlink -f $0))`
cd $CWD
export APP_HOME=`pwd`

t=`pwd`;
APP=`basename "$t"`
CONF=`echo $APP | awk -F"-" '{print $2}'`

export SITE=${SITE:-$CONF}

MAIN=io.syspulse.haas.ingest.eth.App

DOCKER_DEF=${DOCKER_DEF:-649502643044.dkr.ecr.eu-west-1.amazonaws.com}

>&2 echo "app: $APP"
>&2 echo "site: $SITE"
>&2 echo "main: $MAIN"

if [ "$DOCKER" != "" ]; then
  case "$DOCKER" in
     "default")
        docker run --rm -i -v `pwd`/output:/output ${DOCKER_DEF}/syspulse/$APP:latest $@
        ;;
     *)
        docker run --rm -i -v `pwd`/output:/output syspulse/$APP:latest $@
        ;;
  esac
   
else
   exec ../../run-app.sh $APP $MAIN $@
fi

