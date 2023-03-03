#!/bin/bash                                                                                                                                                                                            
CWD=`echo $(dirname $(readlink -f $0))`
cd $CWD

t=`pwd`;
APP=`basename "$t"`
CONF=`echo $APP | awk -F"-" '{print $2}'`

export SITE=${SITE:-$CONF}

MAIN=io.syspulse.haas.token.App

>&2 echo "app: $APP"
>&2 echo "site: $SITE"
>&2 echo "main: $MAIN"

if [ "$DOCKER" != "" ]; then
   #docker run --rm -it -v `pwd`/store:/store -p 8080:8080 syspulse/$APP:latest --datastore='file:///store' $@
   docker run --rm -it -v /mnt/share/data/haas/gecko/tokens:/store -p 8080:8080 syspulse/$APP:latest $@
else
   exec ../run-app.sh $APP $MAIN "$@"
fi