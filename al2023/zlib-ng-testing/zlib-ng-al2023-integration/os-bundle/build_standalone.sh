#!/bin/bash
REGISTRY_PATH=ghcr.io
IMAGE_TAG=plasticity-cloud/al2023-zlib-ng
docker rmi $REGISTRY_PATH/amazonlinux:$IMAGE_TAG-zlib-ng-dist
docker rmi $REGISTRY_PATH/amazonlinux:$IMAGE_TAG-zlib-ng-builder
docker build -f Dockerfile --target zlib-ng-builder --tag $REGISTRY_PATH/amazonlinux:$IMAGE_TAG-zlib-ng-builder .
docker build -f Dockerfile --target zlib-ng-dist --tag $REGISTRY_PATH/amazonlinux:$IMAGE_TAG-zlib-ng-dist .

docker run -b --name=zlib-ng-builder $REGISTRY_PATH/amazonlinux:$IMAGE_TAG-zlib-ng-builder bash
docker cp zlib-ng-builder:/root/al2023-zlib-ng.tar.gz ./al2023-zlib-ng.tar.gz
docker stop zlib-ng-builder
docker rm zlib-ng-builder
