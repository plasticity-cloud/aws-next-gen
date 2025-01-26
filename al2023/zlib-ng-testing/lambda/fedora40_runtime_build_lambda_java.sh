
AWS_REGION=us-east-1
AWS_ACCOUNT=864899852480

IMAGE_REGISTRY=${AWS_ACCOUNT}.dkr.ecr.${AWS_REGION}.amazonaws.com
#aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin $IMAGE_REGISTRY


PROJECT_ROOT=java/$1

BUILD_ROOT=$PWD

cd $PROJECT_ROOT
mvn clean
mvn dependency:go-offline dependency:copy-dependencies
mvn package

cd $BUILD_ROOT

IMAGE_TARGET=$2
IMAGE_TAG=java-21-fedora40
IMAGE_NAME=plasticity/amazonlinux
docker build -f ./fedora40_Dockerfile_java -t $IMAGE_REGISTRY/$IMAGE_NAME:$IMAGE_TAG .

docker push $IMAGE_REGISTRY/$IMAGE_NAME:$IMAGE_TAG
