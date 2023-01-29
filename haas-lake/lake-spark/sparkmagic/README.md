# Sparknagic


## Build base image

1. clone: [https://github.com/jupyter-incubator/sparkmagic.git](https://github.com/jupyter-incubator/sparkmagic.git)

2. Modify [sparkmatic/example_config.json]

3. Build base image

```
docker build -f Dockerfile.jupyter -t jupyter/sparkmagic .
```

## Build Customized image for haas-demo

```
./docker-build.sh
```

## Verify the custom build

```
./run-sparkmagic.sh
```

```
curl -i http://localhost:8888/sparkmagic
```


## Push images

```
./docker-push.sh aws
```




