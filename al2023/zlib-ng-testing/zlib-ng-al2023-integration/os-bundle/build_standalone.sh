#!/bin/bash
REGISTRY_PATH=localhost
IMAGE_TAG=al2023
docker rmi $REGISTRY_PATH/amazonlinux:$IMAGE_TAG-zlib-ng-dist
docker rmi $REGISTRY_PATH/amazonlinux:$IMAGE_TAG-zlib-ng-builder
docker build -f Dockerfile --target zlib-ng-builder --tag $REGISTRY_PATH/amazonlinux:$IMAGE_TAG-zlib-ng-builder .
docker build -f Dockerfile --target zlib-ng-dist --tag $REGISTRY_PATH/amazonlinux:$IMAGE_TAG-zlib-ng-dist .
