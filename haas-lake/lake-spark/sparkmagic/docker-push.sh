#!/bin/bash

../../../infra/aws/aws-ecr-login.sh

../../../tools/docker-push.sh $@
