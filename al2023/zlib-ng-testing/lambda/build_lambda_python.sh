
AWS_REGION=us-east-1
AWS_ACCOUNT=864899852480

IMAGE_REGISTRY=${AWS_ACCOUNT}.dkr.ecr.${AWS_REGION}.amazonaws.com
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin $IMAGE_REGISTRY


IMAGE_TARGET=$1
IMAGE_TAG=python-3.13-$IMAGE_TARGET
IMAGE_NAME=plasticity/amazonlinux
docker build -f ./Dockerfile_python --target $IMAGE_TARGET -t $IMAGE_REGISTRY/$IMAGE_NAME:$IMAGE_TAG .

docker push $IMAGE_REGISTRY/$IMAGE_NAME:$IMAGE_TAG
