
AWS_REGION=us-east-1
AWS_ACCOUNT=864899852480

IMAGE_REGISTRY=${AWS_ACCOUNT}.dkr.ecr.${AWS_REGION}.amazonaws.com
#aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin $IMAGE_REGISTRY


PROJECT_ROOT=java/$1

BUILD_ROOT=$PWD

cd $PROJECT_ROOT

mvn clean package
mvn compile dependency:copy-dependencies -DincludeScope=runtime

cd $BUILD_ROOT

IMAGE_TARGET=$2
IMAGE_TAG=java-21-$IMAGE_TARGET
IMAGE_NAME=plasticity/amazonlinux
docker build -f ./Dockerfile_java --target $IMAGE_TARGET -t $IMAGE_REGISTRY/$IMAGE_NAME:$IMAGE_TAG .

docker push $IMAGE_REGISTRY/$IMAGE_NAME:$IMAGE_TAG
