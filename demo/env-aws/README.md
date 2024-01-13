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
curl -i http://localhost/api/v1/{service}/health
```

| /api/v1/{serivce} | Description |
| ----------- | ----------- |
| auth      | Authentication       |
| user   | User Service        |
| enroll   | Enrollment        |
| notify   | Notification        |
| tag   | Tag (Label)        |
| token   | Token Search        |
| circ   | Circulation Supply        |
| intercept   | Interceptor        |


Public:

```
curl -i http://api.demo.hacken.cloud/api/v1/auth/health
```


By default compose is configured in __permissive__ API mode (no auth)

To enable Authentication and Authorization, modify [docker-compose.yaml]:

Remove: `JAVA_OPTS: -Dgod`

```
docker-compose restart
```

## API Documentation

Every Service exposes API as Swagger (and UI)

```
curl -i http://api.demo.hacken.cloud/api/v1/{service}/doc/swagger.json | jq .
```

Examples:

```
curl -i http://api.demo.hacken.cloud/api/v1/user/doc/swagger.json | jq .
```

Swagger UI:

```
http://api.demo.hacken.cloud/api/v1/auth/swagger
```


## SecurityGroup

