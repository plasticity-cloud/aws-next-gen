#!/bin/bash
REGISTRY_PATH=ghcr.io
IMAGE_TAG=plasticity-cloud/amazonlinux/rpm-builder:2023
docker rmi $REGISTRY_PATH/$IMAGE_TAG
docker build -f Dockerfile --tag $REGISTRY_PATH/$IMAGE_TAG ./rpm-builder

