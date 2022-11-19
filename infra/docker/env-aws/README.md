# Local HaaS Environment with AWS binding

## Setup

1. Source HaaS environment

```
source ./env-aws.sh
```

2. Export your AWS credentials:

```
export AWS_ACCESS_KEY_ID=xxx
export AWS_SECRET_ACCESS_KEY=xxx
export AWS_REGION=eu-west-1
```

3. Login to AWS Docker Registry

This is needed to pull images from private DCR

```
aws ecr get-login-password --region eu-west-1 | docker login --username AWS --password-stdin $AWS_ACCOUNT.dkr.ecr.${AWS_REGION}.amazonaws.com
```

## Run

```
docker-compose up -d
docler-compose logs -f 
```

## Check API

Locally

```
curl -i http://localhost/api/v1/auth/health
curl -i http://localhost/api/v1/user/health
curl -i http://localhost/api/v1/notify/health
curl -i http://localhost/api/v1/enroll/health
curl -i http://localhost/api/v1/tag/health
curl -i http://localhost/api/v1/token/health
```

Public:

```
curl -i http://api.hacken.cloud/api/v1/auth/health
```


By default compose is configured in __permissive__ API mode (no auth)

To enable Authentication and Authorization, modify [docker-compose.yaml]:

Remove: `JAVA_OPTS: -Dgod`

```
docker-compose restart
```


## SecurityGroup

Only the following IPs are whitelisted:

```
188.163.122.27 (vg)
89.216.24.81 (sc)
```