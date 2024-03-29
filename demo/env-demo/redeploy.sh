#!/bin/bash

DOCKER=${1}

REPO="syspulse"

if [ "$DOCKER" == "" ]; then
   echo "Redeploying all. Confirm or CTRL+C"
   read
fi

docker-compose stop $DOCKER
docker-compose rm -f $DOCKER
docker rmi -f  $REPO/$DOCKER 

echo "Waiting for new image..."
read

if [ "$?" == "0" ]; then
   docker-compose up -d $DOCKER

   sleep 1
   # no need to restart after fixing nginx conf
   #docker-compose restart nginx

   docker-compose logs -f  $DOCKER
fi


