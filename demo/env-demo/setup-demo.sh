#!/bin/bash

export DEMO_DATA=/mnt/share/data/skel/demo/demo-haas/data

echo "Data: $DEMO_DATA"

source env-demo.sh

mkdir -p $DEMO_DATA
mkdir -p $DEMO_DATA/db_data

cp -r store $DEMO_DATA/
#mkdir -p $DEMO_DATA/store

# Initialize dirs if not copied
mkdir -p $DEMO_DATA/store/auth
mkdir -p $DEMO_DATA/store/auth/cred
mkdir -p $DEMO_DATA/store/auth/rbac/permissions
mkdir -p $DEMO_DATA/store/auth/rbac/users

mkdir -p $DEMO_DATA/store/notify
mkdir -p $DEMO_DATA/store/token
mkdir -p $DEMO_DATA/store/label
mkdir -p $DEMO_DATA/store/pipeline
mkdir -p $DEMO_DATA/store/intercept
mkdir -p $DEMO_DATA/store/job
mkdir -p $DEMO_DATA/store/script

mkdir -p $DEMO_DATA/store/s3

# authorizations
mkdir -p $DEMO_DATA/auth
cp ACCESS_TOKEN_SERVICE $DEMO_DATA/auth/ACCESS_TOKEN_SERVICE


# link
ln -s $DEMO_DATA ./data

# unpack data

pushd $DEMO_DATA/store/job
tar xvf jobs.tar.gz
rm jobs.tar.gz
popd

pushd $DEMO_DATA/store/token
tar xvf tokens.tar.gz
rm tokens.tar.gz
popd

pushd $DEMO_DATA/store/label
tar xvf labels.tar.gz
rm labels.tar.gz
popd




