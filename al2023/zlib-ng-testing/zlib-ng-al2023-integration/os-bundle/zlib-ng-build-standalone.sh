#!/bin/bash
REGISTRY_PATH=ghcr.io
IMAGE_PATH=plasticity-cloud/amazonlinux
IMAGE_NAME=zlib-ng
docker rmi $REGISTRY_PATH/$IMAGE_PATH/$IMAGE_NAME-builder:2023
docker rmi $REGISTRY_PATH/$IMAGE_PATH/$IMAGE_NAME:2023
docker build --force-rm -f Dockerfile --target rpm-builder --tag $REGISTRY_PATH/$IMAGE_PATH/zlib-ng-builder:2023 ./$IMAGE_NAME
docker build --force-rm -f Dockerfile --target al2023-zlib-ng-dist --tag $REGISTRY_PATH/$IMAGE_PATH/zlib-ng:2023 ./$IMAGE_NAME

docker run -d --name=zlib-ng-builder $REGISTRY_PATH/$IMAGE_PATH/$IMAGE_NAME-builder:2023 bash
mkdir -p ./releases/latest
rm -rf ./releases/latest/al2023-zlib-ng.tar.gz
docker cp zlib-ng-builder:/home/rpm-builder/al2023-zlib-ng.tar.gz ./releases/latest/al2023-zlib-ng.tar.gz
docker stop zlib-ng-builder
docker rm zlib-ng-builder
