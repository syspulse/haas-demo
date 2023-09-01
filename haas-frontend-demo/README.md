# HaaS Frontend

1. HaaS Authorization module
2. HaaS CirculatingSupply module

## Prepare

`yarn setup` install and prepare project

## Run

`yarn start` run project on localhost

## Build

`yarn build` build project
`yarn deploy` deploy project

## Dockerize

`yarn setup` install dependencies and generate build version, make sure you .git exist in the project
`docker build -t haas-frontend-demo .` build image
`docker run -p 3000:3000 haas-frontend-demo` run image

## Test

`yarn test` run to execute tests

## Change api

- go to src/common/config/env.dev.ts
- update variables with API endpoints