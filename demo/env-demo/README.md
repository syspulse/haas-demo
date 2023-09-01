# Demo HaaS Environment with local Infra (Kafka)

## Setup

0. Init

```
./setup-demo.sh
```

1. Edit and Source environment

```
source ./env-demo.sh
```

2. Run

```
docker-compose up -d
docler-compose logs -f 
```

3. Check API

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
| job   | Job        |
| intercept   | intercept        |


4. Console

Open in Browser and login

[http://localhost:3000]
