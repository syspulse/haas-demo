#!/bin/bash

DOCKER=$1

if [[ $DOCKER = skel-* ]]; then
   REPO="syspulse"
else
   REPO="649502643044.dkr.ecr.eu-west-1.amazonaws.com/syspulse"
fi

docker-compose stop $DOCKER
docker-compose rm -f $DOCKER
docker rmi -f  $REPO/$DOCKER 

echo "Waiting for new image..."
read

if [ "$?" == "0" ]; then
   docker-compose up -d $DOCKER

   sleep 1
   docker-compose restart nginx

   docker-compose logs -f  $DOCKER
fi


